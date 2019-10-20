package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller

import android.databinding.ObservableField
import android.util.Log
import com.google.gson.Gson
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.ControllerInternalStageableProperty
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightController
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller.retrofitInterfaces.LightsRetrofitInterface
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.json.JsonLight
import io.reactivex.schedulers.Schedulers

class HueLightController(
        override val hubController: HueHubController,
        private val lightNumberInHub: Int,
        jsonLight: JsonLight?,
        private val lightsRetrofitInterface: LightsRetrofitInterface,
        private val hubUsernameCredentials: String
) : LightController {

    var jsonLight: JsonLight? = jsonLight
    set(value) {
        field = value

        isReachable.set(jsonLight?.state?.reachable ?: false)
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

    override val isReachable: ObservableField<Boolean> = ObservableField(false)

    private var backingDoesSupportColorMode: Boolean = false
    override val doesSupportColorMode: Boolean
        get() = backingDoesSupportColorMode

    override val doesSupportTemperatureMode: Boolean
        get() = true

    private var backingLightId: String = ""
    override val lightId: String
        get() = backingLightId

    override val displayName: ControllerInternalStageableProperty<String> = ControllerInternalStageableProperty(
            onValueStaged = {
                hubController.onPropertyStagedFromLight(this)
            }
    )

    override val isInColorMode: ControllerInternalStageableProperty<Boolean> = ControllerInternalStageableProperty(
            onValueStaged = {
                hubController.onPropertyStagedFromLight(this)
            }
    )

    override val isOn: ControllerInternalStageableProperty<Boolean?> = ControllerInternalStageableProperty(
            onValueStaged = {
                hubController.onPropertyStagedFromLight(this)
            }
    )

    override val brightness: ControllerInternalStageableProperty<Float?> = ControllerInternalStageableProperty(
            onValueStaged = {
                hubController.onPropertyStagedFromLight(this)
            }
    )

    override val hue: ControllerInternalStageableProperty<Float> = ControllerInternalStageableProperty(
            onValueStaged = {
                isInColorMode.stageValue(true)
                // Staging a value for isInColorMode will cause onPropertyStagedFromLight to be
                // called on the hub
            }
    )

    override val saturation: ControllerInternalStageableProperty<Float> = ControllerInternalStageableProperty(
            onValueStaged = {
                isInColorMode.stageValue(true)
                // Staging a value for isInColorMode will cause onPropertyStagedFromLight to be
                // called on the hub
            }
    )

    override val miredColorTemperature: ControllerInternalStageableProperty<Float> = ControllerInternalStageableProperty(
            onValueStaged = {
                isInColorMode.stageValue(false)
                // Staging a value for isInColorMode will cause onPropertyStagedFromLight to be
                // called on the hub
            }
    )

    override val colorTemperatureInSupportedRange: ControllerInternalStageableProperty<Float> = ControllerInternalStageableProperty()


    init {
        this.jsonLight = jsonLight
    }

    fun completableForUpdatingLightAndNumberOfUpdatedProperties()
            : UpdateLightCompletableWithNumberOfZigbeeOperationsRequired {
        var numberOfPropertiesUpdated = 0

        var hueValueSetTo: Float? = null
        var satValueSetTo: Float? = null
        var miredColorTemperatureSetTo: Float? = null
        var isOnSetTo: Boolean? = null
        var brightnessSetTo: Float? = null

        val jsonLightState = JsonLight.JsonState()

        if (isInColorMode.stagedValue == true) {

        }
        if (isInColorMode.stagedValueOrLastValueFromHub == true
                && (hue.stagedValue != null || saturation.stagedValue != null)) {
            jsonLightState.hue = hue.stagedValue?.times(65535f)?.toInt()
            jsonLightState.sat = saturation.stagedValue?.times(254f)?.toInt()
            //if (isInColorMode.stagedValue == true) {
                jsonLightState.colorMode = JsonLight.JsonState.JsonLightColorMode.HUE_AND_SATURATION
            //}

            numberOfPropertiesUpdated += 1
            hueValueSetTo = hue.stagedValue
            satValueSetTo = saturation.stagedValue
        } else if (isInColorMode.stagedValueOrLastValueFromHub == false
                && (miredColorTemperature.stagedValue != null)) {
            jsonLightState.miredColorTemperature = miredColorTemperature.stagedValue?.toInt()
            //if (isInColorMode.stagedValue == false) {
                jsonLightState.colorMode = JsonLight.JsonState.JsonLightColorMode.COLOR_TEMPERATURE
            //}

            numberOfPropertiesUpdated += 1
            miredColorTemperatureSetTo = miredColorTemperature.stagedValue
        }


        if (isOn.stagedValue != null) {
            jsonLightState.on = isOn.stagedValue

            numberOfPropertiesUpdated += 1
            isOnSetTo = isOn.stagedValue
        }
        if (brightness.stagedValue != null) {
            jsonLightState.brightness = brightness.stagedValue?.times(254f)?.toInt()

            numberOfPropertiesUpdated += 1
            brightnessSetTo = brightness.stagedValue
        }

        Log.v("tinge", "updating light $lightNumberInHub with $numberOfPropertiesUpdated properties - ${Gson().toJson(jsonLightState)}")
        val completableForUpdatingLight =
                if (numberOfPropertiesUpdated == 0) null
                else lightsRetrofitInterface.updateLightState(
                        hubUsernameCredentials, "$lightNumberInHub", jsonLightState)
                        .subscribeOn(Schedulers.io())
                        .map {

                            if (isOnSetTo != null) {
                                isOn.setValueRetrievedFromHub(isOnSetTo)
                            }
                            if (brightnessSetTo != null) {
                                brightness.setValueRetrievedFromHub(brightnessSetTo)
                            }
                            if (hueValueSetTo != null) {
                                hue.setValueRetrievedFromHub(hueValueSetTo)
                                isInColorMode.setValueRetrievedFromHub(true)
                            }
                            if (satValueSetTo != null) {
                                saturation.setValueRetrievedFromHub(satValueSetTo)
                                isInColorMode.setValueRetrievedFromHub(true)
                            }
                            if (miredColorTemperatureSetTo != null) {
                                miredColorTemperature.setValueRetrievedFromHub(miredColorTemperatureSetTo)
                                isInColorMode.setValueRetrievedFromHub(false)
                            }


                            return@map it
                        }.toCompletable()

        return UpdateLightCompletableWithNumberOfZigbeeOperationsRequired(
                    completableForUpdatingLight, numberOfPropertiesUpdated)
    }

}