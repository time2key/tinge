package com.thaddeussoftware.tinge.ui.sliderView.inner

import android.content.Context
import androidx.databinding.Observable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thaddeussoftware.tinge.R
import com.thaddeussoftware.tinge.databinding.ViewSliderHandleBinding
import com.thaddeussoftware.tinge.ui.sliderView.SliderView
import com.thaddeussoftware.tinge.ui.sliderView.SliderViewHandle

class SliderViewSingleHandleDetails(
        var sliderViewHandle: SliderViewHandle,
        private val context: Context,
        parentView: ViewGroup): SliderViewSingleOrGroupHandleDetails() {

    var sliderHandleView = ViewSliderHandleBinding.inflate(LayoutInflater.from(context), parentView, true)

    /**
     * If this handle is part of a group multi-handle, this property will be set to the group,
     * otherwise null.
     *
     * See [SliderViewGroupHandleDetails] for more info.
     * */
    var groupHandleDetails: SliderViewGroupHandleDetails? = null

    /**
     * This will be registered with [SliderViewHandle.value], and is kept track of so it can
     * be unregistered when the handle is removed from the SliderView
     * */
    var valueObservablePropertyChangedCallback: Observable.OnPropertyChangedCallback? = null

    /**
     * This will be registered with [SliderViewHandle.color], and is kept track of so it can
     * be unregistered when the handle is removed from the SliderView
     * */
    var colorObservablePropertyChangedCallback: Observable.OnPropertyChangedCallback? = null


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

    override fun updateHandleDrawableForCurrentColor() {
        val size = context.resources.getDimension(R.dimen.slider_handle_diameter).toInt()
        val circleDrawable = ShapeDrawable(OvalShape())
        circleDrawable.intrinsicHeight = size
        circleDrawable.intrinsicWidth = size
        circleDrawable.paint.color = sliderViewHandle.color.get() ?: 0
        sliderHandleView.handleView.setBackgroundDrawable(circleDrawable)
    }

}
