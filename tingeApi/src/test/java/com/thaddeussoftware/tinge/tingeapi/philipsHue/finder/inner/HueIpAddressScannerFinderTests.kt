package com.thaddeussoftware.tinge.tingeapi.philipsHue.finder.inner

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before

class HueIpAddressScannerFinderTests {

    lateinit var mockWebServer: MockWebServer

    var numberOfTimesOnNextCalled = 0
    var wasOnErrorCalled = false
    var wasOnCompleteCalled = false

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        numberOfTimesOnNextCalled = 0
        wasOnErrorCalled = false
        wasOnCompleteCalled = false
    }

    @After
    fun tidyUp() {
        try { mockWebServer.shutdown() } catch (e: Exception) { /*Server wasn't running*/ }
    }



}