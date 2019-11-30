package com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.philipsHue.inner

import com.time2key.modularmockserver.DispatcherModule
import com.time2key.modularmockserver.ServerPath


class HueFakeServerDispatcherModule(): DispatcherModule() {

    @ServerPath(".*i")
    fun hi() {

    }
}