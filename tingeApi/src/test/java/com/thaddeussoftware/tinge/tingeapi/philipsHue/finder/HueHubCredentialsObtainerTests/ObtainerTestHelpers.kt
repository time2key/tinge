package com.thaddeussoftware.tinge.tingeapi.philipsHue.finder.HueHubCredentialsObtainerTests

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

object ObtainerTestHelpers {

    fun enqueueValidCredentialsResponse(
            mockWebServer: MockWebServer,
            username: String) {
        mockWebServer.enqueue(
                MockResponse()
                        .setResponseCode(200)
                        .setBody("[{'success':{'username': '$username'}}]"))
    }

    fun enqueueInvalidCredentialsResponse(
            mockWebServer: MockWebServer) {
        mockWebServer.enqueue(
                MockResponse()
                        .setResponseCode(101))
    }

    fun enqueueMalformedResponse1(
            mockWebServer: MockWebServer) {
        mockWebServer.enqueue(
                MockResponse()
                        .setBody(""))
    }


}
