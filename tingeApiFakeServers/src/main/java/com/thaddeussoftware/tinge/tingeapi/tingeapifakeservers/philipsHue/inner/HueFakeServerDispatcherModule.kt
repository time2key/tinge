package com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.philipsHue.inner

import com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.modularMockServer.DispatcherModule
import com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.modularMockServer.ServerPath

class HueFakeServerDispatcherModule: DispatcherModule() {

    @ServerPath(".*i")
    fun hi() {

    }
}