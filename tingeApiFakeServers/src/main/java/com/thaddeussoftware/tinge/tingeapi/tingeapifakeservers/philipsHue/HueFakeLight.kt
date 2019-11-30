package com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.philipsHue

import com.thaddeussoftware.tinge.tingeapi.internalnetworkingclasses.philipsHue.json.JsonLight
import com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.generic.FakeLight
import com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.generic.FakeLightWishlist

internal class HueFakeLight(
        fakeLightWishlist: FakeLightWishlist
): FakeLight(fakeLightWishlist) {


    fun getJsonLight(): JsonLight {
        val jsonLight = JsonLight()

        jsonLight.name = displayName

        jsonLight.state = JsonLight.JsonState()
        jsonLight.state?.on = isOn
        jsonLight.state?.brightness = (brightness*254f).toInt()
        jsonLight.state?.hue = (hue*65535f).toInt()
        jsonLight.state?.sat = (saturation*254f).toInt()

        return jsonLight
    }
}