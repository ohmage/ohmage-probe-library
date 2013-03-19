
package org.ohmage.probemanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

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
    private final ArrayList<Builder> mBuffer;

    private IProbeManager dataService;

    private final Context mContext;

    private ServiceConnectionChange mListener;

    public static interface ServiceConnectionChange {
        public void onServiceConnected(ProbeWriter writer);

        public void onServiceDisconnected(ProbeWriter writer);
    }

    public ProbeWriter(Context context) {
        mContext = context;
        mBuffer = new ArrayList<Builder>();
    }

    /** is called once the bind succeeds */
    @Override
    public synchronized void onServiceConnected(ComponentName name, IBinder service) {
        dataService = IProbeManager.Stub.asInterface(service);

        // Write any probes which came before we were connected
        for (Builder probe : mBuffer) {
            try {
                probe.write(this);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        mBuffer.clear();

        if (mListener != null)
            mListener.onServiceConnected(this);
    }

    /*** is called once the remote service is no longer available */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        dataService = null;

        if (mListener != null)
            mListener.onServiceDisconnected(this);
    }

    public boolean connect() {
        Intent intent = new Intent(ACTION_WRITE_PROBE);
        return mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public void setServiceConnectionChangeListener(ServiceConnectionChange listener) {
        mListener = listener;
    }

    public void close() {
        mContext.unbindService(this);
        dataService = null;
    }

    public synchronized void write(String observerId, int observerVersion, String streamId,
            int streamVersion, int uploadPriority, String metadata, String data)
            throws RemoteException {

        if (TextUtils.isEmpty(data))
            throw new RuntimeException("Must specify data");

        // Check that the data is valid json
        try {
            new JSONObject(data);
        } catch (JSONException e) {
            throw new RuntimeException("data not valid json");
        }

        // Check that the metadata is valid json
        if (!TextUtils.isEmpty(metadata)) {
            try {
                new JSONObject(metadata);
            } catch (JSONException e) {
                throw new RuntimeException("metadata not valid json");
            }
        }

        if (dataService != null) {
            dataService.writeProbe(observerId, observerVersion, streamId, streamVersion,
                    uploadPriority, metadata, data);
        } else {
            mBuffer.add(new ProbeBuilder(observerId, observerVersion)
                    .setStream(streamId, streamVersion).setData(data).setMetadata(metadata)
                    .setUploadPriority(uploadPriority));
            if (!connect())
                mBuffer.clear(); // No point in buffering data if we can't
                                 // connect to the service
        }
    }

    public void write(String observerId, int observerVersion, String streamId, int streamVersion,
            String metadata, String data) throws RemoteException {
        write(observerId, observerVersion, streamId, streamVersion, DEFAULT_UPLOAD_PRIORITY,
                metadata, data);
    }

    public synchronized void writeResponse(String campaignUrn, String campaignCreationTimestamp,
            int uploadPriority, String data) throws RemoteException {
        if (dataService != null) {
            dataService.writeResponse(campaignUrn, campaignCreationTimestamp, uploadPriority, data);
        } else {
            mBuffer.add(new ResponseBuilder(campaignUrn, campaignCreationTimestamp).setData(data)
                    .setUploadPriority(uploadPriority));
            if (!connect())
                mBuffer.clear(); // No point in buffering data if we can't
                                 // connect to the service
        }
    }

    public void writeResponse(String campaignUrn, String campaignCreationTimestamp, String data)
            throws RemoteException {
        writeResponse(campaignUrn, campaignCreationTimestamp, DEFAULT_UPLOAD_PRIORITY, data);
    }

    public interface Builder {
        public void write(ProbeWriter writer) throws RemoteException;
    }

}
