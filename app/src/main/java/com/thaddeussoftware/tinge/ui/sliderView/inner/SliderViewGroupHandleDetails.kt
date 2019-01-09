package com.thaddeussoftware.tinge.ui.sliderView.inner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thaddeussoftware.tinge.databinding.ViewSliderGroupHandleBinding
import com.thaddeussoftware.tinge.ui.sliderView.SliderView

/**
 * A group of several handles displayed together into a single multi-handle in a [SliderView].
 *
 * If the handles have been merged together as a result of the user pulling them into each other
 * on this SliderView, they will all have the same value.
 *
 * If they have been merged together as a result of the values being updated in another way (e.g.
 * from the server, or from a different SliderView), they may be shown as a single merged
 * multi-handle but will actually have slightly different values to each other.
 *
 * */
class SliderViewGroupHandleDetails(
        context: Context,
        parentView: ViewGroup
): SliderViewSingleOrGroupHandleDetails() {

    var view = ViewSliderGroupHandleBinding.inflate(LayoutInflater.from(context), parentView, true)

    var handlesInsideGroup = ArrayList<SliderViewSingleHandleDetails>()

    var bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888)
    var canvas = Canvas()

    init {
        canvas.setBitmap(bitmap)
    }

    override val handleView: View = view.handleView

    override val onMoveLabelTextView: View = view.onMoveLabelTextView

    override val rootView: View = view.root


    override val scaleIfTouchHeldDown: Float = 1.2f

    override val scaleIfHoveredOverForMerge: Float = 1.4f

    override val scaleIfHoveringOverForMerge: Float = 0.9f


    override fun getCurrentHandleValue(): Float? = handlesInsideGroup.firstOrNull()?.getCurrentHandleValue()

    override fun setCurrentHandleValue(value: Float) {
        handlesInsideGroup.forEach {
            it.setCurrentHandleValue(value)
        }
    }
}