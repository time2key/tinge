package com.thaddeussoftware.tinge.tingeapi.philipsHue.finder.inner

class HueBridgeXmlDeviceDescriptionTests {

    /*@Test
    @SmallTest
    fun validData_correctDataParsed() {
        // Arrange:
        val data = "<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\n" +
                "<specVersion>\n" +
                "<major>1</major>\n" +
                "<minor>0</minor>\n" +
                "</specVersion>\n" +
                "<URLBase>http://192.168.0.43:80/</URLBase>\n" +
                "<device>\n" +
                "<deviceType>urn:schemas-upnp-org:device:Basic:1</deviceType>\n" +
                "<friendlyName>Custom name (127.0.0.8)</friendlyName>\n" +
                "<manufacturer>Royal Philips Electronics</manufacturer>\n" +
                "<manufacturerURL>http://www.philips.com</manufacturerURL>\n" +
                "<modelDescription>Philips hue Personal Wireless Lighting</modelDescription>\n" +
                "<modelName>Philips hue bridge 2015</modelName>\n" +
                "<modelNumber>BSB002</modelNumber>\n" +
                "<modelURL>http://www.meethue.com</modelURL>\n" +
                "<serialNumber>SERIALNUMBER</serialNumber>\n" +
                "<UDN>udn</UDN>\n" +
                "<presentationURL>index.html</presentationURL>\n" +
                "<iconList>\n" +
                "<icon>\n" +
                "<mimetype>image/png</mimetype>\n" +
                "<height>48</height>\n" +
                "<width>48</width>\n" +
                "<depth>24</depth>\n" +
                "<url>hue_logo_0.png</url>\n" +
                "</icon>\n" +
                "</iconList>\n" +
                "</device>\n" +
                "</root>"

        // Act:
        val xmlDevice = HueBridgeXmlDeviceDescription("127.0.0.8", data)

        // Assert:
        assertEquals("127.0.0.8", xmlDevice.ipAddress)
        assertEquals("SERIALNUMBER", xmlDevice.serialNumber)
        assertEquals("Custom name", xmlDevice.hueHubName)
        assertEquals("Custom name (127.0.0.8)", xmlDevice.friendlyName)
        assertEquals("BSB002", xmlDevice.modelNumber)
        assertEquals(data, xmlDevice.responseBody)
    }*/
}