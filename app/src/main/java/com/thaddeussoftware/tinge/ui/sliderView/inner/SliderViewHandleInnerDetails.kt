package com.thaddeussoftware.tinge.ui.sliderView.inner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import com.thaddeussoftware.tinge.databinding.ViewSliderHandleBinding
import com.thaddeussoftware.tinge.ui.sliderView.SliderViewHandle

class SliderViewHandleInnerDetails(
        var sliderViewHandle: SliderViewHandle,
        context: Context,
        parentView: ViewGroup) {
    var sliderHandleView = ViewSliderHandleBinding.inflate(LayoutInflater.from(context), parentView, true)

    /**
     * Set to true if the view has been animated into the touch-down state (with the handle bigger
     * etc).
     *
     * This value changes when an animation begins to change the view into the touch-down or
     * touch-up state.
     *
     * Changed and queried by [SliderView.updateUiForWhetherTouchIsCurrentlyDown] only.
     * */
    var isUiInTouchDownState = false
}
