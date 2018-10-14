package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder.HueHubCredentialsObtainerTests

import android.support.test.filters.LargeTest
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder.HueHubCredentialsObtainer
import junit.framework.Assert.*
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class ObtainerTestsWithMockWebServer {
    private val TIME_TO_WAIT_MS: Long = 11_000

    lateinit var mockWebServer: MockWebServer
    lateinit var mockWebServer2: MockWebServer
    lateinit var obtainer: HueHubCredentialsObtainer

    var wasCorrectMethodCalled = false

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer2 = MockWebServer()
        obtainer = HueHubCredentialsObtainer()
        wasCorrectMethodCalled = false
    }

    @After
    fun tidyUp() {
        try { mockWebServer.shutdown() } catch (e: Exception) { /*Server wasn't running*/ }
        try { mockWebServer2.shutdown() } catch (e: Exception) { /*Server wasn't running*/ }
        obtainer.pause()
    }


    //region Tests asserting standard basic functionality (valid, invalid responses)

    @Test
    @LargeTest
    fun mockWebServer_noCredentialsReturnedByServer_onNextNotCalledOnObservable() {
        //Arrange:
        ObtainerTestHelpers.enqueueInvalidCredentialsResponse(mockWebServer)
        mockWebServer.start()

        obtainer.ipAddress = mockWebServer.url("").toString()
        obtainer.start()
        obtainer.credentialsObservable.subscribe {result ->
            fail("No valid credentials - subscribe should not be called")
        }

        //Act:
        obtainer.start()

        //Assert (wait to see that onNext is not called) :
        Thread.sleep(TIME_TO_WAIT_MS)
    }

    @Test
    @LargeTest
    fun mockWebServer_credentialsReturnedByServer_onNextCalledWithCredentials() {
        //Arrange:
        ObtainerTestHelpers.enqueueValidCredentialsResponse(mockWebServer, "AAA")
        mockWebServer.start()

        //Act:
        obtainer.ipAddress = mockWebServer.url("").toString()
        obtainer.credentialsObservable.subscribe {result ->
            assertEquals("AAA", result)
            wasCorrectMethodCalled = true
        }
        obtainer.start()

        //Assert:
        Thread.sleep(TIME_TO_WAIT_MS)
        assertTrue(wasCorrectMethodCalled)
    }

    @Test
    @LargeTest
    fun mockWebServer_credentialsNotReturnedFirstTimeThenReturnedSecondTimeByServer_onNextCalledWithCredentials() {
        //Arrange:
        ObtainerTestHelpers.enqueueInvalidCredentialsResponse(mockWebServer)
        ObtainerTestHelpers.enqueueValidCredentialsResponse(mockWebServer, "BBB")
        mockWebServer.start()

        //Act:
        obtainer.ipAddress = mockWebServer.url("").toString()
        obtainer.credentialsObservable.subscribe {result ->
            assertEquals("BBB", result)
            wasCorrectMethodCalled = true
        }
        obtainer.start()

        //Assert:
        Thread.sleep(TIME_TO_WAIT_MS * 2)
        assertTrue(wasCorrectMethodCalled)
    }

    //endregion


    //region Tests asserting fuctionality when class is used in unusual ways / has not been fully setup correctly

    /**
     * Asserts that no exception is thrown when credentials are returned by the server but there
     * is no observable.
     * */
    @Test
    @LargeTest
    fun mockWebServer_observableNotObserved_credentialsReturnedByServer_noException() {
        //Arrange:
        ObtainerTestHelpers.enqueueValidCredentialsResponse(mockWebServer, "AAA")
        mockWebServer.start()

        //Act:
        obtainer.ipAddress = mockWebServer.url("").toString()
        obtainer.start()

        //Assert (wait for server to return credentials, test will fail if exception thrown).
        Thread.sleep(TIME_TO_WAIT_MS)
    }

    /**
     * Asserts that no exception is thrown when ipAddress is null but start is called.
     * */
    @Test
    @LargeTest
    fun ipAddressNull_noException() {
        //Act:
        obtainer.start()

        //Assert (wait for network operation that is scheduled, test will fail if exception thrown).
        Thread.sleep(TIME_TO_WAIT_MS)
    }

    //endregion


    //region Tests asserting functionality under error conditions (no network, invalid json, etc)

    /**
     * Tests that when contacting an invalid server, this does not leave the
     * instance in an error state internally which means it cannot contact further servers.
     * */
    @Test
    @LargeTest
    fun mockWebServer_invalidIpContactedThenMockServerContacted_correctCredentialsReturned() {
        // Arrange:
        ObtainerTestHelpers.enqueueValidCredentialsResponse(mockWebServer, "FFF")
        mockWebServer.start()

        // Step 1 - make request with invalid ip address:

        // Step 1 Arrange:
        obtainer.ipAddress = ""
        obtainer.credentialsObservable.subscribe(
                { fail("onNext should not be called") },
                { fail("onError should not be called")}
        )

        // Step 1 Act:
        obtainer.start()

        // Step 1 Assert (wait for scheduled network operation, test will fail if exception thrown).
        Thread.sleep(TIME_TO_WAIT_MS)


        // Step 2 - make request with valid mock server:

        // Step 2 Arrange / Act:
        obtainer.ipAddress = mockWebServer.url("").toString()
        obtainer.credentialsObservable.subscribe {
            assertEquals("FFF", it)
            wasCorrectMethodCalled = true
        }

        // Step 3 Assert:
        Thread.sleep(TIME_TO_WAIT_MS)
        assertTrue(wasCorrectMethodCalled)
    }

    /**
     * Tests that when contacting an unreachable server, this does not leave the
     * instance in an error state internally which means it cannot contact further servers.
     * */
    @Test
    @LargeTest
    fun mockWebServer_notReachableIpContactedThenMockServerContacted_correctCredentialsReturned() {
        // Arrange:
        ObtainerTestHelpers.enqueueValidCredentialsResponse(mockWebServer, "FFF")
        mockWebServer.start()

        // Step 1 - make request with invalid ip address:

        // Step 1 Arrange:
        obtainer.ipAddress = "127.0.0.1"
        obtainer.credentialsObservable.subscribe(
                { fail("onNext should not be called") },
                { fail("onError should not be called")}
        )

        // Step 1 Act:
        obtainer.start()

        // Step 1 Assert (wait for scheduled network operation, test will fail if exception thrown).
        Thread.sleep(TIME_TO_WAIT_MS)


        // Step 2 - make request with valid mock server:

        // Step 2 Arrange / Act:
        obtainer.ipAddress = mockWebServer.url("").toString()
        obtainer.credentialsObservable.subscribe {
            assertEquals("FFF", it)
            wasCorrectMethodCalled = true
        }

        // Step 3 Assert:
        Thread.sleep(TIME_TO_WAIT_MS)
        assertTrue(wasCorrectMethodCalled)
    }

    /**
     * Tests that when contacting a server which responds with malformed json, this does not leave
     * the instance in an error state internally which means it will not behave correctly later.
     * */
    @Test
    @LargeTest
    fun mockWebServers_malformedResponseReturnedThenValidResponseReturned_correctCredentialsReturned() {
        // Arrange:
        ObtainerTestHelpers.enqueueMalformedResponse1(mockWebServer)
        ObtainerTestHelpers.enqueueValidCredentialsResponse(mockWebServer, "FFF")
        mockWebServer.start()
        obtainer.ipAddress = mockWebServer.url("").toString()
        obtainer.credentialsObservable.subscribe {
            assertEquals("FFF", it)
            wasCorrectMethodCalled = true
        }

        // Act:
        obtainer.start()
        Thread.sleep(TIME_TO_WAIT_MS * 2)

        // Assert:
        assertTrue(wasCorrectMethodCalled)
    }

    //endregion


    //region Tests asserting functionality when class is paused / resumed

    /**
     * Asserts that there is not background activity to be fetching requests and returning
     * credentials after pause() has been called, but that the background activity resumes
     * after start() has been called.
     * */
    @Test
    @LargeTest
    fun mockWebServer_validCredentialsEnqueuedOnServer_pause_wait_start_credentialsReturnedOnlyAfterOnResume() {
        //Arrange:
        ObtainerTestHelpers.enqueueValidCredentialsResponse(mockWebServer, "AAA")
        mockWebServer.start()

        //Act:

        //Step 1 - pause, fail test if onNext() is called on observable:
        obtainer.ipAddress = mockWebServer.url("").toString()
        val disposable = obtainer.credentialsObservable.subscribe {
            fail("onNext() with credentials should not be called while obtainer was paused")
        }
        obtainer.start()
        obtainer.pause()

        Thread.sleep(TIME_TO_WAIT_MS * 2)

        //Step 2 - resume, assert that onNext() is called on observable with correct result:
        disposable.dispose()
        obtainer.credentialsObservable.subscribe() {
            assertEquals("AAA", it)
            wasCorrectMethodCalled = true
        }
        obtainer.start()

        Thread.sleep(TIME_TO_WAIT_MS)

        assertTrue(wasCorrectMethodCalled)
    }

    /**
     * Tests that the obtainer is initially in the paused state when created / before start() is
     * called.
     * */
    @Test
    @LargeTest
    fun mockWebServer_validCredentialsEnqueued_notStarted_credentialsNotReturned() {
        //Arrange:
        ObtainerTestHelpers.enqueueValidCredentialsResponse(mockWebServer, "AAA")
        mockWebServer.start()

        //Act:
        obtainer.ipAddress = mockWebServer.url("").toString()
        obtainer.credentialsObservable.subscribe {
            fail("onNext() with credentials should not be called")
        }

        Thread.sleep(TIME_TO_WAIT_MS * 2)
    }

    //endregion


    //region Tests asserting functionality when ipAddress is changed

    @Test
    @LargeTest
    fun mockWebServers_switchServer_credentialsFromNewServerReturned() {
        //Arrange:
        ObtainerTestHelpers.enqueueInvalidCredentialsResponse(mockWebServer)
        mockWebServer.start()

        ObtainerTestHelpers.enqueueValidCredentialsResponse(mockWebServer2, "ZZZ")
        mockWebServer2.start()

        obtainer.credentialsObservable.subscribe {
            assertEquals("ZZZ", it)
            wasCorrectMethodCalled = true
        }

        //Act:
        obtainer.ipAddress = mockWebServer.url("").toString()
        obtainer.start()

        Thread.sleep(TIME_TO_WAIT_MS)

        obtainer.ipAddress = mockWebServer2.url("").toString()

        Thread.sleep(TIME_TO_WAIT_MS)

        assertTrue(wasCorrectMethodCalled)
    }

    //endregion
}