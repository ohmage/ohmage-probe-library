
package org.ohmage.probemanager;

import android.location.Location;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Response builder class which makes it easy to create the response and send
 * it. More information on how the json data should be structured can be found
 * on the wiki
 * 
 * @see <a
 *      href="https://github.com/cens/ohmageServer/wiki/Survey-Manipulation">https://github.com/cens/ohmageServer/wiki/Survey-Manipulation</a>
 * @author cketcham
 */
public class ResponseBuilder implements ProbeWriter.Builder {

    private static final String TAG = "ProbeBuilder";

    private static final String UNAVALIABLE_LOCATION = "unavailable";

    public ResponseBuilder() {
    }

    public ResponseBuilder(String campaignUrn, String campaignCreationTimestamp) {
        mCampaignUrn = campaignUrn;
        mCampaignCreationTimestamp = campaignCreationTimestamp;
    }

    /**
     * Campaign Urn
     */
    private String mCampaignUrn;

    /**
     * Timestamp of when the campaign was created
     */
    private String mCampaignCreationTimestamp;

    /**
     * Upload priority
     */
    private int mUploadPriority = ProbeWriter.DEFAULT_UPLOAD_PRIORITY;

    /**
     * Data Json
     */
    private String mData;

    /**
     * A UUID unique to this survey response.
     */
    private String mSurveyKey;

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
     * A string describing location status. Must be one of: unavailable, valid,
     * inaccurate, stale. If the status is unavailable, it is an error to send a
     * location object.
     */
    private String mLocationStatus = UNAVALIABLE_LOCATION;

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
     * A string defining a survey in the campaign's associated configuration
     * file at the XPath /surveys/survey/id
     */
    private String mSurveyId;

    /**
     * A JSON object with variable properties that describes the survey's launch
     * context.
     */
    private String mSurveyLaunchContext;

    /**
     * A JSON array composed of JSON object prompt responses and/or JSON object
     * repeatable set responses.
     */
    private String mResponses;

    /**
     * Campaign Urn and creation timestamp to specify the campaign
     * 
     * @param campaignUrn
     * @param campaignCreationTimestamp
     * @return
     */
    public ResponseBuilder setCampaign(String campaignUrn, String campaignCreationTimestamp) {
        mCampaignUrn = campaignUrn;
        mCampaignCreationTimestamp = campaignCreationTimestamp;
        return this;
    }

    /**
     * Upload Priority
     * 
     * @param uploadPriority
     * @return
     */
    public ResponseBuilder setUploadPriority(int uploadPriority) {
        mUploadPriority = uploadPriority;
        return this;
    }

    /**
     * Set the response data. This data will be ignored on write if response
     * data other than the campaignUrn, campaignCreationTimestamp, and
     * uploadPriority is specified
     * 
     * @param data
     * @return
     */
    public ResponseBuilder setData(String data) {
        mData = data;
        return this;
    }

    /**
     * A UUID unique to this survey response.
     * 
     * @param surveyKey
     * @return
     */
    public ResponseBuilder withSurveyKey(String surveyKey) {
        mSurveyKey = surveyKey;
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
    public ResponseBuilder withTime(long time, String timezone) {
        mTime = time;
        mTimezone = timezone;
        return this;
    }

    /**
     * Location that this response was taken
     * 
     * @param location
     * @param timezone
     * @param status: A string describing location status. Must be one of:
     *            unavailable, valid, inaccurate, stale. If the status is
     *            unavailable, it is an error to send a location object.
     * @return
     */
    public ResponseBuilder withLocation(Location location, String timezone, String status) {
        mLocation = location;
        mLocationTimezone = timezone;
        mLocationStatus = status;
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
     * @param status: A string describing location status. Must be one of:
     *            unavailable, valid, inaccurate, stale. If the status is
     *            unavailable, it is an error to send a location object.
     * @return
     */
    public ResponseBuilder withLocation(long time, String timezone, double latitude,
            double longitude, float accuracy, String provider, String status) {
        mLocation = new Location(provider);
        mLocation.setTime(time);
        mLocationTimezone = timezone;
        mLocation.setLatitude(latitude);
        mLocation.setLongitude(longitude);
        mLocation.setAccuracy(accuracy);
        mLocationStatus = status;
        return this;
    }

    /**
     * A string defining a survey in the campaign's associated configuration
     * file at the XPath /surveys/survey/id
     * 
     * @param surveyId
     * @return
     */
    public ResponseBuilder withSurveyId(String surveyId) {
        mSurveyId = surveyId;
        return this;
    }

    /**
     * A JSON object with variable properties that describes the survey's launch
     * context.
     * 
     * @param surveyLaunchContext
     * @return
     */
    public ResponseBuilder withSurveyLaunchContext(String surveyLaunchContext) {
        mSurveyLaunchContext = surveyLaunchContext;
        return this;
    }

    /**
     * Values which describes the survey's launch context.
     * 
     * @param launchTime
     * @param launchTimezone
     * @param activeTriggers
     * @return
     * @throws JSONException
     */
    public ResponseBuilder withSurveyLaunchContext(long launchTime, String launchTimezone,
            String... activeTriggers) {
        try {
            JSONObject launchContext = new JSONObject();
            launchContext.put("launch_time", launchTime);
            launchContext.put("launch_timezone", launchTimezone);
            launchContext.put("active_triggers", new JSONArray(Arrays.asList(activeTriggers)));
            mSurveyLaunchContext = launchContext.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this;
    }

    /**
     * A JSON array composed of JSON object prompt responses and/or JSON object
     * repeatable set responses.
     * 
     * @param responses
     * @return
     */
    public ResponseBuilder withResponses(String responses) {
        mResponses = responses;
        return this;
    }

    /**
     * Clear all data from this response
     * 
     * @return
     */
    public ResponseBuilder clear() {
        mCampaignUrn = null;
        mCampaignCreationTimestamp = null;
        mUploadPriority = ProbeWriter.DEFAULT_UPLOAD_PRIORITY;
        mData = null;
        mSurveyKey = null;
        mTime = null;
        mTimezone = null;
        mLocationStatus = UNAVALIABLE_LOCATION;
        mLocation = null;
        mLocationTimezone = null;
        mSurveyLaunchContext = null;
        mResponses = null;
        return this;
    }

    @Override
    public void write(ProbeWriter writer) throws RemoteException {
        setupWrite();
        writer.writeResponse(mCampaignUrn, mCampaignCreationTimestamp, mUploadPriority, mData);
    }

    private void setupWrite() {
        buildData();
        if (TextUtils.isEmpty(mData))
            throw new RuntimeException("Must specify data");
    }

    /**
     * Builds the data string. Only sets the string if not null.
     */
    private void buildData() {
        try {
            JSONObject data = new JSONObject();
            if (mSurveyKey != null)
                data.put("survey_key", mSurveyKey);
            if (mTime != null)
                data.put("time", mTime);
            if (mTimezone != null)
                data.put("timezone", mTimezone);
            if (mLocationStatus != null)
                data.put("location_status", mLocationStatus);
            if (mLocation != null) {
                JSONObject location = new JSONObject();
                location.put("time", mLocation.getTime());
                location.put("timezone", mLocationTimezone);
                location.put("latitude", mLocation.getLatitude());
                location.put("longitude", mLocation.getLongitude());
                location.put("accuracy", mLocation.getAccuracy());
                location.put("provider", mLocation.getProvider());
                data.put("location", location);
            }
            if (mSurveyId != null)
                data.put("survey_id", mSurveyId);
            if (mSurveyLaunchContext != null)
                data.put("survey_launch_context", new JSONObject(mSurveyLaunchContext));
            if (mResponses != null)
                data.put("responses", new JSONArray(mResponses));
            if (data.length() > 0)
                mData = data.toString();
        } catch (JSONException e) {
            Log.e(TAG, "JSON format exception");
        }
    }
}
