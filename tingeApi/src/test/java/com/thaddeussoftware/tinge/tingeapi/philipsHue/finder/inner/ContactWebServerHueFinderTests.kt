package com.thaddeussoftware.tinge.tingeapi.philipsHue.finder.inner

import androidx.test.filters.MediumTest
import junit.framework.Assert.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class ContactWebServerHueFinderTests {

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

    @Test
    @MediumTest
    fun mockServer_hubsReturnedByServer_correctHubsReturnedByObservable() {
        // Arrange:
        mockWebServer.enqueue(MockResponse().setBody("[{\"id\":\"aaa111aaa111\",\"internalipaddress\":\"192.168.0.43\"}, {\"id\":\"bbb222bbb222\",\"internalipaddress\":\"192.168.0.1\"}]"))
        mockWebServer.start()
        val finder = ContactWebServerHueFinder(mockWebServer.url("").toString())

        // Act:
        finder.getHueBridgesFromServer().subscribe(
                { device ->
                    if (numberOfTimesOnNextCalled == 0) {
                        assertEquals("aaa111aaa111", device.id)
                        assertNull(device.name)
                        assertEquals("192.168.0.43", device.internalIpAddress)
                    } else {
                        assertEquals("bbb222bbb222", device.id)
                        assertNull(device.name)
                        assertEquals("192.168.0.1", device.internalIpAddress)
                    }
                    numberOfTimesOnNextCalled += 1
                },
                { error ->
                    fail("onError() should not be called")
                },
                {
                    wasOnCompleteCalled = true
                }
        )

        Thread.sleep(100)

        // Assert:
        assertEquals(2, numberOfTimesOnNextCalled)
        assertTrue(wasOnCompleteCalled)
    }

    @Test
    @MediumTest
    fun mockServer_noHubsReturnedByServer_noHubsReturnedByObservable() {
        // Arrange:
        mockWebServer.enqueue(MockResponse().setBody("[]"))
        mockWebServer.start()
        val finder = ContactWebServerHueFinder(mockWebServer.url("").toString())

        // Act:
        finder.getHueBridgesFromServer().subscribe(
                { device ->
                    fail("onNext() should not be called")
                },
                { error ->
                    fail("onError() should not be called")
                },
                {
                    wasOnCompleteCalled = true
                }
        )

        Thread.sleep(100)

        // Assert:
        assertTrue(wasOnCompleteCalled)
    }

    @Test
    @MediumTest
    fun couldNotConnectToServer_onErrorCalled() {
        // Arrange:
        Thread.sleep(200) // Wait for all fake servers to be shutdown
        val finder = ContactWebServerHueFinder("http://127.0.0.1")

        // Act:
        finder.getHueBridgesFromServer().subscribe(
                {
                    fail("onNext() should not be called")
                },
                {
                    wasOnErrorCalled = true
                },
                {
                    fail("onComplete() should not be called")
                }
        )

        Thread.sleep(100)

        // Assert:
        assertTrue(wasOnErrorCalled)
    }

    @Test
    @MediumTest
    fun mockServer_invalidJsonReturnedByServer_onErrorCalled() {
        // Arrange:
        mockWebServer.enqueue(MockResponse().setBody("[ [ INVALID JSON "))
        mockWebServer.start()
        val finder = ContactWebServerHueFinder(mockWebServer.url("").toString())

        // Act:
        finder.getHueBridgesFromServer().subscribe(
                { device ->
                    fail("onNext() should not be called")
                },
                { error ->
                    wasOnErrorCalled = true
                },
                {
                    fail("onError() should not be called")
                }
        )

        Thread.sleep(100)

        // Assert:
        assertTrue(wasOnErrorCalled)
    }


}