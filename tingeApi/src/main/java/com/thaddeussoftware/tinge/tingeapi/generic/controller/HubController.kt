package com.thaddeussoftware.tinge.tingeapi.generic.controller

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
abstract class HubController: LightGroupController() {

    /**
     * The ip address that this hub currently exists at.
     * */
    abstract val ipAddress: String

}