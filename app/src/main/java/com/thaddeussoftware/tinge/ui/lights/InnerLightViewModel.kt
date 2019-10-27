package com.thaddeussoftware.tinge.ui.lights

import androidx.lifecycle.ViewModel
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import android.view.View
import com.thaddeussoftware.tinge.ui.sliderView.SliderViewHandle
import com.thaddeussoftware.tinge.ui.lights.lightView.LightViewModel
import com.thaddeussoftware.tinge.ui.lights.groupView.GroupViewModel
import com.thaddeussoftware.tinge.R

/**
 * Represents a ViewModel that contains an inner light view (With a title, a brightness slider,
 * an expandable area containing hue and saturation sliders).
 *
 * This class exists in order to make large parts of shared behaviour from [LightViewModel] and
 * [GroupViewModel] into one common class.
 *
 * [R.layout.view_inner_light] can then bind directly to this ViewModel, whether it is a
 * [LightViewModel] or [GroupViewModel], meaning only one view implementation needs to exist.
 * */
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
     * [SliderViewHandle]s for Hues to show in Hue slider (from 0 - 1)
     * */
    abstract val hueHandles: ObservableList<SliderViewHandle>
    /**
     * [SliderViewHandle]s for Saturation values to show in Saturation slider (from 0 - 1)
     * */
    abstract val saturationHandles: ObservableList<SliderViewHandle>
    /**
     * [SliderViewHandle]s for Brightness values to show in Brightness slider (from 0 - 1)
     * Lights that are off should be set to -1.
     * */
    abstract val brightnessHandles: ObservableList<SliderViewHandle>

    /**
     * [SliderViewHandle]s for White temperatures to show in White temperature slider (from 0 - 1)
     * */
    abstract val whiteTemperatureHandles: ObservableList<SliderViewHandle>

    /**
     * Whether the expander button on the top right of the view currently indicates that the
     * view is expanded or not.
     * */
    abstract val isExpanded: ObservableField<Boolean>

    /**
     * Whether the top right expand/contract button should be visible.
     * */
    abstract val showTopRightExpandButton: ObservableField<Boolean>

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

    /**
     * This can be used to add a second line of information below [displayName]
     * */
    abstract val secondaryInformation: ObservableField<String?>

    /**
     * Whether this light / group is currently reachable or not.
     *
     * Groups should always return true if the Hub that the group is in is reachable by the app,
     * regardless of whether any of the lights in the group are reachable or not.
     * */
    abstract val isReachable: ObservableField<Boolean>


    abstract fun onExpandContractButtonClicked(view: View)

    abstract fun onColorTabClicked(view: View)

    abstract fun onWhiteTabClicked(view: View)

}