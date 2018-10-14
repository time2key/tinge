package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder.inner

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

/**
 * Xml device description of the device from /description.xml
 * */
class HueBridgeXmlDeviceDescription(val ipAddress: String, val responseBody: String) {

    /**
     * The friendly name of the hub as returned in the xml, in the format "name (ip address)" e.g.
     * "Philips Hue (192.168.0.1)"
     *
     * Use [hueHubName] instead, which removes the ip address suffix.
     * */
    var friendlyName: String? = null
        private set

    var modelNumber: String? = null
        private set

    var serialNumber: String? = null
        private set

    var uuid: String? = null
        private set

    /**
     * Gets the user-given name of this hub. This is not returned on its own in the xml, so this
     * is obtained by removing the ending " (ip address)" from [friendlyName].
     * */
    val hueHubName: String?
        get() = friendlyName?.replace(Regex(" ?[(][0-9.]*[)]"), "")

    init {
        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(responseBody.byteInputStream(), "UTF-8")

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {

                //Parse the friendlyName property:
                if (eventType.equals(XmlPullParser.START_TAG) && parser.name == "friendlyName") {
                    parser.next()
                    friendlyName = parser.text
                    continue
                }

                //Parse the modelNumber property:
                if (eventType.equals(XmlPullParser.START_TAG) && parser.name == "modelNumber") {
                    parser.next()
                    modelNumber = parser.text
                    continue
                }

                //Parse the serialNumber property:
                if (eventType.equals(XmlPullParser.START_TAG) && parser.name == "serialNumber") {
                    parser.next()
                    serialNumber = parser.text
                    continue
                }

                //Parse the uuid property:
                if (eventType.equals(XmlPullParser.START_TAG) && parser.name == "UDN") {
                    parser.next()
                    uuid = parser.text
                    continue
                }

                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            throw NotAValidHueDeviceException()
        }
        if (!responseBody.contains("Philips hue")) {
            throw NotAValidHueDeviceException()
        }
    }

    class NotAValidHueDeviceException: Exception() {

    }
}