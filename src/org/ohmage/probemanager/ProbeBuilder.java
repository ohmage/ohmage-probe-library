
package org.ohmage.probemanager;

import android.location.Location;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimeZone;
import java.util.UUID;

/**
 * Probe builder class which makes it easy to create the probe response and send
 * it. More information on how the json data should be structured can be found
 * on the wiki
 * 
 * @see <a
 *      href="https://github.com/cens/ohmageServer/wiki/Observer-Manipulation">https://github.com/cens/ohmageServer/wiki/Observer-Manipulation</a>
 * @author cketcham
 */
public class ProbeBuilder implements ProbeWriter.Builder {

    private static final String TAG = "ProbeBuilder";

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
    private int mUploadPriority = ProbeWriter.DEFAULT_UPLOAD_PRIORITY;

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
     * The ISO8601-formatted date-time-timezone string. If this is not present,
     * then the time and timezone fields will be used.
     */
    private String mTimestamp;

    /**
     * The number of milliseconds since the Unix epoch at UTC. This will only be
     * checked if "timestamp" was not present.
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

    /**
     * The unique ID for the observer and the number describing this observer's
     * version.
     * 
     * @param observerId
     * @param observerVersion
     * @return
     */
    public ProbeBuilder setObserver(String observerId, int observerVersion) {
        mObserverName = observerId;
        mObserverVersion = observerVersion;
        return this;
    }

    /**
     * The unique identifier for the stream to which this data applies and the
     * version of this stream to which this data applies.
     * 
     * @param streamId
     * @param streamVersion
     * @return
     */
    public ProbeBuilder setStream(String streamId, int streamVersion) {
        mStreamId = streamId;
        mStreamVersion = streamVersion;
        return this;
    }

    public ProbeBuilder setUploadPriority(int uploadPriority) {
        mUploadPriority = uploadPriority;
        return this;
    }

    /**
     * Set the probe data.
     * 
     * @param data
     * @return
     */
    public ProbeBuilder setData(String data) {
        mData = data;
        return this;
    }

    /**
     * This should be a JSON object containing the metadata for this point. This
     * field is optional. This field will be ignored on write if any other
     * metadata for this probe is supplied.
     * 
     * @param metadata
     * @return
     */
    public ProbeBuilder setMetadata(String metadata) {
        clearMetadata();
        mMetadata = metadata;
        return this;
    }

    /**
     * A UUID unique to this probe.
     * 
     * @param id
     * @return
     */
    public ProbeBuilder withId(String id) {
        mId = id;
        return this;
    }

    /**
     * Generates a UUID unique to this probe.
     * 
     * @param id
     * @return
     */
    public ProbeBuilder withId() {
        mId = UUID.randomUUID().toString();
        return this;
    }

    public String id() {
        return mId;
    }

    /**
     * The ISO8601-formatted date-time-timezone string. If this is not present,
     * then the time and timezone fields will be used.
     * 
     * @param timestamp
     * @return
     */
    public ProbeBuilder withTimestamp(String timestamp) {
        mTimestamp = timestamp;
        return this;
    }

    /**
     * A long specifying the survey completion time by the number of
     * milliseconds since the UNIX epoch and the timezone ID for the timezone of
     * the device when this survey was taken.
     * 
     * @param time
     * @param timezone
     * @return
     */
    public ProbeBuilder withTime(long time, String timezone) {
        mTime = time;
        mTimezone = timezone;
        return this;
    }

    /**
     * A long specifying the survey completion time by the number of
     * milliseconds since the UNIX epoch. The timezone used is the current
     * timezone.
     * 
     * @param time
     * @param timezone
     * @return
     */
    public ProbeBuilder withTime(long time) {
        mTime = time;
        mTimezone = TimeZone.getDefault().getID();
        return this;
    }

    /**
     * Sets the time for this probe to now
     * 
     * @return
     */
    public ProbeBuilder now() {
        mTime = System.currentTimeMillis();
        mTimezone = TimeZone.getDefault().getID();
        return this;
    }

    /**
     * Location that this response was taken
     * 
     * @param location
     * @param timezone
     * @return
     */
    public ProbeBuilder withLocation(Location location, String timezone) {
        mLocation = location;
        mLocationTimezone = timezone;
        return this;
    }

    /**
     * Location that this response was taken
     * 
     * @param time
     * @param timezone
     * @param latitude
     * @param longitude
     * @param accuracy
     * @param provider
     * @return
     */
    public ProbeBuilder withLocation(long time, String timezone, double latitude, double longitude,
            float accuracy, String provider) {
        mLocation = new Location(provider);
        mLocation.setTime(time);
        mLocationTimezone = timezone;
        mLocation.setLatitude(latitude);
        mLocation.setLongitude(longitude);
        mLocation.setAccuracy(accuracy);
        return this;
    }

    /**
     * Clears all metadata for this probe. Any other data will remain.
     * 
     * @return
     */
    public ProbeBuilder clearMetadata() {
        mId = null;
        mTimestamp = null;
        mTime = null;
        mTimezone = null;
        mLocation = null;
        mMetadata = null;
        return this;
    }

    /**
     * Clears everything associated with this probe.
     * 
     * @return
     */
    public ProbeBuilder clear() {
        clearMetadata();
        mObserverName = null;
        mObserverVersion = 0;
        mStreamId = null;
        mStreamVersion = 0;
        mData = null;
        mUploadPriority = ProbeWriter.DEFAULT_UPLOAD_PRIORITY;
        return this;
    }

    @Override
    public void write(ProbeWriter writer) throws RemoteException {
        setupWrite();
        writer.write(mObserverName, mObserverVersion, mStreamId, mStreamVersion, mUploadPriority,
                mMetadata, mData);
    }

    private void setupWrite() {
        buildMetaData();
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
