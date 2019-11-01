package com.thaddeussoftware.tinge.testHelpers

import androidx.databinding.ObservableField
import com.thaddeussoftware.tinge.tingeapi.generic.controller.ControllerInternalStageableProperty
import com.thaddeussoftware.tinge.tingeapi.generic.controller.HubController
import com.thaddeussoftware.tinge.tingeapi.generic.controller.LightController
import com.thaddeussoftware.tinge.tingeapi.generic.controller.LightGroupController

object ControllerTestHelpers {
    /**
     * @return
     * An empty [LightController] instance for testing.
     *
     * All values will be initially set to null.
     *
     * This is not backed by any logic (such as logic to set aggregate properties or change colour
     * modes), so test classes are responsible for setting all properties.
     * */
    fun getEmptyLightController(
            hubController: HubController = getEmptyHubController()
    ): LightController {
        return object: LightController {
            override val hubController = hubController
            override val isReachable = ObservableField<Boolean>()
            override val doesSupportColorMode = true
            override val doesSupportTemperatureMode = true
            override val lightId = ""
            override val displayName = ControllerInternalStageableProperty<String>()
            override val isInColorMode = ControllerInternalStageableProperty<Boolean>()
            override val isOn = ControllerInternalStageableProperty<Boolean?>()
            override val brightness = ControllerInternalStageableProperty<Float?>()
            override val hue = ControllerInternalStageableProperty<Float>()
            override val saturation = ControllerInternalStageableProperty<Float>()
            override val miredColorTemperature = ControllerInternalStageableProperty<Float>()
            override val colorTemperatureInSupportedRange = ControllerInternalStageableProperty<Float>()
        }
    }

    fun getEmptyHubController(): HubController {
        return object: HubController() {
            override val ipAddress = ""
            override val hubController: HubController
                get() = this
            override val parentLightGroupController: LightGroupController? = null
            override val lightsNotInSubgroup = ArrayList<LightController>()
            override val lightsInGroupOrSubgroups = ArrayList<LightController>()
            override val lightGroups = ArrayList<LightGroupController>()
            override val id = ""
            override val name = ControllerInternalStageableProperty<String?>("")
        }
    }
}