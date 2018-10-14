package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller

import com.google.gson.Gson
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.ControllerInternalStageableProperty
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.HubController
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightController
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller.retrofitInterfaces.LightsRetrofitInterface
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.json.JsonLight
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

class HueLightController(
        override val hubController: HubController,
        private val lightNumberInHub: Int,
        jsonLight: JsonLight?,
        private val lightsRetrofitInterface: LightsRetrofitInterface,
        private val hubUsernameCredentials: String
) : LightController {

    private var backingIsReachable: Boolean = false
    override val isReachable: Boolean
        get() = backingIsReachable

    private var backingDoesSupportColorMode: Boolean = false
    override val doesSupportColorMode: Boolean
        get() = backingDoesSupportColorMode

    override val doesSupportTemperatureMode: Boolean
        get() = true

    private var backingLightId: String = ""
    override val lightId: String
        get() = backingLightId

    override val displayName: ControllerInternalStageableProperty<String> = ControllerInternalStageableProperty()

    override val isInColorMode: ControllerInternalStageableProperty<Boolean> = ControllerInternalStageableProperty()

    override val isOn: ControllerInternalStageableProperty<Boolean> = ControllerInternalStageableProperty()

    override val brightness: ControllerInternalStageableProperty<Float> = ControllerInternalStageableProperty()

    override val hue: ControllerInternalStageableProperty<Float> = ControllerInternalStageableProperty()

    override val saturation: ControllerInternalStageableProperty<Float> = ControllerInternalStageableProperty()

    override val miredColorTemperature: ControllerInternalStageableProperty<Float> = ControllerInternalStageableProperty()

    override val colorTemperatureInSupportedRange: ControllerInternalStageableProperty<Float> = ControllerInternalStageableProperty()


    init {
        backingIsReachable = jsonLight?.state?.reachable ?: false
        backingDoesSupportColorMode = true
        backingLightId = jsonLight?.uniqueId ?: ""

        displayName.setValueRetrievedFromHub(jsonLight?.name)
        isInColorMode.setValueRetrievedFromHub(
                jsonLight?.state?.colorMode == JsonLight.JsonState.JsonLightColorMode.HUE_AND_SATURATION)
        isOn.setValueRetrievedFromHub(jsonLight?.state?.on)
        brightness.setValueRetrievedFromHub(jsonLight?.state?.brightness?.div(254f))
        hue.setValueRetrievedFromHub((jsonLight?.state?.hue ?: 0) / 65535f)
        saturation.setValueRetrievedFromHub( (jsonLight?.state?.sat ?: 0) / 254f)
        // TODO color temperature properties
    }


    override fun applyChanges(): Completable {
        val jsonLightState = JsonLight.JsonState()
        jsonLightState.brightness = brightness.stagedValue?.times(254)?.toInt()

        if (isInColorMode.stagedValueOrLastValueFromHub == true) {
            //jsonLightState.colorMode = JsonLight.JsonState.JsonLightColorMode.HUE_AND_SATURATION
            jsonLightState.hue = hue.stagedValue?.times(65535f)?.toInt()
            jsonLightState.sat = saturation.stagedValue?.times(254f)?.toInt()
        } else if (isInColorMode.stagedValueOrLastValueFromHub == false) {
            // TODO
        }

        return lightsRetrofitInterface.updateLightState(hubUsernameCredentials, "$lightNumberInHub", jsonLightState)
                .subscribeOn(Schedulers.io())
                .map {
                    return@map it
                }.toCompletable()
    }

    override fun refresh(): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}