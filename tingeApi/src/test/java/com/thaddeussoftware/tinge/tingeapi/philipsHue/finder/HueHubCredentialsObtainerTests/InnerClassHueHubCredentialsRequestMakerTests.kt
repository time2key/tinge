package com.thaddeussoftware.tinge.tingeapi.philipsHue.finder.HueHubCredentialsObtainerTests

import androidx.test.filters.MediumTest
import com.thaddeussoftware.tinge.tingeapi.philipsHue.finder.HueHubCredentialsObtainer
import junit.framework.Assert.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Created by thaddeusreason on 22/04/2018.
 */
class InnerClassHueHubCredentialsRequestMakerTests {

    lateinit var mockWebServer: MockWebServer
    lateinit var mockWebServer2: MockWebServer
    lateinit var requestMaker: HueHubCredentialsObtainer.HueHubCredentialsRequestMaker

    var wasCorrectMethodCalled = false

    @Before
    fun setupMockServer() {
        mockWebServer = MockWebServer()
        mockWebServer2 = MockWebServer()
        requestMaker = HueHubCredentialsObtainer.HueHubCredentialsRequestMaker()
        wasCorrectMethodCalled = false
    }

    @After
    fun shutdownMockServer() {
        try { mockWebServer.shutdown() } catch (e: Exception) { /*Server wasn't running*/ }
        try { mockWebServer2.shutdown() } catch (e: Exception) { /*Server wasn't running*/ }
    }

    @Test
    @MediumTest
    fun buttonNotPressed_onErrorCalled() {
        //Arrange:
        mockWebServer.enqueue(MockResponse().setResponseCode(101).setBody(""))
        mockWebServer.start()

        //Act & Assert:
        requestMaker.obtainUsernameTokenOrNullFrom(mockWebServer.url("").toString())!!.subscribe(
                {
                    fail("onSuccess() should not be called when button not pressed")
                },
                {
                    wasCorrectMethodCalled = true
                }
        )
        assert(wasCorrectMethodCalled)
    }

    @Test
    @MediumTest
    fun buttonPressed_usernameCredentialsReturned() {
        //Arrange:
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[{'success':{'username': 'AAA'}}]"))
        mockWebServer.start()

        //Act & Assert:
        requestMaker.obtainUsernameTokenOrNullFrom(mockWebServer.url("").toString())!!.subscribe(
                {
                    assertEquals("AAA", it)
                    wasCorrectMethodCalled = true
                },
                {
                    fail("onError() should not be called when button pressed")
                }
        )
        assert(wasCorrectMethodCalled)
    }

    @Test
    @MediumTest
    fun changeServer_correctServerContacted() {
        //Arrange:
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[{'success':{'username': 'AAA'}}]"))
        mockWebServer.start()
        mockWebServer2.enqueue(MockResponse().setResponseCode(200).setBody("[{'success':{'username': 'BBB'}}]"))
        mockWebServer2.start()

        //Act:
        requestMaker.obtainUsernameTokenOrNullFrom(mockWebServer.url("").toString())!!.subscribe()
        requestMaker.obtainUsernameTokenOrNullFrom(mockWebServer2.url("").toString())!!.subscribe(
                {
                    assertEquals("BBB", it)
                    wasCorrectMethodCalled = true
                },
                {
                    fail("onError() should not be called when button pressed")
                }
        )
        assert(wasCorrectMethodCalled)
    }
}