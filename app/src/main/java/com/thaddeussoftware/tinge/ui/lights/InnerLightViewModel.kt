package com.thaddeussoftware.tinge.ui.lights

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.view.View

abstract class InnerLightViewModel: ViewModel() {

    /**
     * True if full colour mode is supported, false if only temperature mode is supported
     * */
    abstract val isColorSupported: ObservableField<Boolean>

    /**
     * True if the light is in full colour mode,
     * False if it is in single colour mode,
     * Null if unknown or this is a group with different values
     * */
    abstract val isInColorMode: ObservableField<Boolean?>

    /**
     * Hue of the light from 0 - 1
     * Null if unknown or this is a group with different values
     * */
    abstract val hue: ObservableField<Float?>
    /**
     * Saturation of the light from 0 - 1
     * Null if unknown or this is a group with different values
     * */
    abstract val saturation: ObservableField<Float?>
    /**
     * Brightness of the light from 0 - 1
     * Null if unknown or this is a group with different values
     * */
    abstract val brightness: ObservableField<Float?>

    /**
     * White temperature of the light from 0 - 1
     * Null if unknown or this is a group with different values
     * */
    abstract val whiteTemperature: ObservableField<Float?>

    /**
     * Whether the expander button on the top right of the view currently indicates that the
     * view is expanded or not.
     * */
    abstract val isExpanded: ObservableField<Boolean>

    /**
     * Tint colour for the left preview image view
     * */
    abstract val colorForPreviewImageView: ObservableField<Int>

    /**
     * Background colour for the view
     * */
    abstract val colorForBackgroundView: ObservableField<Int>

    /**
     * Name to display
     * */
    abstract val displayName: ObservableField<String?>


    abstract fun onExpandContractButtonClicked(view: View)

    abstract fun onColorTabClicked(view: View)

    abstract fun onWhiteTabClicked(view: View)

    abstract fun onHueSliderChanged(newValue:Float)

    abstract fun onSaturationSliderChanged(newValue:Float)

    abstract fun onBrightnessSliderChanged(newValue:Float)

    abstract fun onWhiteSliderChanged(newValue: Float)
}