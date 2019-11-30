package com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.philipsHue

import com.google.gson.Gson
import com.thaddeussoftware.tinge.tingeapi.internalnetworkingclasses.philipsHue.json.JsonLight
import com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.generic.FakeLight
import com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.generic.FakeLightWishlist
import com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.generic.FakeServer
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.lang.RuntimeException

class HueFakeServer: FakeServer() {

    private val fakeLights = ArrayList<HueFakeLight>()

    private val validUsernames = HashSet<String>()

    private var mockWebServer: MockWebServer? = null

    override var isServerStarted: Boolean = false
        private set

    private val mockWebServerDispatcher = object: Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            val authenticatedCallRegex = Regex("^api/([a-zA-Z0-9])*/(.*)$")
            val authenticatedCallMatchResult = authenticatedCallRegex.find(request.path)
            if (authenticatedCallMatchResult != null) {
                val username = authenticatedCallMatchResult.groupValues[1]
                val apiCall = authenticatedCallMatchResult.groupValues[2]
                val isUsernameValid = validUsernames.contains(username)

                if (apiCall == "/lights") {
                    val responseMap = HashMap<Int, JsonLight>()
                    fakeLights.forEachIndexed { index, fakeLight ->
                        responseMap[index] = fakeLight.getJsonLight()
                    }
                    val responseString = Gson().toJson(responseMap)
                    val response = MockResponse()
                    response.setBody(responseString)
                    return response
                }


            }

            throw RuntimeException("Unknown call \"${request.path}\" made")
        }
    }

    override fun addFakeLightToServer(fakeLightWishlist: FakeLightWishlist): FakeLight {
        val fakeLight = HueFakeLight(fakeLightWishlist)
        fakeLights.add(fakeLight)
        return fakeLight
    }

    fun startServer() {
        mockWebServer = MockWebServer()
        mockWebServer!!.setDispatcher(mockWebServerDispatcher)
    }
}