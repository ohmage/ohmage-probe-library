ohmageProbeLibrary
==================

The ohmageProbeLibrary is an Android Library apk which makes it easier to write a probe apk. The
Library consists of the ProbeWriter which helps handle connecting and disconnecting from the
ohmage service, as well as the ProbeBuilder and ResponseBuilder classes which help in creating
the objects to send to ohmage. Look at [ohmageProbeExample](https://github.com/cketcham/ohmageProbeExample)
as an example apk which uses this library.

ProbeWriter
-----------

The probe writer helps with the creation of probe data to be sent to ohmage. It has the methods
`connect()` and `disconnect()` which help connect to ohmage. The ProbeWriter is a wrapper around
the two aidl methods exposed by ohmage, `writeProbe` and `writeResponse`. These two methods are
handled by `write` for Probes, and `writeResponse` for Responses. The ProbeWriter can be used by
itself to write probe data to ohmage, or the two Builders can be used to help with the process of
formatting the data correctly.

ProbeBuilder
------------

The ProbeBuilder class makes it easy to create probe data. You can specify exactly the data you want
to send in the probe and easily send it. Use the builder to set the observer, stream, data and any
metadata that you would like to include based on the specification of the observer. Call `write`
and pass in the ProbeWriter object to send the data to ohmage.

ResponseBuilder
---------------

The ResponseBuilder works similarly to the ProbeBuilder except that it assists with the creation of
a response.