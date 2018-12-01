package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller

import android.graphics.Color
import android.support.v4.graphics.ColorUtils
import android.util.Log
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.ControllerInternalStageableProperty
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.HubController
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightController
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightGroupController
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller.json.JsonRoom
import io.reactivex.Completable

class HueRoomGroupController(
        override val hubController: HubController,
        /**
         * Map of lights in the format (Light number in hub -> HueLightController)
         * */
        lightsMap: Map<Int, HueLightController>,
        val groupNoInHub: Int,
        jsonRoom: JsonRoom
) : LightGroupController() {

    var jsonRoom = jsonRoom
    set(value) {
        field = value
        name.setValueRetrievedFromHub(jsonRoom.name)
        updateLightListForCurrentJsonRoomAndLightsMap()
    }

    var lightsMap = lightsMap
    set(value) {
        field = value
        updateLightListForCurrentJsonRoomAndLightsMap()
    }

    private fun updateLightListForCurrentJsonRoomAndLightsMap() {
        lightListBackingProperty.clear()
        jsonRoom.lightNumbersInBridge?.forEach {  lightNumberInBridge ->
            lightsMap.forEach { entry ->
                if (entry.key == lightNumberInBridge) {
                    lightListBackingProperty.add(entry.value)
                }
            }
        }
    }

    val lightListBackingProperty = ArrayList<LightController>()


    override val parentLightGroupController: LightGroupController?
        get() = hubController

    override val lightsNotInSubgroup: List<LightController>
        get() = lightListBackingProperty

    override val lightsInGroupOrSubgroups: List<LightController>
        get() = lightListBackingProperty

    override val lightGroups: List<LightGroupController> = ArrayList(0)

    override val id: String
        get() = hubController.id + "-" + groupNoInHub

    override val name: ControllerInternalStageableProperty<String?> = ControllerInternalStageableProperty(jsonRoom.name)

    override fun applyChanges(vararg dataInGroupTypes: DataInGroupType): Completable {
        //TODO consider replacing with single network call
        val completables = ArrayList<Completable>()
        lightsInGroupOrSubgroups.forEach {
           completables.add(it.applyChanges())
        }
        return Completable.mergeDelayError(completables)
                .doOnComplete {
                    uniformBrightnessOfAllLightsInGroupOrNull.discardStagedValue()
                    uniformHueOfAllLightsInGroupOrNull.discardStagedValue()
                    uniformSaturationOfAllLightsInGroupOrNull.discardStagedValue()
                }
    }

    override fun refresh(vararg dataInGroupTypes: DataInGroupType): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    init {
        this.jsonRoom = jsonRoom
        this.lightsMap = lightsMap
        hueNetworkRefreshHasHappened()
    }

    /**
     * Will be called by the [HueHubController] on this instance when the [HueHubController]
     * receives updated data from the server.
     *
     * Updates all uniform...OfAllLightsInGroupOrNull and all average...OfAllLightsInGroupOrNull
     * properties
     *
     * TODO ensure this updates if an individual light is updated
     * */
    fun hueNetworkRefreshHasHappened() {

        var meanRed = 0f
        var meanGreen = 0f
        var meanBlue = 0f

        var areAllLightsTheSameBrightness = true
        var areAllLightsTheSameHue = true
        var areAllLightsTheSameSaturation = true

        var previousLightBrightness: Float? = null
        var previousLightHue: Float? = null
        var previousLightSaturation: Float? = null

        val filteredLights = lightListBackingProperty.filter {
            it.isReachable
                    && it.isOn.lastValueRetrievedFromHub == true
                    && it.brightness.lastValueRetrievedFromHub != null
                    && it.hue.lastValueRetrievedFromHub != null
                    && it.saturation.lastValueRetrievedFromHub != null
        }

        filteredLights.forEach {
            val color = getColorFromHsv(it.hue.lastValueRetrievedFromHub ?: 0f,
                    it.saturation.lastValueRetrievedFromHub ?: 0f,
                    it.brightness.lastValueRetrievedFromHub ?: 0f)

            meanRed += Color.red(color) / filteredLights.size
            meanGreen += Color.green(color) / filteredLights.size
            meanBlue += Color.blue(color) / filteredLights.size


            if (previousLightBrightness != it.brightness.lastValueRetrievedFromHub
                    && previousLightBrightness != null) {
                areAllLightsTheSameBrightness = false
            }
            if (previousLightHue != it.hue.lastValueRetrievedFromHub
                    && previousLightHue != null) {
                areAllLightsTheSameHue = false
            }
            if (previousLightSaturation != it.saturation.lastValueRetrievedFromHub
                    && previousLightSaturation != null) {
                areAllLightsTheSameSaturation = false
            }

            previousLightBrightness = it.brightness.lastValueRetrievedFromHub
            previousLightHue = it.hue.lastValueRetrievedFromHub
            previousLightSaturation = it.saturation.lastValueRetrievedFromHub
        }

        uniformBrightnessOfAllLightsInGroupOrNull.setValueRetrievedFromHub(if (areAllLightsTheSameBrightness) previousLightBrightness else null)
        uniformHueOfAllLightsInGroupOrNull.setValueRetrievedFromHub(if (areAllLightsTheSameHue) previousLightHue else null)
        uniformSaturationOfAllLightsInGroupOrNull.setValueRetrievedFromHub(if (areAllLightsTheSameSaturation) previousLightSaturation else null)

        val hsv = FloatArray(3) {0f}
        Color.colorToHSV(Color.rgb(meanRed.toInt(), meanGreen.toInt(), meanBlue.toInt()), hsv)

        averageHueOfAllLightsInGroup.setValueRetrievedFromHub(hsv[0]/360f)
        averageSaturationOfAllLightsInGroup.setValueRetrievedFromHub(hsv[1])
        averageBrightnessOfAllLightsInGroup.setValueRetrievedFromHub(hsv[2])
    }

    private fun getColorFromHsv(h:Float, s:Float, v:Float) = Color.HSVToColor(floatArrayOf(h*360f, s, v))
}