package com.thaddeussoftware.tinge.ui.lights

import android.animation.Animator
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

    private val ANIMATION_DURATION_MIN_MAX_LABELS_IN_MS = 400L
    private val ANIMATION_DURATION_MIN_MAX_LABELS_OUT_MS = 200L
    private val ANIMATION_DURATION_MIN_MAX_LABELS_OUT_SINGLE_CLICK_MS = 50L

    private val ANIMATION_DURATION_TOUCH_DOWN_MS = 100L
    private val ANIMATION_DURATION_TOUCH_UP_MS = 400L
    private val ANIMATION_HANDLE_SCALE_X_Y_TOUCH_DOWN = 1.3f

    private val ANIMATION_DURATION_TOUCH_DOWN_SINGLE_CLICK_MS = 100L
    private val ANIMATION_DURATION_TOUCH_UP_SINGLE_CLICK_MS = 400L
    private val ANIMATION_HANDLE_SCALE_X_Y_TOUCH_DOWN_SINGLE_CLICK = 1.3f


    private val AMOUNT_SLID_TO_HIDE_MAX_LABEL = 0.8f
    private val AMOUNT_SLID_TO_RESHOW_MAX_LABEL = 0.75f
    private val AMOUNT_SLID_TO_HIDE_MIN_LABEL = 0.2f
    private val AMOUNT_SLID_TO_RESHOW_MIN_LABEL = 0.25f

    private val DEFAULT_TRACK_OPACITY = 0.65f

    /**
     * How much the slider view needs to be moved to disable parent scrolling - see [onTouchEvent]
     * */
    private val AMOUNT_TO_MOVE_SLIDER_TO_DISABLE_PARENT_SCROLLING_DP = 8f;

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
     * The 'on move' text shows a label up below the handle when the slider handle is moved.
     *
     * This value determines the maximum value that will be shown in this text, e.g. 100 if the
     * on move text should show a percent symbol. This value will be formatted by
     * [onMoveTextStringFormat].
     * */
    var onMoveTextMaximumAmount: Float = 1f
        set(value) {
            field = value
        }

    /**
     * The 'on move' text shows a label up below the handle when the slider handle is moved.
     *
     * This value determines the format that the value will be shown in.
     * See also [onMoveTextMaximumAmount].
     * */
    var onMoveTextStringFormat: String = ""
        set(value) {
            field = value
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
     * We keep track of the x position when the user touched down, for use in working out
     * [hasBeenMovedEnoughInXDirectionToBeValidSide] - see [onTouchEvent]
     * */
    private var touchDownX: Float? = null

    /**
     * When this view is initially touched in [onTouchEvent], this value will be false. When the
     * touch has been moved [AMOUNT_TO_MOVE_SLIDER_TO_DISABLE_PARENT_SCROLLING_DP], this value
     * will be set to true, and parent scrolling will be disabled.
     *
     * Before this value has been set to true, this view will not respond to touches. This ensures
     * that if the user grabs a slider view and then scrolls up or down in the parent ScrollView,
     * the slider view value will not change as a result of this touch event.
     *
     * The exception to this is clicks - if the user presses down and then presses back up, without
     * a touch cancel event indicating the parent scrollview has consumed the touch, this view will
     * respond to the touch.
     * */
    private var hasBeenMovedEnoughInXDirectionToBeValidSide = false

    /**
     * Whether there is currently an active touch held down on this view.
     *
     * Updated in [onTouchEvent] only, read in multiple other places.
     * */
    private var isTouchCurrentlyDown: Boolean = false

    /**
     * Set to true if the view has been animated into the touch-down state (with the handle bigger
     * etc).
     *
     * This value changes when an animation begins to change the view into the touch-down or
     * touch-up state.
     *
     * Changed and queried by [updateUiForWhetherTouchIsCurrentlyDown] only.
     * */
    private var isUiInTouchDownState = false

    /**
     * Set to true when the max label has been made visible, set to false when the max label has
     * been made invisible to accomodate the on-move text.
     * */
    private var isMaxValueLabelAnimatedVisible = true
    /**
     * Set to true when the min label has been made visible, set to false when the min label has
     * been made invisible to accomodate the on-move text.
     * */
    private var isMinValueLabelAnimatedVisible = false

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

        onMoveTextMaximumAmount = attributes.getFloat(R.styleable.SliderView_onMoveTextMaximumAmount, 1f)
        onMoveTextStringFormat = attributes.getString(R.styleable.SliderView_onMoveTextStringFormat) ?: ""

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
            hasBeenMovedEnoughInXDirectionToBeValidSide = false
            isTouchCurrentlyDown = false
            return true
        }
        if (event?.actionMasked == MotionEvent.ACTION_UP) {
            requestDisallowInterceptTouchEvent(false)
            hasBeenMovedEnoughInXDirectionToBeValidSide = false
            isTouchCurrentlyDown = false

            moveSliderPositionToTouchEvent(event)
            if (!isUiInTouchDownState) {
                // UI is not in a touch down state - this indicates that this ACTION_UP event
                // is as a result of a quick click down and then up again, without moving the
                // slider around in between the down and up:
                animateInThenOutForSingleClick()
                return true
            }
        }
        if (event?.actionMasked == MotionEvent.ACTION_DOWN) {
            isTouchCurrentlyDown = true
            touchDownX = event.x
        }
        if (event?.actionMasked == MotionEvent.ACTION_DOWN ||
                event?.actionMasked == MotionEvent.ACTION_MOVE) {

            isTouchCurrentlyDown = true

            // If has been moved enough in the x direction, disallow parent scrolling:

            if (touchDownX?.minus(event.x)?.absoluteValue ?: 0f
                    > UiHelper.getPxFromDp(context, AMOUNT_TO_MOVE_SLIDER_TO_DISABLE_PARENT_SCROLLING_DP)) {
                requestDisallowInterceptTouchEvent(true)
                hasBeenMovedEnoughInXDirectionToBeValidSide = true
            }

            if (hasBeenMovedEnoughInXDirectionToBeValidSide) {
                moveSliderPositionToTouchEvent(event)
            } else {
                return true
            }
        }

        updateUiOfMaxAndMinLabelVisibility()
        updateUiForWhetherTouchIsCurrentlyDown()
        return true
    }

    /**
     * Starts animations to either turn the view into the touch-down state (with the bigger handle
     * etc) or the touch-up state (with the regular handle etc).
     *
     * Updates [isUiInTouchDownState].
     * */
    private fun updateUiForWhetherTouchIsCurrentlyDown() {
        if (!isUiInTouchDownState && isTouchCurrentlyDown && hasBeenMovedEnoughInXDirectionToBeValidSide) {
            // Animate to touch down state:
            isUiInTouchDownState = true

            binding.sliderHandleView.clearAnimation()
            binding.sliderHandleView
                    .animate()
                    .scaleX(ANIMATION_HANDLE_SCALE_X_Y_TOUCH_DOWN)
                    .scaleY(ANIMATION_HANDLE_SCALE_X_Y_TOUCH_DOWN)
                    .setDuration(ANIMATION_DURATION_TOUCH_DOWN_MS)
                    .setListener(null)

            binding.handleBelowLabel.clearAnimation()
            binding.handleBelowLabel
                    .animate()
                    .alpha(1f)
                    .setDuration(ANIMATION_DURATION_TOUCH_DOWN_MS)
                    .setListener(null)
        }
        else if (isUiInTouchDownState && !isTouchCurrentlyDown) {
            // Animate to touch up state:
            isUiInTouchDownState = false

            binding.sliderHandleView
                    .animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(ANIMATION_DURATION_TOUCH_UP_MS)
                    .setListener(null)

            binding.handleBelowLabel
                    .animate()
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION_TOUCH_UP_MS)
                    .setListener(null)
        }

    }

    /**
     * Called by [onTouchEvent] when the user single-clicks (touches down and then up without
     * moving the touch first).
     *
     * Performs an animation that animates the touch handle big and then small again.
     * */
    private fun animateInThenOutForSingleClick() {

        val firstAnimationDoneStartSecondAnimationListener = object: Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}

            override fun onAnimationCancel(p0: Animator?) {}

            override fun onAnimationStart(p0: Animator?) {}

            override fun onAnimationEnd(p0: Animator?) {

                binding.sliderHandleView
                        .animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(ANIMATION_DURATION_TOUCH_UP_SINGLE_CLICK_MS)
                        .setListener(null)

                binding.handleBelowLabel
                        .animate()
                        .alpha(0f)
                        .setDuration(ANIMATION_DURATION_TOUCH_UP_SINGLE_CLICK_MS)
                        .setListener(null)

                updateUiOfMaxAndMinLabelVisibility()

            }
        }

        binding.sliderHandleView.clearAnimation()
        binding.sliderHandleView
                .animate()
                .scaleX(ANIMATION_HANDLE_SCALE_X_Y_TOUCH_DOWN_SINGLE_CLICK)
                .scaleY(ANIMATION_HANDLE_SCALE_X_Y_TOUCH_DOWN_SINGLE_CLICK)
                .setDuration(ANIMATION_DURATION_TOUCH_DOWN_SINGLE_CLICK_MS)
                .setListener(firstAnimationDoneStartSecondAnimationListener)

        binding.handleBelowLabel.clearAnimation()
        binding.handleBelowLabel
                .animate()
                .alpha(1f)
                .setDuration(ANIMATION_DURATION_TOUCH_DOWN_SINGLE_CLICK_MS)
                .setListener(null)

        if (slidAmount ?: 0f > AMOUNT_SLID_TO_HIDE_MAX_LABEL) {
            isMaxValueLabelAnimatedVisible = false
            binding.onRightTextView
                    .animate()
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION_MIN_MAX_LABELS_OUT_SINGLE_CLICK_MS)
                    .setListener(null)
        }
        if (slidAmount ?: 1f < AMOUNT_SLID_TO_HIDE_MIN_LABEL) {
            isMinValueLabelAnimatedVisible = false
            binding.onLeftTextView
                    .animate()
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION_MIN_MAX_LABELS_OUT_SINGLE_CLICK_MS)
                    .setListener(null)
        }
    }

    /**
     * Sets the slider current slid position to the touch event and updates the UI accordingly.
     *
     * Called by [onTouchEvent] if it determines that the slider should be moved to the touch event.
     * */
    private fun moveSliderPositionToTouchEvent(event: MotionEvent) {
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
            binding.handleBelowLabel.text = ""
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

            binding.handleBelowLabel.text = String.format(onMoveTextStringFormat, slidAmount?.times(onMoveTextMaximumAmount))
        }
    }

    /**
     * Updates the visibility of the "Min" and "Max" labels, with animation.
     *
     * These are hidden if the slider is moved close to either edge, so that the value label below
     * the slider is not obscured by the Min/Max labels.
     * */
    private fun updateUiOfMaxAndMinLabelVisibility() {
        // Animate max label out:
        if (slidAmount ?: 0f > AMOUNT_SLID_TO_HIDE_MAX_LABEL && isMaxValueLabelAnimatedVisible && isTouchCurrentlyDown) {
            binding.onRightTextView
                    .animate()
                    .setDuration(ANIMATION_DURATION_MIN_MAX_LABELS_OUT_MS)
                    .alpha(0f)
                    .setListener(null)
            isMaxValueLabelAnimatedVisible = false
        }
        // Animate max label back in:
        else if (slidAmount ?: 0f < AMOUNT_SLID_TO_RESHOW_MAX_LABEL && !isMaxValueLabelAnimatedVisible || !isTouchCurrentlyDown) {
            binding.onRightTextView
                    .animate()
                    .setDuration(ANIMATION_DURATION_MIN_MAX_LABELS_IN_MS)
                    .alpha(1f)
                    .setListener(null)
            isMaxValueLabelAnimatedVisible = true
        }
        // Animate min label out:
        if (slidAmount ?: 0f < AMOUNT_SLID_TO_HIDE_MIN_LABEL && isMinValueLabelAnimatedVisible && isTouchCurrentlyDown) {
            binding.onLeftTextView
                    .animate()
                    .setDuration(ANIMATION_DURATION_MIN_MAX_LABELS_OUT_MS)
                    .alpha(0f)
                    .setListener(null)
            isMinValueLabelAnimatedVisible = false
        }
        // Animate min label back in:
        else if (slidAmount ?: 0f > AMOUNT_SLID_TO_RESHOW_MIN_LABEL && !isMinValueLabelAnimatedVisible || !isTouchCurrentlyDown) {
            binding.onLeftTextView
                    .animate()
                    .setDuration(ANIMATION_DURATION_MIN_MAX_LABELS_IN_MS)
                    .alpha(1f)
                    .setListener(null)
            isMinValueLabelAnimatedVisible = true
        }
    }


    interface SliderViewListener {
        fun slideAmountChanged(newAmount: Float)
    }
}
