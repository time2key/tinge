package com.thaddeussoftware.tinge.ui.sliderView

import android.animation.Animator
import android.content.Context
import android.databinding.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.thaddeussoftware.tinge.databinding.ViewSliderBinding
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.ShapeDrawable
import android.os.Looper
import android.util.Log
import com.thaddeussoftware.tinge.R
import android.view.MotionEvent
import android.view.View
import com.thaddeussoftware.tinge.helpers.UiHelper
import com.thaddeussoftware.tinge.ui.sliderView.inner.SimplifiedSetOnObservableListChangedCallback
import com.thaddeussoftware.tinge.ui.sliderView.inner.SliderViewHandleInnerDetails
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
@BindingMethods(BindingMethod(type = SliderView::class, attribute = "app:handles", method = "setHandles"))
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
     * Opacity of the background track
     * */
    var trackOpacity = 1f
        set(value) {
            field = value
            binding.sliderTrackView.alpha = value
            binding.sliderTrackViewStart.alpha = value
        }

    var handles: ObservableList<SliderViewHandle>? = null
        set(value) {
            field = value
            value?.addOnListChangedCallback(object: SimplifiedSetOnObservableListChangedCallback<SliderViewHandle>() {
                override fun listModified(itemsAdded: Collection<SliderViewHandle>, itemsRemoved: Collection<SliderViewHandle>) {
                    itemsAdded.forEach {
                        addSliderViewHandle(it)
                    }
                }
            })
            value?.forEach {
                addSliderViewHandle(it)
            }
        }

    private fun addSliderViewHandle(sliderViewHandle: SliderViewHandle) {
        handleDetailsMap[sliderViewHandle] = SliderViewHandleInnerDetails(sliderViewHandle, context, binding.frameLayout)
        updateUiOfHandleToMatchCurrentPosition(sliderViewHandle, handleDetailsMap[sliderViewHandle]!!)
        updateHandleColor(sliderViewHandle, handleDetailsMap[sliderViewHandle]!!)

        sliderViewHandle.value.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (Thread.currentThread() == Looper.getMainLooper().thread) {
                    Log.v("tinge", "SliderView value updated to ${sliderViewHandle.value.get()} - updating UI ("+handles?.size+" handles total)")
                    updateUiOfHandleToMatchCurrentPosition(sliderViewHandle, handleDetailsMap[sliderViewHandle]!!)
                } else {
                    Log.v("tinge", "SliderView value updated to ${sliderViewHandle.value.get()} - updating UI ("+handles?.size+" handles total (not from ui thread))")
                    post {
                        updateUiOfHandleToMatchCurrentPosition(sliderViewHandle, handleDetailsMap[sliderViewHandle]!!)
                    }
                }
            }
        })
        sliderViewHandle.color.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (Thread.currentThread() == Looper.getMainLooper().thread) {
                    updateHandleColor(sliderViewHandle, handleDetailsMap[sliderViewHandle]!!)
                } else {
                    Log.v("tinge", "SliderView color updated - not from ui thread")
                    post {
                        updateHandleColor(sliderViewHandle, handleDetailsMap[sliderViewHandle]!!)
                    }
                }
            }
        })
    }

    private val handleDetailsMap = HashMap<SliderViewHandle, SliderViewHandleInnerDetails>()

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
     * Set to true when the max label has been made visible, set to false when the max label has
     * been made invisible to accomodate the on-move text.
     * */
    private var isMaxValueLabelAnimatedVisible = true
    /**
     * Set to true when the min label has been made visible, set to false when the min label has
     * been made invisible to accomodate the on-move text.
     * */
    private var isMinValueLabelAnimatedVisible = false

    private val DP_TOLERANCE_TO_MERGE_HANDLES = 4f

    private val DP_TOLERANCE_TO_SELECT_HANDLE = 16f

    private var currentlyHeldSliderViewHandleDetails: SliderViewHandleInnerDetails? = null

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

            if (currentlyHeldSliderViewHandleDetails != null) {
                moveSingleHandleToTouchEvent(currentlyHeldSliderViewHandleDetails!!, event)
            } else {
                moveAllSlidersAroundTouchEvent(event)
            }

            requestDisallowInterceptTouchEvent(false)
            hasBeenMovedEnoughInXDirectionToBeValidSide = false
            isTouchCurrentlyDown = false
            currentlyHeldSliderViewHandleDetails = null

            var isAnyHandleInTouchDownState = false
            handleDetailsMap.values.forEach {
                if (it.isUiInTouchDownState) isAnyHandleInTouchDownState = true
            }
            if (!isAnyHandleInTouchDownState) {
                // no handles are in a touch down state - this indicates that this ACTION_UP event
                // is as a result of a quick click down and then up again, without moving the
                // slider around in between the down and up:
                if (currentlyHeldSliderViewHandleDetails != null) {
                    animateInThenOutForSingleClick(currentlyHeldSliderViewHandleDetails!!)
                } else {
                    handleDetailsMap.values.forEach {
                        animateInThenOutForSingleClick(it)
                    }
                }
                return true
            }
        }
        if (event?.actionMasked == MotionEvent.ACTION_DOWN) {
            isTouchCurrentlyDown = true
            touchDownX = event.x

            val slidAmoutnOfTouchXMinusTolerance = getSlidAmountFromTouchEventXPosition(event.x - UiHelper.getPxFromDp(context, DP_TOLERANCE_TO_SELECT_HANDLE))
            val slidAmoutnOfTouchXPlusTolerance = getSlidAmountFromTouchEventXPosition(event.x + UiHelper.getPxFromDp(context, DP_TOLERANCE_TO_SELECT_HANDLE))

            handleDetailsMap.values.forEach {
                val handleValue = it.sliderViewHandle.value.get() ?: 0f

                if (slidAmoutnOfTouchXMinusTolerance < handleValue
                        && slidAmoutnOfTouchXPlusTolerance > handleValue) {
                    currentlyHeldSliderViewHandleDetails = it
                }
            }
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
                if (currentlyHeldSliderViewHandleDetails != null) {
                    moveSingleHandleToTouchEvent(currentlyHeldSliderViewHandleDetails!!, event)
                } else {
                    moveAllSlidersAroundTouchEvent(event)
                }
            } else {
                return true
            }
        }

        updateUiOfMaxAndMinLabelVisibility()
        updateAnimationsForWhetherTouchIsCurrentlyDown()

        return true

    }

    /**
     * Starts animations to either turn the view into the touch-down state (with the bigger handle
     * etc) or the touch-up state (with the regular handle etc).
     *
     * Updates [isUiInTouchDownState].
     * */
    private fun updateAnimationsForWhetherTouchIsCurrentlyDown() {

        handleDetailsMap.forEach { entry ->

            if (!entry.value.isUiInTouchDownState
                    && isTouchCurrentlyDown
                    && hasBeenMovedEnoughInXDirectionToBeValidSide
                    && (currentlyHeldSliderViewHandleDetails == null
                            || currentlyHeldSliderViewHandleDetails == entry.value)) {
                // Animate to touch down state:
                entry.value.isUiInTouchDownState = true

                entry.value.sliderHandleView.handleView.clearAnimation()
                entry.value.sliderHandleView.handleView
                        .animate()
                        .scaleX(ANIMATION_HANDLE_SCALE_X_Y_TOUCH_DOWN)
                        .scaleY(ANIMATION_HANDLE_SCALE_X_Y_TOUCH_DOWN)
                        .setDuration(ANIMATION_DURATION_TOUCH_DOWN_MS)
                        .setListener(null)

                entry.value.sliderHandleView.onMoveLabelTextView.clearAnimation()
                entry.value.sliderHandleView.onMoveLabelTextView
                        .animate()
                        .alpha(1f)
                        .setDuration(ANIMATION_DURATION_TOUCH_DOWN_MS)
                        .setListener(null)
            }
            else if (entry.value.isUiInTouchDownState && !isTouchCurrentlyDown) {
                // Animate to touch up state:
                entry.value.isUiInTouchDownState = false

                entry.value.sliderHandleView.handleView
                        .animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(ANIMATION_DURATION_TOUCH_UP_MS)
                        .setListener(null)

                entry.value.sliderHandleView.onMoveLabelTextView
                        .animate()
                        .alpha(0f)
                        .setDuration(ANIMATION_DURATION_TOUCH_UP_MS)
                        .setListener(null)
            }
        }

    }

    /**
     * Called by [onTouchEvent] when the user single-clicks (touches down and then up without
     * moving the touch first).
     *
     * Performs an animation that animates the touch handle big and then small again.
     * */
    private fun animateInThenOutForSingleClick(hueDetails: SliderViewHandleInnerDetails) {
        val firstAnimationDoneStartSecondAnimationListener = object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}

            override fun onAnimationCancel(p0: Animator?) {}

            override fun onAnimationStart(p0: Animator?) {}

            override fun onAnimationEnd(p0: Animator?) {

                hueDetails.sliderHandleView.handleView
                        .animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(ANIMATION_DURATION_TOUCH_UP_SINGLE_CLICK_MS)
                        .setListener(null)

                hueDetails.sliderHandleView.onMoveLabelTextView
                        .animate()
                        .alpha(0f)
                        .setDuration(ANIMATION_DURATION_TOUCH_UP_SINGLE_CLICK_MS)
                        .setListener(null)

                updateUiOfMaxAndMinLabelVisibility()

            }
        }

        hueDetails.sliderHandleView.handleView.clearAnimation()
        hueDetails.sliderHandleView.handleView
                .animate()
                .scaleX(ANIMATION_HANDLE_SCALE_X_Y_TOUCH_DOWN_SINGLE_CLICK)
                .scaleY(ANIMATION_HANDLE_SCALE_X_Y_TOUCH_DOWN_SINGLE_CLICK)
                .setDuration(ANIMATION_DURATION_TOUCH_DOWN_SINGLE_CLICK_MS)
                .setListener(firstAnimationDoneStartSecondAnimationListener)

        hueDetails.sliderHandleView.onMoveLabelTextView.clearAnimation()
        hueDetails.sliderHandleView.onMoveLabelTextView
                .animate()
                .alpha(1f)
                .setDuration(ANIMATION_DURATION_TOUCH_DOWN_SINGLE_CLICK_MS)
                .setListener(null)

        if (hueDetails.sliderViewHandle.value.get() ?: 0f > AMOUNT_SLID_TO_HIDE_MAX_LABEL) {
            isMaxValueLabelAnimatedVisible = false
            binding.onRightTextView
                    .animate()
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION_MIN_MAX_LABELS_OUT_SINGLE_CLICK_MS)
                    .setListener(null)
        }
        if (hueDetails.sliderViewHandle.value.get() ?: 1f < AMOUNT_SLID_TO_HIDE_MIN_LABEL) {
            isMinValueLabelAnimatedVisible = false
            binding.onLeftTextView
                    .animate()
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION_MIN_MAX_LABELS_OUT_SINGLE_CLICK_MS)
                    .setListener(null)
        }
    }

    private fun getSlidAmountFromTouchEventXPosition(eventX: Float): Float {
        var newSlidAmount = 0f

        if (eventX < binding.sliderTrackViewStart.width*0.7f && supportsOffValue == true) {
            newSlidAmount = -1f
        } else {
            newSlidAmount = ((eventX
                    ?: 0f) - binding.sliderTrackView.x.toInt()) / binding.sliderTrackView.width.toFloat()
            newSlidAmount = maxOf(minOf(newSlidAmount, 1f), 0f)
        }
        return newSlidAmount
    }

    /**
     * Move a single handle so it is at a given touch event.
     * */
    private fun moveSingleHandleToTouchEvent(sliderViewHandleInnerDetails: SliderViewHandleInnerDetails, event: MotionEvent) {
        val newSlidAmount = getSlidAmountFromTouchEventXPosition(event.x)
        Log.v("tinge", "----------------")
        Log.v("tinge", "SliderView single handle moved to position: "+newSlidAmount+ " ("+handleDetailsMap.size+" handles total)")
        sliderViewHandleInnerDetails.sliderViewHandle.value.set(newSlidAmount)
        updateUiOfHandleToMatchCurrentPosition(sliderViewHandleInnerDetails.sliderViewHandle, sliderViewHandleInnerDetails)
    }

    /**
     * Moves all the handles so they are around the given touch event.
     *
     * Called by [onTouchEvent]
     * */
    private fun moveAllSlidersAroundTouchEvent(event: MotionEvent) {
        //val previousSlidAmount = slidAmount
        val newSlidAmount = getSlidAmountFromTouchEventXPosition(event.x)

        var oldAverageSlidAmount = 0f
        handleDetailsMap.forEach { entry ->
            oldAverageSlidAmount += (entry.key.value.get() ?: 0f) / handleDetailsMap.size
        }

        var moveDifference = newSlidAmount - oldAverageSlidAmount

        Log.v("tinge", "----------------")
        Log.v("tinge", "SliderView average handle moved to position: "+newSlidAmount+ " ("+handleDetailsMap.size+" handles total)")

        handleDetailsMap.forEach { entry ->
            if (newSlidAmount < 0f) {
                entry.key.value.set(-1f)
            } else {
                var newValue = (entry.key.value.get() ?: 0f) + moveDifference
                newValue = maxOf(minOf(newValue, 1f), 0f)
                entry.key.value.set(newValue)
                updateUiOfHandleToMatchCurrentPosition(entry.key, entry.value)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            handleDetailsMap.forEach {  entry ->
                updateUiOfHandleToMatchCurrentPosition(entry.key, entry.value)
            }
        }
    }


    private fun updateUiOfHandleToMatchCurrentPosition(sliderViewHandle: SliderViewHandle, sliderViewHandleInnerDetails: SliderViewHandleInnerDetails) {

        val value = sliderViewHandle.value.get() ?: 0f

        var positionFromLeft = 0f

        val startTrackWidth = if (supportsOffValue == true) binding.sliderTrackViewStart.width else 0
        val mainTrackWidth = binding.sliderTrackView.width

        val handleViewWidth = UiHelper.getPxFromDp(context, 40f)
        val parentLeftPadding = UiHelper.getPxFromDp(context, 8f)

        if (value < 0) {
            positionFromLeft = parentLeftPadding - handleViewWidth/2 + startTrackWidth * 0f
        } else {
            positionFromLeft = parentLeftPadding - handleViewWidth/2 + (startTrackWidth + mainTrackWidth * value)
        }

        Log.v("tinge", "SliderView updateUiOfHandleToMatchCurrentPosition setting left to $positionFromLeft (${if (Thread.currentThread() == Looper.getMainLooper().thread) "on UI thread" else "NOT ON UI THREAD"}) (${handleDetailsMap.size} handles total)")
        //if (positionFromLeft < 1) positionFromLeft = 1f

        (sliderViewHandleInnerDetails.sliderHandleView.root.layoutParams as MarginLayoutParams).leftMargin = positionFromLeft.toInt()
        sliderViewHandleInnerDetails.sliderHandleView.root.requestLayout()

        sliderViewHandleInnerDetails.sliderHandleView.onMoveLabelTextView.text = String.format(onMoveTextStringFormat, value.times(onMoveTextMaximumAmount))
    }

    private fun updateHandleColor(sliderViewHandle: SliderViewHandle, sliderViewHandleInnerDetails: SliderViewHandleInnerDetails) {
        val size = resources.getDimension(R.dimen.slider_handle_diameter).toInt()
        val circleDrawable = ShapeDrawable(OvalShape())
        circleDrawable.intrinsicHeight = size
        circleDrawable.intrinsicWidth = size
        circleDrawable.paint.color = sliderViewHandle.color.get() ?: 0
        sliderViewHandleInnerDetails.sliderHandleView.handleView.setBackgroundDrawable(circleDrawable)
    }

    /**
     * Updates the visibility of the "Min" and "Max" labels, with animation.
     *
     * These are hidden if the slider is moved close to either edge, so that the value label below
     * the slider is not obscured by the Min/Max labels.
     * */
    private fun updateUiOfMaxAndMinLabelVisibility() {
        var maxSlidAmount: Float? = null
        var minSlidAmount: Float? = null

        handleDetailsMap?.forEach {
            if (!it.value.isUiInTouchDownState) return@forEach
            val value = it.key.value.get()
            if (maxSlidAmount == null || value ?: 0f > maxSlidAmount!!) {
                maxSlidAmount = value
            }
            if (minSlidAmount == null || value ?: 1f < minSlidAmount!!) {
                minSlidAmount = value
            }
        }

        // Animate max label out:
        if (maxSlidAmount ?: 0f > AMOUNT_SLID_TO_HIDE_MAX_LABEL && isMaxValueLabelAnimatedVisible && isTouchCurrentlyDown) {
            binding.onRightTextView
                    .animate()
                    .setDuration(ANIMATION_DURATION_MIN_MAX_LABELS_OUT_MS)
                    .alpha(0f)
                    .setListener(null)
            isMaxValueLabelAnimatedVisible = false
        }
        // Animate max label back in:
        else if (maxSlidAmount ?: 0f < AMOUNT_SLID_TO_RESHOW_MAX_LABEL && !isMaxValueLabelAnimatedVisible || !isTouchCurrentlyDown) {
            binding.onRightTextView
                    .animate()
                    .setDuration(ANIMATION_DURATION_MIN_MAX_LABELS_IN_MS)
                    .alpha(1f)
                    .setListener(null)
            isMaxValueLabelAnimatedVisible = true
        }
        // Animate min label out:
        if (minSlidAmount ?: 0f < AMOUNT_SLID_TO_HIDE_MIN_LABEL && isMinValueLabelAnimatedVisible && isTouchCurrentlyDown) {
            binding.onLeftTextView
                    .animate()
                    .setDuration(ANIMATION_DURATION_MIN_MAX_LABELS_OUT_MS)
                    .alpha(0f)
                    .setListener(null)
            isMinValueLabelAnimatedVisible = false
        }
        // Animate min label back in:
        else if (minSlidAmount ?: 0f > AMOUNT_SLID_TO_RESHOW_MIN_LABEL && !isMinValueLabelAnimatedVisible || !isTouchCurrentlyDown) {
            binding.onLeftTextView
                    .animate()
                    .setDuration(ANIMATION_DURATION_MIN_MAX_LABELS_IN_MS)
                    .alpha(1f)
                    .setListener(null)
            isMinValueLabelAnimatedVisible = true
        }
    }

}
