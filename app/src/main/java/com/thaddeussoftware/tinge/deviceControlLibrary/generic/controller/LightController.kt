package com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller

import io.reactivex.Completable

/**
 * Represents a single light of any type that can be controlled.
 *
 * Each supported hub type should have an implementation of this interface allowing that type
 * of light to be controlled.
 *
 *
 * **Usage:**
 *
 * Each instance of this will have all properties on it, such as brightness, color, etc, setup
 * upon creation to match the values returned from the hub.
 *
 * These properties can be changed. Changing these properties will cause their new values to be
 * staged locally in the instance. This allows you to set multiple values and then apply them
 * all at once, which in many cases means only one web call needs to be made to set multiple
 * values.
 *
 * To apply or discard these staged changes, call [applyChanges] or [discardChanges] respectively.
 *
 * To refresh this light, updating its values from the hub, call [refresh].
 *
 * */
interface LightController {

    /**
     * The [HubController] associated with this lightController
     * */
    val hubController: HubController

    /**
     * Whether this device is currently reachable by the hub or not
     * */
    val isReachable: Boolean

    /**
     * Indicates whether this device supports full color mode, which means that this light can
     * be set to any color in the spectrum (hue/saturation).
     *
     * Note that some devices may not support color mode but will still support color temperature
     * mode - see [doesSupportTemperatureMode].
     * */
    val doesSupportColorMode: Boolean

    /**
     * Indicates whether this device supports color temperature mode, which is where the color
     * temperature can be set between orangey white - bluish white.
     *
     * If [doesSupportColorMode] is true, this property must also be true.
     * */
    val doesSupportTemperatureMode: Boolean

    /**
     * Unique id identifying this light.
     * */
    val lightId: String




    /**
     * The user-configurable display name of this light.
     * */
    val displayName: ControllerInternalStageableProperty<String>

    /**
     * If true, this device is in color mode. Otherwise, this device is in temperature mode
     * (if supported).
     * */
    val isInColorMode: ControllerInternalStageableProperty<Boolean>

    /**
     * Whether this device is currently turned on or not.
     * */
    val isOn: ControllerInternalStageableProperty<Boolean?>

    /**
     * Brightness from 0 (least bright but still on) to 1 (most bright).
     * */
    val brightness: ControllerInternalStageableProperty<Float?>


    /**
     * Hue from 0 (red) cycling round to 1 (red).
     * Only supported if [doesSupportColorMode] is true.
     * */
    val hue: ControllerInternalStageableProperty<Float>

    /**
     * Saturation from 0 to 1.
     * Only supported if [doesSupportColorMode] is true.
     * */
    val saturation: ControllerInternalStageableProperty<Float>

    /**
     * The current mired color temperature of this light.
     * Only supported if [doesSupportTemperatureMode] is true.
     * */
    val miredColorTemperature: ControllerInternalStageableProperty<Float>

    /**
     * The current color temperature of this light relative to the range of color temperatures
     * it supports, from 0 to 1.
     * Only supported if [doesSupportTemperatureMode] is true.
     * */
    val colorTemperatureInSupportedRange: ControllerInternalStageableProperty<Float>




    /**
     * This method applies all staged changes, communicating with the hub to update the light.
     *
     * Note that if you are modifying multiple lights at once, for some hub types it can be more
     * efficient to call [HubController#applyAllLightChanges], as it may be possible to make one
     * call only that updates multiple lights at once.
     *
     * See [LightController] and [discardChanges] for more information.
     * */
    fun applyChanges(): Completable

    /**
     * This method discards all staged changes, reverting this instance back to the state that
     * matches the last returned state of the actual light from the api.
     *
     * See [LightController] and [applyChanges] for more information.
     * */
    fun discardChanges() {
        displayName.discardStagedValue()
        isInColorMode.discardStagedValue()
        isOn.discardStagedValue()
        brightness.discardStagedValue()
        hue.discardStagedValue()
        saturation.discardStagedValue()
        miredColorTemperature.discardStagedValue()
    }

    /**
     * Contacts the hub associated with this light to refresh the properties of this instance to
     * match the physical light.
     *
     * You should call [applyChanges] or [discardChanges] before calling this method. Calling this
     * method without applying or discarding changes will cause staged changes to be discarded.
     * */
    fun refresh(): Completable
}