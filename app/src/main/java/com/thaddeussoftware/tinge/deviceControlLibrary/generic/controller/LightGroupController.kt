package com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller

import io.reactivex.Completable

/**
 * Represents a group of lights.
 *
 * This can be a group of lights as defined by the hub itself, such as rooms in a hue hub.
 * Alternatively, it can be a user assigned group of lights, completely independent of a hub.
 *
 * A light can be in multiple groups.
 * */
abstract class LightGroupController {

    /**
     * The HubController associated with this LightGroupController. This should also be the topmost
     * parent LightGroupController of this one.
     * If this LightGroupController is a HubController, this same instance should be returned.
     * */
    abstract val hubController: HubController

    /**
     * The direct parent light group controller of this one, or null if this light group controller
     * is the topmost one.
     * */
    abstract val parentLightGroupController: LightGroupController?

    /**
     * All of the lights directly inside this group.
     * See also [lightsInGroupOrSubgroups]
     * */
    abstract val lightsNotInSubgroup: List<LightController>

    /**
     * All of the lights inside this group, directly or otherwise.
     * See also [lightsNotInSubgroup]
     * */
    abstract val lightsInGroupOrSubgroups: List<LightController>

    /**
     * Light groups inside this one.
     * */
    abstract val lightGroups: List<LightGroupController>

    /**
     * The unique id of this group.
     * */
    abstract val id: String

    /**
     * The user-configurable display name given to this group.
     * */
    abstract val name: ControllerInternalStageableProperty<String?>


    var uniformIsOnOfAllLightsInGroupOrNull = ControllerInternalStageableProperty<Boolean?> { stagedValue ->
        lightsNotInSubgroup.forEach { lightController ->
            lightController.isOn.stagedValueObservable.set(stagedValue)
        }
    }

    /**
     * If all (reachable, on) [lightsNotInSubgroup] have the same brightness, property will be
     * brightness, otherwise null.
     * Setting this property sets all lights.
     * */
    val uniformBrightnessOfAllLightsInGroupOrNull = ControllerInternalStageableProperty<Float?> { stagedValue ->
        lightsNotInSubgroup.forEach { lightController ->
            lightController.brightness.stagedValueObservable.set(stagedValue)
        }
    }

    /**
     * If all (reachable, on) [lightsNotInSubgroup] have the same hue, property will be hue, otherwise
     * null.
     * Setting this properties sets all lights.
     * */
    val uniformHueOfAllLightsInGroupOrNull = ControllerInternalStageableProperty<Float?> { stagedValue ->
        lightsNotInSubgroup.forEach { lightController ->
            lightController.hue.stagedValueObservable.set(stagedValue)
        }
    }

    /**
     * If all (reachable, on) [lightsNotInSubgroup] have the same saturation, property will be sat,
     * otherwise null.
     * Setting this property sets all lights.
     * */
    val uniformSaturationOfAllLightsInGroupOrNull = ControllerInternalStageableProperty<Float?> { stagedValue ->
        lightsNotInSubgroup.forEach { lightController ->
            lightController.saturation.stagedValueObservable.set(stagedValue)
        }
    }

    /**
     * Average brightness of all (reachable, on) [lightsNotInSubgroup]
     * */
    val averageBrightnessOfAllLightsInGroup = ControllerInternalStageableProperty<Float?>()

    /**
     * Average hue of all (reachable, on) [lightsNotInSubgroup]
     * */
    val averageHueOfAllLightsInGroup = ControllerInternalStageableProperty<Float?>()

    /**
     * Average saturation of all (reachable, on) [lightsNotInSubgroup]
     * */
    val averageSaturationOfAllLightsInGroup = ControllerInternalStageableProperty<Float?>()


    /**
     * Sends all shelved changes for the [DataInGroupType]s specified to the group.
     *
     * See [LightGroupController] and [discardChanges].
     * */
    abstract fun applyChanges(vararg dataInGroupTypes: DataInGroupType): Completable

    /**
     * Discards all shelved changes for the [DataInGroupType]s specified.
     *
     * See [LightGroupController] and [applyChanges].
     * */
    open fun discardChanges(vararg dataInGroupTypes: DataInGroupType) {
        if (dataInGroupTypes.contains(DataInGroupType.GROUP_CONFIG)) {
            name.discardStagedValue()
        }

        if (dataInGroupTypes.contains(DataInGroupType.LIGHTS)) {
            lightsInGroupOrSubgroups.forEach { it.discardChanges() }
        }
    }

    /**
     * Contacts the hub to update the properties relating to the [DataInGroupType]s specified to
     * match the values returned for the group.
     *
     * See [LightGroupController].
     * */
    abstract fun refresh(vararg dataInGroupTypes: DataInGroupType): Completable




    /**
     * Represents a type of data in the group that can be updated.
     *
     * This allows methods such as [applyChanges] to be called and only apply to particular types
     * of data, such as the current group config but not lights associated with the group.
     * */
    enum class DataInGroupType {
        /**
         * The properties of all lights associated with this group.
         * */
        LIGHTS,
        /**
         * The group configuration itself, such as the name of the group etc.
         * */
        GROUP_CONFIG
    }
}
