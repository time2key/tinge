package com.thaddeussoftware.tinge.ui.lights

import android.content.Context
import android.content.res.TypedArray
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
import android.view.View
import com.thaddeussoftware.tinge.helpers.UiHelper
import kotlin.math.absoluteValue


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

    /**
     * If set to true, this view will have an off value to the left of it, in addition to the
     * standard slider range. If the user sets the slider to the 'Off' position, [slidAmount]
     * will be set to -1.
     *
     * See also [offValueText]
     * */
    var supportsOffValue: Boolean? = null
        set(value) {
            field = value
            binding.supportsOffValue = value
        }

    /**
     * Text label shown below the slider view at the start position.
     * */
    var offValueText: String? = null
        set(value) {
            field = value
            binding.offTextView.visibility =
                    if (value.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.offTextView.text = value
        }

    /**
     * Listener that gets notified when [slidAmount] is changed
     * */
    var onSlideAmountChangedListener: SliderViewListener? = null

    /**
     * Text label shown below the slider view at the start position.
     * */
    var startValueText: String? = null
        set(value) {
            field = value
            binding.onLeftTextView.visibility =
                    if (value.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.onLeftTextView.text = value
        }

    /**
     * Text label shown below the slider view at the end position.
     * */
    var endValueText: String? = null
        set(value) {
            field = value
            binding.onRightTextView.visibility =
                    if (value.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.onRightTextView.text = value
        }

    /**
     * Amount the current slider is slid:
     * If the slider is in the normal range, this value will be from 0 - 1
     * If [supportsOffValue] is true, and the slider is in the 'Off' position, this value will be -1
     * If the slider is not set to a value, this value will be null
     * */
    var slidAmount: Float? = 0f
        set(value) {
            if (field != value) {
                field = value
                updateUIToCurrentSlidAmount()
            }
        }

    /**
     * Opacity of the background track
     * */
    var trackOpacity = 1f
        set(value) {
            field = value
            binding.sliderTrackView.alpha = value
            binding.sliderTrackViewStart.alpha = value
        }


    private var binding: ViewSliderBinding = ViewSliderBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     * Array of colours set using [setHandleToAutoColors] that the handle should change between
     * depending on [slidAmount]
     * */
    private var handleAutoColors: IntArray? = null


    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.SliderView)


        trackOpacity = DEFAULT_TRACK_OPACITY

        isClickable = true
        isFocusable = true
        supportsOffValue = attributes.getBoolean(R.styleable.SliderView_supportsOffValue, false)
        startValueText = attributes.getString(R.styleable.SliderView_startValueText)
        endValueText = attributes.getString(R.styleable.SliderView_endValueText)
        offValueText = attributes.getString(R.styleable.SliderView_offValueText)
        setTrackToColors(0xffaaaaaa.toInt(), 0xffaaaaaa.toInt())
        setHandleToAutoColors(0xffaaaaaa.toInt(), 0xffaaaaaa.toInt())
    }



    /**
     * Sets the track to a given multi-stop gradient. Does not affect the handle colour.
     * */
    fun setTrackToColors(vararg colors: Int) {

        val offColour = 0xff333333.toInt()
        val transparentColour = 0x33000000.toInt() or (colors[0] and 0xffffff).toInt()

        val startColors = ArrayList<Int>()
        //startColors.add(0x00000000.toInt())
        for (i in 0..10) {
            startColors.add(offColour)
            startColors.add(offColour)
        }
        for (i in 0..10) {
            startColors.add(transparentColour)
            startColors.add(transparentColour)
            startColors.add(transparentColour)
        }
        for (i in 0..10) {
            startColors.add(colors[0])
            startColors.add(colors[0])
        }

        val startGradient = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, startColors.toIntArray())
        startGradient.cornerRadius = 0f
        binding.sliderTrackViewStart.background = startGradient;

        val gd = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors)
        gd.cornerRadius = 0f
        binding.sliderTrackView.background = gd;
    }

    /**
     * Sets the handle to auto update its color to its slid position on a given multi-stop gradient.
     * Does not affect the track colour.
     * */
    fun setHandleToAutoColors(vararg colors: Int) {
        handleAutoColors = colors
        innerSetHandleToColor(getCurrentAutoColor())
    }



    /**
     * We keep track of the x position when the user touched down, for use in working out whether
     * to disable parent scrolling - see [onTouchEvent]
     * */
    private var touchDownX: Float? = null

    /**
     * How much the slider view needs to be moved to disable parent scrolling - see [onTouchEvent]
     * */
    private val AMOUNT_TO_MOVE_SLIDER_TO_DISABLE_PARENT_SCROLLING_DP = 8f;

    /**
     * Called by android when a touch happens.
     *
     * This SliderView will often be in a parent scrollview, and by default, if the touch
     * goes outside this view, the parent ScrollView will take the touch for scrolling instead.
     *
     * This is disabled here by calling requestDisallowInterceptTouchEvent if the user drags
     * the slider more than 20 pixels.
     *
     * When the touch goes up or is cancelled, requestDisallowInterceptTouchEvent is set back.
     * */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        if (event?.actionMasked == MotionEvent.ACTION_CANCEL) {
            requestDisallowInterceptTouchEvent(false)
            return true
        }
        if (event?.actionMasked == MotionEvent.ACTION_UP) {
            requestDisallowInterceptTouchEvent(false)
        }
        if (event?.actionMasked == MotionEvent.ACTION_DOWN) {
            touchDownX = event.x
        }
        if (event?.actionMasked == MotionEvent.ACTION_DOWN ||
                event?.actionMasked == MotionEvent.ACTION_MOVE) {

            // If has been moved enough in the x direction, disallow parent scrolling:

            if (touchDownX?.minus(event.x)?.absoluteValue ?: 0f
                    > UiHelper.getPxFromDp(context, AMOUNT_TO_MOVE_SLIDER_TO_DISABLE_PARENT_SCROLLING_DP)) {
                requestDisallowInterceptTouchEvent(true)
            }


            val previousSlidAmount = slidAmount
            var xPercent = 0f

            if (event.x < binding.sliderTrackViewStart.width*0.7f && supportsOffValue == true) {
                xPercent = -1f
            } else {
                xPercent = ((event.x
                        ?: 0f) - binding.sliderTrackView.x.toInt()) / binding.sliderTrackView.width.toFloat()
                xPercent = maxOf(minOf(xPercent, 1f), 0f)
            }

            slidAmount = xPercent
            if (slidAmount != previousSlidAmount) {
                onSlideAmountChangedListener?.slideAmountChanged(xPercent)
            }
        }
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) updateUIToCurrentSlidAmount()
    }



    private fun getCurrentAutoColor(): Int {
        if (handleAutoColors == null) return 0
        val handleAutoColors = handleAutoColors!!
        val slidAmount: Float = Math.min(Math.max(slidAmount ?: 0f, 0f), 1f)

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
        if (slidAmount == null) {
            binding.sliderHandleView.visibility = GONE
        } else {

            var horizontalBias = 0f

            val startWidth = if (supportsOffValue == true) binding.sliderTrackViewStart.width else 0
            val mainWidth = binding.sliderTrackView.width

            if (slidAmount ?: 0f < 0) {
                horizontalBias = startWidth * 0f / (startWidth + mainWidth)
            } else {
                horizontalBias = (startWidth + mainWidth * (slidAmount ?: 0f)) / (startWidth + mainWidth)
            }

            binding.sliderHandleView.visibility = VISIBLE

            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.constraintLayout)
            constraintSet.setHorizontalBias(binding.sliderHandleView.id, horizontalBias)
            constraintSet.applyTo(binding.constraintLayout)

            innerSetHandleToColor(getCurrentAutoColor())
        }
    }


    interface SliderViewListener {
        fun slideAmountChanged(newAmount: Float)
    }
}
