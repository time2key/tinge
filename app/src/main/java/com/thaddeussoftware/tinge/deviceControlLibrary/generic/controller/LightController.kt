package com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller

import android.databinding.ObservableField
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
    val isReachable: ObservableField<Boolean>

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
     *
     * Setting this property will cause [isInColorMode] to be set to true.
     * */
    val hue: ControllerInternalStageableProperty<Float>

    /**
     * Saturation from 0 to 1.
     * Only supported if [doesSupportColorMode] is true.
     *
     * Setting this property will cause [isInColorMode] to be set to true.
     * */
    val saturation: ControllerInternalStageableProperty<Float>

    /**
     * The current mired color temperature of this light.
     * Only supported if [doesSupportTemperatureMode] is true.
     *
     * Setting this property will cause [isInColorMode] to be set to false.
     * */
    val miredColorTemperature: ControllerInternalStageableProperty<Float>

    /**
     * The current color temperature of this light relative to the range of color temperatures
     * it supports, from 0 to 1.
     * Only supported if [doesSupportTemperatureMode] is true.
     * */
    val colorTemperatureInSupportedRange: ControllerInternalStageableProperty<Float>


}