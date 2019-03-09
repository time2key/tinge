package com.thaddeussoftware.tinge.ui.lights

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.databinding.ObservableList
import android.view.View
import com.thaddeussoftware.tinge.ui.sliderView.SliderViewHandle

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
     * True if full color mode is supported by this light
     *
     * For groups, true will be returned if any of the lights in the group support full color mode.
     * */
    abstract val doesSupportColorMode: ObservableField<Boolean>

    /**
     * Hue of the light from 0 - 1
     * Null if unknown or this is a group with different values
     * */
    abstract val hueHandles: ObservableList<SliderViewHandle>
    /**
     * Saturation of the light from 0 - 1
     * Null if unknown or this is a group with different values
     * */
    abstract val saturationHandles: ObservableList<SliderViewHandle>
    /**
     * Brightness of the light from 0 - 1
     * Null if unknown or this is a group with different values
     * */
    abstract val brightnessHandles: ObservableList<SliderViewHandle>

    /**
     * White temperature of the light from 0 - 1
     * Null if unknown or this is a group with different values
     * */
    abstract val whiteTemperatureHandles: ObservableList<SliderViewHandle>

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

    abstract val secondaryInformation: ObservableField<String?>


    abstract fun onExpandContractButtonClicked(view: View)

    abstract fun onColorTabClicked(view: View)

    abstract fun onWhiteTabClicked(view: View)

}