package com.thaddeussoftware.tinge.ui.sliderView.inner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import com.thaddeussoftware.tinge.databinding.ViewSliderGroupHandleBinding
import com.thaddeussoftware.tinge.helpers.UiHelper
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
        private val context: Context,
        parentView: ViewGroup
): SliderViewSingleOrGroupHandleDetails() {

    var view = ViewSliderGroupHandleBinding.inflate(LayoutInflater.from(context), parentView, true)

    var handlesInsideGroup = ArrayList<SliderViewSingleHandleDetails>()

    var bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888)
    var canvas = Canvas()

    /**
     * ViewOutlineProvider (for material design elevation shadows) that treats the outline
     * as a circle. Used for all handles - can be reused for multiple views.
     * */
    private val circleOutlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setOval(0, 0, view.width, view.height)
        }
    }

    init {
        canvas.setBitmap(bitmap)
        view.handleView.outlineProvider = circleOutlineProvider
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

    override fun updateHandleDrawableForCurrentColor() {
        val paint = Paint()
        paint.isAntiAlias = true
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        handlesInsideGroup.forEachIndexed { i, handleInnerDetails ->
            val startAngle = 360f * i.toFloat() / handlesInsideGroup.size
            val endAngle = 360f * (i.toFloat() + 1f) / handlesInsideGroup.size
            val midAngle = startAngle * 0.5 + endAngle * 0.5

            paint.color = handleInnerDetails.sliderViewHandle.color.get() ?: 0xff000000.toInt()

            canvas.drawArc(0f, 0f, width, height,
                    startAngle, endAngle-startAngle, true, paint)

        }

        paint.color = 0xdd_ffffff.toInt()
        canvas.drawCircle(width/2f, height/2f, width*0.7f/2f, paint)

        paint.color = 0xff_333333.toInt()
        paint.textSize = UiHelper.getPxFromDp(context, 11f)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("x${handlesInsideGroup.size}", width*0.5f, height*0.67f, paint)

        view.handleView.setBackgroundDrawable(BitmapDrawable(bitmap))
    }
}