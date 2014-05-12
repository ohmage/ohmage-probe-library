ohmageProbeLibrary
==================

###This library has been depricated for ohmage 3.0 and is here only for compatibility with ohmage 2.x. The new library can be found here https://github.com/ohmage/android-stream-lib

The ohmageProbeLibrary is an Android Library apk which makes it easier to write a probe apk. The
Library consists of the ProbeWriter which helps handle connecting and disconnecting from the
ohmage service, as well as the ProbeBuilder and ResponseBuilder classes which help in creating
the objects to send to ohmage. Look at [ohmageProbeExample](https://github.com/cketcham/ohmageProbeExample)
as an example apk which uses this library.

[ProbeWriter](https://github.com/cens/ohmageProbeLibrary/wiki/ProbeWriter)
-----------

The [ProbeWriter](https://github.com/cens/ohmageProbeLibrary/wiki/ProbeWriter) helps with managing
a connection with ohmage to send probe data. It has the methods `connect()` and `close()`
which help connect to ohmage. The ProbeWriter is a wrapper around the two aidl methods exposed
by ohmage, `writeProbe` and `writeResponse`. These two methods are handled by `write` for Probes,
and `writeResponse` for Responses. The ProbeWriter can be used by itself to write probe data to
ohmage, or the two Builders can be used to help with the process of formatting the data correctly.

[ProbeBuilder](https://github.com/cens/ohmageProbeLibrary/wiki/ProbeBuilder)
------------

The [ProbeBuilder](https://github.com/cens/ohmageProbeLibrary/wiki/ProbeBuilder) class makes it
easy to create probe data. You can specify exactly the data you want to send in the probe and easily
send it. Use the builder to set the observer, stream, data and any metadata that you would like to
include based on the specification of the observer. Call `write` and pass in the ProbeWriter object
to send the data to ohmage.

[ResponseBuilder](https://github.com/cens/ohmageProbeLibrary/wiki/ResponseBuilder)
---------------

The [ResponseBuilder](https://github.com/cens/ohmageProbeLibrary/wiki/ResponseBuilder) works
similarly to the ProbeBuilder except that it assists with the creation of a response.

Probe Examples
--------------
* [LogProbe](https://github.com/cens/LogProbe) - Wrapper around android.util.Log which uploads logs as an observer
* [Mobility](https://github.com/cens/MobilityPhone) - An activity classification service for android

Expose Probe Information to ohmage
----------------------------------

**This is not required but it can be useful for the user to have control over their probe data**. Probes can also give information to ohmage so users can easily access and configure any probes installed on the system. A meta-data tag should be added to the manifest of the probe under the application tag. It will probably look like this:

    <meta-data
        android:name="org.ohmage.probemanager"
        android:resource="@xml/probe" />

In the /res/xml/ folder a probe.xml file should be created which defines your probe. The information should match the observer definition on the server:

    <probe xmlns:probe="http://schemas.android.com/apk/res-auto"
        probe:observerId="edu.ucla.cens.Mobility"
        probe:observerName="Mobility"
        probe:observerVersionCode="2012061300"
        probe:observerVersionName="1.4.3" />

Or if your apk has multiple probes you can define more than one with:

    <probes xmlns:probe="http://schemas.android.com/apk/res-auto" >
        <probe
            probe:observerId="edu.ucla.cens.Mobility"
            probe:observerName="Mobility"
            probe:observerVersionCode="2012061300"
            probe:observerVersionName="1.4.3" />
        <probe
            probe:observerId="org.ohmage.LogProbe"
            probe:observerName="LogProbe"
            probe:observerVersionCode="1"
            probe:observerVersionName="1.0" />
    </probes>

In your manifest, you can also define activities which can handle `org.ohmage.probes.ACTION_CONFIGURE` and `org.ohmage.probes.ACTION_VIEW_ANALYTICS`. They should also specify a mimeType of `probe/observerId`. `org.ohmage.probes.ACTION_CONFIGURE` will be called when the user tries to configure your probe through ohmage and `org.ohmage.probes.ACTION_VIEW_ANALYTICS` will be called when the user tries to view data from your probe through ohmage. It will probably look something like this:

    <activity android:name=".ConfigureActivity" android:label="@string/app_name">
        <intent-filter>
            <action android:name="org.ohmage.probes.ACTION_CONFIGURE" />
            <data android:mimeType="probe/edu.ucla.cens.Mobility" />
        </intent-filter>
    </activity>
    <activity android:name=".AnalyticsActivity" android:label="@string/app_name">
        <intent-filter>
            <action android:name="org.ohmage.probes.ACTION_VIEW_ANALYTICS" />
            <data android:mimeType="probe/edu.ucla.cens.Mobility" />
        </intent-filter>
    </activity>
