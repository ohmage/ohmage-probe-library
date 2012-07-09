
package org.ohmage.probemanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Easily connect and write probes to ohmage to be uploaded.
 * 
 * @author cketcham
 */
public class ProbeWriter implements ServiceConnection {

    private static final String ACTION_WRITE_PROBE = "org.ohmage.probemanager.ACTION_WRITE_PROBE";

    private static final String TAG = "ProbeWriter";

    public static final int DEFAULT_UPLOAD_PRIORITY = 0;

    /**
     * Holds a list of probes which were collected before the service connected
     */
    private final ArrayList<ProbeBuilder> mBuffer;

    private IProbeManager dataService;

    private final Context mContext;

    public ProbeWriter(Context context) {
        mContext = context;
        mBuffer = new ArrayList<ProbeBuilder>();
    }

    /** is called once the bind succeeds */
    @Override
    public synchronized void onServiceConnected(ComponentName name, IBinder service) {
        dataService = IProbeManager.Stub.asInterface(service);

        // Write any probes which came before we were connected
        for (ProbeBuilder probe : mBuffer) {
            try {
                probe.write(this);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        mBuffer.clear();
    }

    /*** is called once the remote service is no longer available */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        dataService = null;
    }

    public boolean connect() {
        Intent intent = new Intent(ACTION_WRITE_PROBE);
        return mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public void close() {
        if (dataService != null) {
            mContext.unbindService(this);
            dataService = null;
        }
    }

    public synchronized void write(String observerId, int observerVersion,
            String streamId, int streamVersion, int uploadPriority, String metadata, String data)
            throws RemoteException {
        if (dataService != null) {
            dataService.send(observerId, observerVersion, streamId, streamVersion, uploadPriority, metadata, data);
        } else {
            mBuffer.add(new ProbeBuilder(observerId, observerVersion)
                    .setStream(streamId, streamVersion)
                    .setData(data)
                    .setMetadata(metadata)
                    .setUploadPriority(uploadPriority));
            if (!connect())
                mBuffer.clear(); // No point in buffering data if we can't
                                 // connect to the service
        }
    }

    public void write(String observerId, int observerVersion, String streamId, int streamVersion,
            String metadata, String data)
            throws RemoteException {
        write(observerId, observerVersion, streamId, streamVersion, DEFAULT_UPLOAD_PRIORITY, metadata, data);
    }

    /**
     * Probe builder class which makes it easy to create the probe response and
     * send it
     * 
     * @author cketcham
     */
    public static class ProbeBuilder {

        public ProbeBuilder() {
        }

        public ProbeBuilder(String observerName, int observerVersion) {
            mObserverName = observerName;
            mObserverVersion = observerVersion;
        }

        /**
         * Observer name
         */
        private String mObserverName;

        /**
         * Observer version number
         */
        private int mObserverVersion;

        /**
         * Stream name
         */
        private String mStreamId;

        /**
         * Stream version number
         */
        private int mStreamVersion;

        /**
         * Upload priority
         */
        private int mUploadPriority = DEFAULT_UPLOAD_PRIORITY;

        /**
         * Data Json
         */
        private String mData;

        /**
         * Metadata Json
         */
        private String mMetadata;

        /**
         * Point id
         */
        private String mId;

        /**
         * The ISO8601-formatted date-time-timezone string. If this is not
         * present, then the time and timezone fields will be used.
         */
        private String mTimestamp;

        /**
         * The number of milliseconds since the Unix epoch at UTC. This will
         * only be checked if "timestamp" was not present.
         */
        private Long mTime;

        /**
         * The timezone of the device at the time of this recording as a string.
         * This will only be checked if the "timestamp" was not present and the
         * "time" was.
         */
        private String mTimezone;

        /**
         * <ul>
         * <li>time: The number of milliseconds since the Unix epoch at UTC.</li>
         * <li>latitude: The latitude component.</li>
         * <li>longitude: The longitude component.</li>
         * <li>accuracy: The accuracy of the reading.</li>
         * <li>provider: A string representing who provided this information.</li>
         * <ul>
         */
        private Location mLocation;

        /**
         * Location timezone (will most likely be the same as timezone)
         */
        private String mLocationTimezone;

        public ProbeBuilder setObserver(String observerId, int observerVersion) {
            mObserverName = observerId;
            mObserverVersion = observerVersion;
            return this;
        }

        public ProbeBuilder setStream(String streamId, int streamVersion) {
            mStreamId = streamId;
            mStreamVersion = streamVersion;
            return this;
        }

        public ProbeBuilder setUploadPriority(int uploadPriority) {
            mUploadPriority = uploadPriority;
            return this;
        }

        public ProbeBuilder setData(String data) {
            mData = data;
            return this;
        }

        public ProbeBuilder setMetadata(String metadata) {
            clear();
            mMetadata = metadata;
            return this;
        }

        public ProbeBuilder withId(String id) {
            mId = id;
            return this;
        }

        public ProbeBuilder withTimestamp(String timestamp) {
            mTimestamp = timestamp;
            return this;
        }

        public ProbeBuilder withTime(long time, String timezone) {
            mTime = time;
            mTimezone = timezone;
            return this;
        }

        public ProbeBuilder withLocation(Location location) {
            mLocation = location;
            return this;
        }

        public ProbeBuilder withLocation(long time, String timezone, double latitude,
                double longitude, float accuracy, String provider) {
            mLocation = new Location(provider);
            mLocation.setTime(time);
            mLocationTimezone = timezone;
            mLocation.setLatitude(latitude);
            mLocation.setLongitude(longitude);
            mLocation.setAccuracy(accuracy);
            return this;
        }

        public ProbeBuilder clear() {
            mTimestamp = null;
            mTime = null;
            mTimezone = null;
            mLocation = null;
            mMetadata = null;
            return this;
        }

        public void write(ProbeWriter writer) throws RemoteException {
            setupWrite();
            writer.write(mObserverName, mObserverVersion, mStreamId, mStreamVersion, mUploadPriority, mMetadata, mData);
        }

        private void setupWrite() {
            buildMetaData();
            if (TextUtils.isEmpty(mData))
                throw new RuntimeException("Must specify data");
        }

        /**
         * Builds the metadata string. Only sets the string if not null.
         */
        private void buildMetaData() {
            try {
                JSONObject metadata = new JSONObject();
                if (mId != null)
                    metadata.put("id", mId);
                if (mTimestamp != null)
                    metadata.put("timestamp", mTimestamp);
                if (mTime != null)
                    metadata.put("time", mTime);
                if (mTimezone != null)
                    metadata.put("timezone", mTimezone);
                if (mLocation != null) {
                    JSONObject location = new JSONObject();
                    location.put("time", mLocation.getTime());
                    location.put("timezone", mLocationTimezone);
                    location.put("latitude", mLocation.getLatitude());
                    location.put("longitude", mLocation.getLongitude());
                    location.put("accuracy", mLocation.getAccuracy());
                    location.put("provider", mLocation.getProvider());
                    metadata.put("location", location);
                }
                if (metadata.length() > 0)
                    mMetadata = metadata.toString();
            } catch (JSONException e) {
                Log.e(TAG, "JSON format exception");
            }
        }
    }
}
