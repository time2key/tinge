package com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.philipsHue.inner

import com.time2key.modularmockserver.DispatcherModule
import com.time2key.modularmockserver.ServerPath
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest


class HueFakeServerDispatcherModule(): DispatcherModule() {

    @ServerPath(".*i(a*)/.*i")
    fun hi(recordedRequest: RecordedRequest, captureGroup1: String): MockResponse? {
        return null
    }
}