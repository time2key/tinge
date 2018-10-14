package com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller

import io.reactivex.Completable

/**
 * Represents a single hub of any type that can be controlled.
 *
 * Each supported hub type should have an implementation of this interface allowing that type
 * of hub to be controlled.
 *
 * Every implementation will have its values cached - accessing a property does not cause the
 * property to be reloaded, it reads the value in the cache.
 *
 *
 * **Usage:**
 *
 * Initially, some properties may not be known, so [refresh] should be called to load data from the
 * hub.
 *
 * Setting properties on this instance will stage changes locally, which can then be applied
 * with [applyHubChangesAndAllLightChanges] and similar methods, or discarded via
 * [discardHubChanges].
 *
 * Created by thaddeusreason on 14/01/2018.
 */
abstract class HubController {

    /**
     * The unique id of this hub.
     * */
    abstract val id: String

    /**
     * The ip address that this hub currently exists at.
     * */
    abstract val ipAddress: String

    /**
     * All of the lights connected to this hub.
     * */
    abstract val lights: List<LightController>




    /**
     * The user-configurable display name given to this hub.
     * */
    abstract val name: ControllerInternalStageableProperty<String?>




    /*
    fun refreshHub

    /**
     * This method sends all shelved changes made to all [lights] to the hub, but does not send
     * shelved changes made to this hub itself (such as updating the name).
     *
     * See [applyHubChangesAndAllLightChanges], [HubController] and [LightController] for more
     * information.
     * */
    fun applyAllLightChanges(): Completable

    /**
     * This method sends all shelved changes made to this instance only (such as e.g. name) to
     * the hub, but does not send shelved changes made to any of the [lights].
     *
     * See [applyHubChangesAndAllLightChanges], [HubController] for more information.
     * */
    fun applyHubChangesOnly(): Completable

    /**
     * This method sends all shelved changes made to this instance and any of the [lights] in this
     * instance.
     *
     * See [applyAllLightChanges], [applyHubChangesOnly] and [HubController] for more information.
     * */
    fun applyHubChangesAndAllLightChanges(): Completable

    /**
     * Discards all changes made to this hub instance only (such as e.g. changing the name).
     *
     * This does not discard changes made to [lights] etc - those must be discarded manually via
     * e.g. [LightController.discardChanges].
     *
     * See [applyHubChangesOnly] and [HubController] for more information.
     * */
    fun discardHubChanges()*/

    /**
     * Sends all shelved changes for the [DataInHubType]s specified to the hub.
     *
     * See [HubController] and [discardChanges].
     * */
    abstract fun applyChanges(vararg dataInHubTypes: DataInHubType): Completable

    /**
     * Discards all shelved changes for the [DataInHubType]s specified.
     *
     * See [HubController] and [applyChanges].
     * */
    open fun discardChanges(vararg dataInHubTypes: DataInHubType) {
        if (dataInHubTypes.contains(HubController.DataInHubType.HUB_CONFIG)) {
            name.discardStagedValue()
        }

        if (dataInHubTypes.contains(HubController.DataInHubType.LIGHTS)) {
            lights.forEach { it.discardChanges() }
        }
    }

    /**
     * Contacts the hub to update the properties relating to the [DataInHubType]s specified to
     * match the values returned from the hub.
     *
     * See [HubController].
     * */
    abstract fun refresh(vararg dataInHubType: DataInHubType): Completable




    /**
     * Represents a type of data in the hub that can be updated.
     *
     * This allows methods such as [applyChanges] to be called and only apply to particular types
     * of data, such as the current hub config but not lights associated with the hub.
     * */
    enum class DataInHubType {
        /**
         * The properties of all lights associated with this hub.
         * */
        LIGHTS,
        /**
         * The hub configuration itself, such as the name of the hub etc.
         * */
        HUB_CONFIG
    }

}