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
         * See [HueRoomGroupController.lightsMap]
         * */
        lightsMap: Map<Int, HueLightController>,

        val groupNoInHub: Int,

        /**
         * See [HueRoomGroupController.jsonRoom]
         * */
        jsonRoom: JsonRoom
) : LightGroupController() {

    /**
     * Json data for this room - can be set to a new value and this instance will use the new value
     * to work out the new [lightsInGroupOrSubgroups] and [lightsNotInSubgroup].
     *
     * Note that [lightsMap] must be kept up to date when updating this value too.
     * */
    var jsonRoom = jsonRoom
    set(value) {
        field = value
        name.setValueRetrievedFromHub(jsonRoom.name)
        updateLightListForCurrentJsonRoomAndLightsMap()
    }

    /**
     * Map of lights in the format (Light number in hub -> HueLightController)
     *
     * [HueHubController] is responsible for updating this property every time the lights in it
     * change.
     * */
    var lightsMap = lightsMap
    set(value) {
        field = value
        updateLightListForCurrentJsonRoomAndLightsMap()
    }

    /**
     * Uses [jsonRoom] and [lightsMap] to calculate [lightListBackingProperty]
     * */
    private fun updateLightListForCurrentJsonRoomAndLightsMap() {
        var hasAnythingBeenAddedOrRemoved = false
        lightListBackingProperty.clear()
        jsonRoom.lightNumbersInBridge?.forEach {  lightNumberInBridge ->
            lightsMap.forEach { entry ->
                if (entry.key == lightNumberInBridge) {
                    lightListBackingProperty.add(entry.value)
                    hasAnythingBeenAddedOrRemoved = true
                }
            }
        }
        if (hasAnythingBeenAddedOrRemoved) {
            onLightsOrSubgroupsAddedOrRemovedSingleLiveEvent.notifyChange()
            onAnythingModifiedSingleLiveEvent.onEventHappenedOnHub()
        }
    }

    /**
     * List of LightControllers calculated to be in this Hue Room - used for [lightsNotInSubgroup]
     * and [lightsInGroupOrSubgroups]
     * */
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

    init {
        this.jsonRoom = jsonRoom
        this.lightsMap = lightsMap
    }

}