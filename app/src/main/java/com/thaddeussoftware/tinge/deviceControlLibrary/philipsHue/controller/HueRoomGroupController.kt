package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller

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

        var meanBrightness = 0f
        var meanHue = 0f
        var meanSaturation = 0f

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

        lightListBackingProperty.forEach {
            meanBrightness += (it.brightness.lastValueRetrievedFromHub ?: 0f) / filteredLights.size
            meanHue += (it.hue.lastValueRetrievedFromHub ?: 0f) / filteredLights.size
            meanSaturation += (it.saturation.lastValueRetrievedFromHub ?: 0f) / filteredLights.size

            if (previousLightBrightness != it.brightness.lastValueRetrievedFromHub) {
                areAllLightsTheSameBrightness = false
            }
            if (previousLightHue != it.hue.lastValueRetrievedFromHub) {
                areAllLightsTheSameHue = false
            }
            if (previousLightSaturation != it.hue.lastValueRetrievedFromHub) {
                areAllLightsTheSameSaturation = false
            }

            previousLightBrightness = it.brightness.lastValueRetrievedFromHub
            previousLightHue = it.hue.lastValueRetrievedFromHub
            previousLightSaturation = it.saturation.lastValueRetrievedFromHub
        }

        uniformBrightnessOfAllLightsInGroupOrNull.setValueRetrievedFromHub(if (areAllLightsTheSameBrightness) meanBrightness else null)
        uniformHueOfAllLightsInGroupOrNull.setValueRetrievedFromHub(if (areAllLightsTheSameHue) meanHue else null)
        uniformSaturationOfAllLightsInGroupOrNull.setValueRetrievedFromHub(if (areAllLightsTheSameSaturation) meanSaturation else null)

        averageBrightnessOfAllLightsInGroup.setValueRetrievedFromHub(meanBrightness)
        averageHueOfAllLightsInGroup.setValueRetrievedFromHub(meanHue)
        averageSaturationOfAllLightsInGroup.setValueRetrievedFromHub(meanSaturation)
    }
}