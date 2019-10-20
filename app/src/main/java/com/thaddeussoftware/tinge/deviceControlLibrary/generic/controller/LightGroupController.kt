package com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller

import android.databinding.ObservableField
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

    /**
     * This is called when:
     * * A light is added / removed
     * * A subgroup is added / removed
     * */
    val onLightsOrSubgroupsAddedOrRemovedSingleLiveEvent = ObservableField<Any>()

    /**
     * This is called when absolutely anything changes related to this group, such as:
     * * A property of this group being modified (such as the name)
     * * A property of a subgroup being modified
     * * A property (such as the brightness) of a light in this group or subgroup being modified
     * * A light being added / removed, or a subgroup being added / removed
     * */
    val onAnythingModifiedSingleLiveEvent = ControllerLiveEvent()

    /**
     * This is called when a property (such as the brightness) of a light in this group or subgroup
     * is modified.
     * */
    val onLightPropertyModifiedSingleLiveEvent = ControllerLiveEvent()

}
