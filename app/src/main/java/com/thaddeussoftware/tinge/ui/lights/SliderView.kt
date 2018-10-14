package com.thaddeussoftware.tinge.ui.lights

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.thaddeussoftware.tinge.databinding.ViewSliderBinding
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.ShapeDrawable
import com.thaddeussoftware.tinge.R
import android.databinding.BindingMethod
import android.databinding.BindingMethods
import android.support.constraint.ConstraintSet
import android.support.v4.graphics.ColorUtils
import android.view.MotionEvent


/**
 * This view is a custom implementation of SeekBar which modifies the appearance from the default
 * in order to provide a vertically thicker selectable track which can be recoloured, including
 * recoloured to multiple colours in a gradient like way
 *
 * This is used to provide the brightness, hue and saturation sliders for each light
 *
 * Created by thaddeusreason on 21/01/2018.
 */
@BindingMethods(BindingMethod(type = SliderView::class, attribute = "app:onSlideAmountChanged", method = "setOnSlideAmountChangedListener"))
class SliderView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0): FrameLayout(context, attrs, defStyle) {

    private val DEFAULT_TRACK_OPACITY = 0.65f

    var onSlideAmountChangedListener: SliderViewListener? = null

    var slidAmount = 0f
        set(value) {
            if (field != value) {
                field = value
                updateUIToCurrentSlidAmount()
            }
        }

    var trackOpacity = 1f
        set(value) {
            field = value
            binding.sliderTrackView.alpha = value
        }

    private var binding: ViewSliderBinding = ViewSliderBinding.inflate(LayoutInflater.from(context), this, true)
    private var handleAutoColors: IntArray? = null
    private var isHandleInAutoColorMode = false


    init {
        trackOpacity = DEFAULT_TRACK_OPACITY
    }

    /**Sets the track to a given multi-stop gradient*/
    fun setTrackToColors(vararg colors: Int) {
        val gd = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors)
        gd.cornerRadius = 0f

        binding.sliderTrackView.background = gd;
    }

    /**Sets the handle to auto update its color to its slid position on a given multi-stop gradient*/
    fun setHandleToAutoColors(vararg colors: Int) {
        isHandleInAutoColorMode = true
        handleAutoColors = colors
        innerSetHandleToColor(getCurrentAutoColor())
    }

    fun setHandleToSingleColor(color: Int) {
        isHandleInAutoColorMode = false
        innerSetHandleToColor(color)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        if (event?.actionMasked == MotionEvent.ACTION_CANCEL) return true

        val previousSlidAmount = slidAmount
        var xPercent = ((event?.x ?: 0f) - binding.sliderTrackView.x.toInt()) / binding.sliderTrackView.width.toFloat()
        xPercent = maxOf(minOf(xPercent, 1f), 0f)

        slidAmount = xPercent
        if (slidAmount != previousSlidAmount) {
            onSlideAmountChangedListener?.slideAmountChanged(xPercent)
        }

        return true
    }


    private fun getCurrentAutoColor(): Int {
        if (handleAutoColors == null) return 0
        val handleAutoColors = handleAutoColors!!

        var mergeColor1 = Math.floor(slidAmount.toDouble() * (handleAutoColors.size.toDouble()-1.0)).toInt()
        var mergeColor2 = Math.ceil(slidAmount.toDouble() * (handleAutoColors.size.toDouble()-1.0)).toInt()
        var mergeAmount = (slidAmount * (handleAutoColors.size.toFloat()-1f)) % 1f

        mergeColor1 = Math.min(mergeColor1, handleAutoColors.size - 1)
        mergeColor2 = Math.min(mergeColor2, handleAutoColors.size - 1)

        return ColorUtils.blendARGB(handleAutoColors[mergeColor1], handleAutoColors[mergeColor2], mergeAmount)
    }

    private fun innerSetHandleToColor(color: Int) {
        val size = resources.getDimension(R.dimen.slider_handle_diameter).toInt()

        val circleDrawable = ShapeDrawable(OvalShape())
        circleDrawable.intrinsicHeight = size
        circleDrawable.intrinsicWidth = size
        circleDrawable.paint.color = color

        binding.sliderHandleView.setBackgroundDrawable(circleDrawable)
    }

    private fun updateUIToCurrentSlidAmount() {
        if (slidAmount<0) {
            binding.sliderHandleView.visibility = GONE
        } else {
            binding.sliderHandleView.visibility = VISIBLE

            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.constraintLayout)
            constraintSet.setHorizontalBias(binding.sliderHandleView.id, slidAmount)
            constraintSet.applyTo(binding.constraintLayout)

            if (isHandleInAutoColorMode) {
                innerSetHandleToColor(getCurrentAutoColor())
            }
        }
    }


    interface SliderViewListener {
        fun slideAmountChanged(newAmount: Float)
    }
}
