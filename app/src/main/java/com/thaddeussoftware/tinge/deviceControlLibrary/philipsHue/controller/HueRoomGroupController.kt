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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun refresh(vararg dataInGroupTypes: DataInGroupType): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    init {
        this.jsonRoom = jsonRoom
        this.lightsMap = lightsMap
        updateLightListForCurrentJsonRoomAndLightsMap()
        Log.v("tinge", "json room light count: "+jsonRoom.lightNumbersInBridge?.size)
        Log.v("tinge", "json map count: "+lightsMap.size)
        val i=0
    }
}