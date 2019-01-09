package com.thaddeussoftware.tinge.ui.sliderView.inner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thaddeussoftware.tinge.databinding.ViewSliderHandleBinding
import com.thaddeussoftware.tinge.ui.sliderView.SliderView
import com.thaddeussoftware.tinge.ui.sliderView.SliderViewHandle

class SliderViewSingleHandleDetails(
        var sliderViewHandle: SliderViewHandle,
        context: Context,
        parentView: ViewGroup): SliderViewSingleOrGroupHandleDetails() {

    var sliderHandleView = ViewSliderHandleBinding.inflate(LayoutInflater.from(context), parentView, true)

    /**
     * If this handle is part of a group multi-handle, this property will be set to the group,
     * otherwise null.
     *
     * See [SliderViewGroupHandleDetails] for more info.
     * */
    var groupHandleDetails: SliderViewGroupHandleDetails? = null


    override val handleView: View = sliderHandleView.handleView

    override val onMoveLabelTextView: View = sliderHandleView.onMoveLabelTextView

    override val rootView: View = sliderHandleView.root


    override val scaleIfTouchHeldDown: Float = 1.3f

    override val scaleIfHoveredOverForMerge: Float = 2f

    override val scaleIfHoveringOverForMerge: Float = 1f


    override fun getCurrentHandleValue(): Float? = sliderViewHandle.value.get()

    override fun setCurrentHandleValue(value: Float) {
        sliderViewHandle.value.set(value)
    }

}
