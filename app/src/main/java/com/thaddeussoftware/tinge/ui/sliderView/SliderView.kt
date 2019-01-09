package com.thaddeussoftware.tinge.ui.sliderView

import android.content.Context
import android.databinding.*
import android.util.AttributeSet
import android.widget.FrameLayout
import com.thaddeussoftware.tinge.databinding.ViewSliderBinding
import android.graphics.drawable.GradientDrawable
import android.os.Looper
import android.util.Log
import android.view.*
import com.thaddeussoftware.tinge.R
import com.thaddeussoftware.tinge.helpers.UiHelper
import com.thaddeussoftware.tinge.ui.sliderView.inner.SimplifiedSetOnObservableListChangedCallback
import com.thaddeussoftware.tinge.ui.sliderView.inner.SliderViewGroupHandleDetails
import com.thaddeussoftware.tinge.ui.sliderView.inner.SliderViewSingleHandleDetails
import com.thaddeussoftware.tinge.ui.sliderView.inner.SliderViewSingleOrGroupHandleDetails
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

    companion object {
        const val ANIMATION_DURATION_MIN_MAX_LABELS_IN_MS = 400L
        const val ANIMATION_DURATION_MIN_MAX_LABELS_OUT_MS = 200L
        const val ANIMATION_DURATION_MIN_MAX_LABELS_OUT_SINGLE_CLICK_MS = 50L

        const val AMOUNT_SLID_TO_HIDE_MAX_LABEL = 0.8f
        const val AMOUNT_SLID_TO_RESHOW_MAX_LABEL = 0.75f
        const val AMOUNT_SLID_TO_HIDE_MIN_LABEL = 0.2f
        const val AMOUNT_SLID_TO_RESHOW_MIN_LABEL = 0.25f

        const val DEFAULT_TRACK_OPACITY = 0.65f

        /**
         * How much the slider view needs to be moved to disable parent scrolling - see [onTouchEvent]
         * */
        private val AMOUNT_TO_MOVE_SLIDER_TO_DISABLE_PARENT_SCROLLING_DP = 8f

        /**
         * How close the user needs to click to a specific handle to select it.
         * */
        private val DP_TOLERANCE_TO_SELECT_HANDLE = 16f

        /**
         * How close the user needs to move a handle to another handle for the SliderView to
         * set the other handle to [currentlyHoveredOverSingleOrGroupHandle], allowing the user
         * to merge them.
         *
         * Only applies if the handle was moved by this SliderView - if the handle was auto moved
         * by another SliderView, [X_DISTANCE_TO_AUTO_MERGE_HANDLES_DP] is used instead.
         * */
        private val X_DISTANCE_TO_SUGGEST_MERGING_HANDLES_DP = 4f

        /**
         * How far away the user needs to move the currently held handle away from
         * [currentlyHoveredOverSingleOrGroupHandle] in order to cancel the process of merging
         * the handles together and set [currentlyHoveredOverSingleOrGroupHandle] back to null.
         *
         * Only applies if the handle was moved by this SliderView - if the handle was auto moved
         * by another SliderView, [X_DISTANCE_TO_AUTO_UNMERGE_MERGED_HANDLES_DP] is used instead.
         * */
        private val X_DISTANCE_TO_CANCEL_MERGING_HANDLES_DP = 6f

        /**
         * If handle values are changed, but not by this SliderView, how close two handles need
         * to be moved together to display them as a merged handle in this SliderView.
         * */
        private val X_DISTANCE_TO_AUTO_MERGE_HANDLES_DP = 4f

        /**
         * If handle values are changed, but not by this SliderView, how close two handles need
         * to be moved apart to display them as a individual handles rather than a merged handle
         * in this SliderView.
         * */
        private val X_DISTANCE_TO_AUTO_UNMERGE_MERGED_HANDLES_DP = 6f
    }

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
     * This value determines the maximum value that will be shown in this text when the handle is
     * at position 1, e.g. this would be 100 if the on move text should show a percent symbol.
     * This value will be formatted by [onMoveTextStringFormat].
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
     * Determines if handles should wrap around from the start value to the end value if multiple
     * handles are moved at once
     * */
    var wrapsAroundIfMultipleHandlesMoved: Boolean = false

    /**
     * Opacity of the background track
     * */
    var trackOpacity = 1f
        set(value) {
            field = value
            binding.sliderTrackView.alpha = value
            binding.sliderTrackViewStart.alpha = value
        }

    /**
     * All individual handles to be shown in this SliderView.
     *
     * The same handle can be added to multiple SliderViews, e.g. to have a group SliderView
     * that controls the handles of multiple other SliderViews.
     * */
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
        handleDetailsMap[sliderViewHandle] = SliderViewSingleHandleDetails(sliderViewHandle, context, binding.frameLayout)
        updateUiOfHandleToMatchCurrentPosition(handleDetailsMap[sliderViewHandle]!!)
        updateWhetherHandlesShouldBeMergedOrUnmerged()
        updateHandleColor(handleDetailsMap[sliderViewHandle]!!)

        sliderViewHandle.value.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (Thread.currentThread() == Looper.getMainLooper().thread) {
                    Log.v("tinge", "SliderView value updated to ${sliderViewHandle.value.get()} - updating UI ("+handles?.size+" handles total)")
                    updateWhetherHandlesShouldBeMergedOrUnmerged()
                    updateUiOfHandleToMatchCurrentPosition(handleDetailsMap[sliderViewHandle]!!)
                } else {
                    Log.v("tinge", "SliderView value updated to ${sliderViewHandle.value.get()} - updating UI ("+handles?.size+" handles total (not from ui thread))")
                    post {
                        updateWhetherHandlesShouldBeMergedOrUnmerged()
                        updateUiOfHandleToMatchCurrentPosition(handleDetailsMap[sliderViewHandle]!!)
                    }
                }
            }
        })
        sliderViewHandle.color.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (Thread.currentThread() == Looper.getMainLooper().thread) {
                    updateHandleColor(handleDetailsMap[sliderViewHandle]!!)
                } else {
                    Log.v("tinge", "SliderView color updated - not from ui thread")
                    post {
                        updateHandleColor(handleDetailsMap[sliderViewHandle]!!)
                    }
                }
            }
        })
    }

    private val handleDetailsMap = HashMap<SliderViewHandle, SliderViewSingleHandleDetails>()

    private var binding: ViewSliderBinding = ViewSliderBinding.inflate(LayoutInflater.from(context), this, true)

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
     * We keep track of the x position when the user touched down, for use in working out
     * [hasBeenMovedEnoughInXDirectionToBeValidSide] - see [onTouchEvent]
     * */
    private var initialTouchDownX: Float? = null

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
     * If the user has selected a single handle to drag, this variable keeps track of this
     * currently held handle.
     *
     * If the user starts dragging without selecting a handle, this property will be null, and
     * all handles will move.
     * */
    private var currentlyHeldSliderViewSingleOrGroupHandleDetails: SliderViewSingleOrGroupHandleDetails? = null

    /**
     * If the user drags the currently held slider handle over another slider handle, then the
     * hovered-over handle gets bigger to indicate that the user can let go to group both
     * handles together.
     *
     * This variable keeps track of the currently hovered-over handle.
     * */
    private var currentlyHoveredOverSingleOrGroupHandle: SliderViewSingleOrGroupHandleDetails? = null

    /**
     * The x position of the last touch event that occurred ([MotionEvent.getX]). When the touch
     * is released, this is set to null.
     * */
    var previousTouchX: Float? = null


    /**
     * Handles that are grouped together into a single multi-handle - see
     * [SliderViewGroupHandleDetails].
     * */
    private val groupHandles = ArrayList<SliderViewGroupHandleDetails>()

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
        wrapsAroundIfMultipleHandlesMoved = attributes.getBoolean(R.styleable.SliderView_wrapsAroundIfMultipleHandlesMoved, false)

        setTrackToColors(0xffaaaaaa.toInt(), 0xffaaaaaa.toInt())
    }



    /**
     * Sets the track to a given multi-stop gradient. Does not affect the handle colour.
     * */
    fun setTrackToColors(vararg colors: Int) {

        val offColour = 0xff333333.toInt()
        val transparentColour = 0x33000000.toInt() or (colors[0] and 0xffffff).toInt()

        val startColors = ArrayList<Int>()
        for (i in 0..20) { startColors.add(offColour) }
        for (i in 0..30) { startColors.add(transparentColour) }
        for (i in 0..20) { startColors.add(colors[0]) }

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
     * the slider more than [AMOUNT_TO_MOVE_SLIDER_TO_DISABLE_PARENT_SCROLLING_DP]
     *
     * When the touch goes up or is cancelled, requestDisallowInterceptTouchEvent is set back.
     * */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        if (event?.actionMasked == MotionEvent.ACTION_CANCEL) {
            requestDisallowInterceptTouchEvent(false)
            hasBeenMovedEnoughInXDirectionToBeValidSide = false
            isTouchCurrentlyDown = false
            currentlyHeldSliderViewSingleOrGroupHandleDetails = null
            currentlyHoveredOverSingleOrGroupHandle = null
            previousTouchX = null
            return true
        }
        if (event?.actionMasked == MotionEvent.ACTION_UP) {

            if (currentlyHeldSliderViewSingleOrGroupHandleDetails != null) {
                moveSingleOrGroupHandleToTouchEvent(currentlyHeldSliderViewSingleOrGroupHandleDetails!!, event)
            } else {
                moveAllSlidersAroundTouchEvent(event.x, previousTouchX)
                previousTouchX = event.x
            }

            if (currentlyHeldSliderViewSingleOrGroupHandleDetails != null
                    && currentlyHoveredOverSingleOrGroupHandle != null) {
                currentlyHeldSliderViewSingleOrGroupHandleDetails?.setCurrentHandleValue(
                        currentlyHoveredOverSingleOrGroupHandle?.getCurrentHandleValue() ?: 0f)
                mergeTwoHandles(currentlyHeldSliderViewSingleOrGroupHandleDetails!!, currentlyHoveredOverSingleOrGroupHandle!!)
                currentlyHoveredOverSingleOrGroupHandle = null
            }

            requestDisallowInterceptTouchEvent(false)
            hasBeenMovedEnoughInXDirectionToBeValidSide = false
            isTouchCurrentlyDown = false
            currentlyHeldSliderViewSingleOrGroupHandleDetails = null
            previousTouchX = null

            var isAnyHandleInTouchDownState = false
            handleDetailsMap.values.forEach {
                if (it.groupHandleDetails != null) return@forEach
                if (it.currentViewStateAnimatedInto != SliderViewSingleOrGroupHandleDetails.AnimatableState.NORMAL) isAnyHandleInTouchDownState = true
            }
            groupHandles.forEach {
                if (it.currentViewStateAnimatedInto != SliderViewSingleOrGroupHandleDetails.AnimatableState.NORMAL) isAnyHandleInTouchDownState = true
            }
            if (!isAnyHandleInTouchDownState) {
                // no handles are in a touch down state - this indicates that this ACTION_UP event
                // is as a result of a quick click down and then up again, without moving the
                // slider around in between the down and up:
                if (currentlyHeldSliderViewSingleOrGroupHandleDetails != null) {
                    animateInThenOutForSingleClick(currentlyHeldSliderViewSingleOrGroupHandleDetails!!)
                } else {
                    handleDetailsMap.values.forEach {
                        if (it.groupHandleDetails != null) return@forEach
                        animateInThenOutForSingleClick(it)
                    }
                    groupHandles.forEach {
                        animateInThenOutForSingleClick(it)
                    }
                }
                return true
            }
        }
        if (event?.actionMasked == MotionEvent.ACTION_DOWN) {
            isTouchCurrentlyDown = true
            initialTouchDownX = event.x

            val slidAmountOfTouchXMinusTolerance = getSlidAmountFromTouchEventXPosition(event.x - UiHelper.getPxFromDp(context, DP_TOLERANCE_TO_SELECT_HANDLE))
            val slidAmountOfTouchXPlusTolerance = getSlidAmountFromTouchEventXPosition(event.x + UiHelper.getPxFromDp(context, DP_TOLERANCE_TO_SELECT_HANDLE))

            handleDetailsMap.values.forEach {
                val handleValue = it.sliderViewHandle.value.get() ?: 0f

                if (slidAmountOfTouchXMinusTolerance <= handleValue
                        && slidAmountOfTouchXPlusTolerance >= handleValue) {
                    if (it.groupHandleDetails == null) {
                        currentlyHeldSliderViewSingleOrGroupHandleDetails = it
                    } else {
                        currentlyHeldSliderViewSingleOrGroupHandleDetails = it.groupHandleDetails
                    }
                }
            }
        }
        if (event?.actionMasked == MotionEvent.ACTION_DOWN ||
                event?.actionMasked == MotionEvent.ACTION_MOVE) {

            isTouchCurrentlyDown = true

            // If has been moved enough in the x direction, disallow parent scrolling:

            if (initialTouchDownX?.minus(event.x)?.absoluteValue ?: 0f
                    > UiHelper.getPxFromDp(context, AMOUNT_TO_MOVE_SLIDER_TO_DISABLE_PARENT_SCROLLING_DP)) {
                requestDisallowInterceptTouchEvent(true)
                hasBeenMovedEnoughInXDirectionToBeValidSide = true
            }

            if (hasBeenMovedEnoughInXDirectionToBeValidSide) {
                if (currentlyHeldSliderViewSingleOrGroupHandleDetails != null) {
                    moveSingleOrGroupHandleToTouchEvent(currentlyHeldSliderViewSingleOrGroupHandleDetails!!, event)
                } else {
                    moveAllSlidersAroundTouchEvent(event.x, previousTouchX)
                    previousTouchX = event.x
                }
            } else {
                return true
            }
        }

        updateUiOfMaxAndMinLabelVisibility()
        updateAnimationsForWhetherTouchIsCurrentlyDown()

        return true

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            handleDetailsMap.forEach {  entry ->
                updateUiOfHandleToMatchCurrentPosition(entry.value)
            }
            groupHandles.forEach {
                updateUiPositionOfGroupToMatchCurrentPosition(it)
            }
        }
    }





    /**
     * Called by [onTouchEvent] when the user single-clicks (touches down and then up without
     * moving the touch first).
     *
     * Performs an animation that animates the touch handle big and then small again.
     * */
    private fun animateInThenOutForSingleClick(singleOrGroupHandleDetails: SliderViewSingleOrGroupHandleDetails) {

        singleOrGroupHandleDetails.animateInThenOutForSingleClick {
            updateUiOfMaxAndMinLabelVisibility()
        }

        if (singleOrGroupHandleDetails.getCurrentHandleValue() ?: 0f > AMOUNT_SLID_TO_HIDE_MAX_LABEL) {
            isMaxValueLabelAnimatedVisible = false
            binding.onRightTextView
                    .animate()
                    .alpha(0f)
                    .setDuration(SliderView.ANIMATION_DURATION_MIN_MAX_LABELS_OUT_SINGLE_CLICK_MS)
                    .setListener(null)
        }
        if (singleOrGroupHandleDetails.getCurrentHandleValue() ?: 1f < AMOUNT_SLID_TO_HIDE_MIN_LABEL) {
            isMinValueLabelAnimatedVisible = false
            binding.onLeftTextView
                    .animate()
                    .alpha(0f)
                    .setDuration(SliderView.ANIMATION_DURATION_MIN_MAX_LABELS_OUT_SINGLE_CLICK_MS)
                    .setListener(null)
        }
    }

    private fun mergeTwoHandles(handle1: SliderViewSingleOrGroupHandleDetails, handle2: SliderViewSingleOrGroupHandleDetails) {

        val singleHandle1 = if (handle1 is SliderViewSingleHandleDetails) handle1
                else (handle1 as SliderViewGroupHandleDetails).handlesInsideGroup.first()

        val singleHandle2 = if (handle2 is SliderViewSingleHandleDetails) handle2
                else (handle2 as SliderViewGroupHandleDetails).handlesInsideGroup.first()

        if (singleHandle1.groupHandleDetails == null && singleHandle2.groupHandleDetails == null) {
            // Neither handle is in a group:

            val groupHandle = SliderViewGroupHandleDetails(context, binding.frameLayout)

            groupHandle.handlesInsideGroup.add(singleHandle1)
            groupHandle.handlesInsideGroup.add(singleHandle2)

            singleHandle1.groupHandleDetails = groupHandle
            singleHandle2.groupHandleDetails = groupHandle

            groupHandles.add(groupHandle)

            singleHandle1.animateOutThenHide()
            singleHandle2.animateOutThenHide()
            groupHandle.animateInAfterDelay()
        } else if (singleHandle1.groupHandleDetails == null) {
            // only singleHandle2 is in a group:

            val groupHandle = singleHandle2.groupHandleDetails!!

            groupHandle.handlesInsideGroup.add(singleHandle1)
            singleHandle1.groupHandleDetails = groupHandle

            singleHandle1.animateOutThenHide()
            groupHandle.animateOutThenBackIn()
        } else if (singleHandle2.groupHandleDetails == null) {
            // only singleHandle1 is in a group:

            val groupHandle = singleHandle1.groupHandleDetails!!

            groupHandle.handlesInsideGroup.add(singleHandle2)
            singleHandle2.groupHandleDetails = groupHandle

            singleHandle2.animateOutThenHide()
            groupHandle.animateOutThenBackIn()
        } else if (singleHandle1.groupHandleDetails != singleHandle2.groupHandleDetails) {
            // Both handles are in different groups:

            val groupHandle1 = singleHandle1.groupHandleDetails!!
            val groupHandle2 = singleHandle2.groupHandleDetails!!

            // Remove first handle and merge contents into second handle:

            groupHandle1.handlesInsideGroup.forEach {
                groupHandle2.handlesInsideGroup.add(it)
                it.groupHandleDetails = groupHandle2
            }

            groupHandle1.handlesInsideGroup.clear()
            groupHandles.remove(groupHandle1)
            //(groupHandle1.view.root.parent as ViewGroup).removeView(groupHandle1.view.root)

            groupHandle1.animateOutThenRemoveFromLayout()
            groupHandle2.animateInAfterDelay()
        }

        updateUiOfHandleToMatchCurrentPosition(singleHandle1)
        updateUiOfHandleToMatchCurrentPosition(singleHandle2)
        updateHandleColor(singleHandle1)
        updateHandleColor(singleHandle2)
    }

    private fun unmergeEntireGroup(groupHandleDetails: SliderViewGroupHandleDetails) {
        groupHandleDetails.handlesInsideGroup.forEach {
            it.groupHandleDetails = null
        }

        groupHandles.remove(groupHandleDetails)
        groupHandleDetails.animateOutThenRemoveFromLayout()

        groupHandleDetails.handlesInsideGroup.forEach {
            it.animateIn()
            updateUiOfHandleToMatchCurrentPosition(it)
            updateHandleColor(it)
        }

        groupHandleDetails.handlesInsideGroup.clear()
    }

    private fun moveSingleOrGroupHandleToTouchEvent(handleDetails: SliderViewSingleOrGroupHandleDetails, event: MotionEvent) {
        var newSlidAmount = getSlidAmountFromTouchEventXPosition(event.x)

        if (currentlyHoveredOverSingleOrGroupHandle != null) {
            val differenceBetweenHandles = handleDetails.getCurrentHandleValue()
                    ?.minus(currentlyHoveredOverSingleOrGroupHandle?.getCurrentHandleValue() ?: 0f)?.absoluteValue ?: 0f
            if (differenceBetweenHandles > getChangeInValueCorrespondingToSizeInDp(X_DISTANCE_TO_CANCEL_MERGING_HANDLES_DP)) {
                currentlyHoveredOverSingleOrGroupHandle = null
            }
        }

        if (currentlyHoveredOverSingleOrGroupHandle == null) {
            var handleToMergeCurrentHandleWith: SliderViewSingleOrGroupHandleDetails? = null
            handleDetailsMap.values.forEach {
                if (handleDetails == it
                        || (handleDetails is SliderViewGroupHandleDetails
                                && handleDetails.handlesInsideGroup.contains(it))) {
                    return@forEach
                }

                val differenceBetweenHandles = handleDetails.getCurrentHandleValue()
                        ?.minus(it.getCurrentHandleValue() ?: 0f)?.absoluteValue ?: 0f
                if (differenceBetweenHandles < getChangeInValueCorrespondingToSizeInDp(X_DISTANCE_TO_SUGGEST_MERGING_HANDLES_DP)) {
                    if (it.groupHandleDetails == null) {
                        handleToMergeCurrentHandleWith = it
                    } else {
                        handleToMergeCurrentHandleWith = it.groupHandleDetails!!
                    }
                }
            }
            handleToMergeCurrentHandleWith?.let {
                currentlyHoveredOverSingleOrGroupHandle = it
            }
        }

        handleDetails.setCurrentHandleValue(newSlidAmount)
    }

    /**
     * Moves all the handles so they are around the given touch event.
     *
     * Called by [onTouchEvent]
     * */
    private fun moveAllSlidersAroundTouchEvent(eventX: Float, previousEventX: Float?) {
        val newSlidAmount = getSlidAmountFromTouchEventXPosition(eventX)

        if (newSlidAmount < 0) {
            // Set all handles to off:
            handleDetailsMap.values.forEach { it.setCurrentHandleValue(-1f) }
        } else {
            val numberOfHandlesThatAreOn = handleDetailsMap.values.count { it.getCurrentHandleValue()?:-1f >= 0f }
            if (numberOfHandlesThatAreOn == 0) {
                // Set all handles to given value:
                handleDetailsMap.values.forEach { it.setCurrentHandleValue(newSlidAmount) }
            } else {
                // Move all handles that are on to a given value:
                var moveDifference = 0f

                if (previousEventX == null) {
                    val averageCurrentSlidAmount = handleDetailsMap.values.sumByDouble {
                        Math.max(0.0, (it.getCurrentHandleValue()?:0f).toDouble())
                    }.toFloat() / numberOfHandlesThatAreOn
                    moveDifference = newSlidAmount - averageCurrentSlidAmount
                } else {
                    val oldSlidAmount = getSlidAmountFromTouchEventXPosition(previousEventX)
                    moveDifference = newSlidAmount - oldSlidAmount
                }

                handleDetailsMap.values.forEach {
                    // Only modify handles that are on:
                    if (it.getCurrentHandleValue()?:-1f < 0f) return@forEach

                    var newValue = (it.getCurrentHandleValue()?:0f) + moveDifference
                    if (wrapsAroundIfMultipleHandlesMoved) {
                        if (newValue < 0) {
                            newValue += 1
                        } else if (newValue > 1) {
                            newValue -= 1
                        }
                    } else {
                        newValue = maxOf(minOf(newValue, 1f), 0f)
                    }
                    it.setCurrentHandleValue(newValue)
                }
            }
        }
        updateWhetherHandlesShouldBeMergedOrUnmerged(true)
    }




    private fun getSlidAmountFromTouchEventXPosition(eventX: Float): Float {
        var newSlidAmount = 0f

        if (eventX < binding.sliderTrackViewStart.x + binding.sliderTrackViewStart.width*0.7f && supportsOffValue == true) {
            newSlidAmount = -1f
        } else {
            newSlidAmount = ((eventX
                    ?: 0f) - binding.sliderTrackView.x.toInt()) / binding.sliderTrackView.width.toFloat()
            newSlidAmount = maxOf(minOf(newSlidAmount, 1f), 0f)
        }
        return newSlidAmount
    }

    private fun getChangeInValueCorrespondingToSizeInDp(sizeInDp: Float): Float
            = UiHelper.getPxFromDp(context, sizeInDp) / binding.sliderTrackView.width.toFloat()

    private fun getPositionFromLeftInPixelsToDisplayViewAt(value: Float): Float {
        var positionFromLeft = 0f
        val startTrackWidth = if (supportsOffValue == true) binding.sliderTrackViewStart.width else 0
        val mainTrackWidth = binding.sliderTrackView.width

        val handleViewWidth = UiHelper.getPxFromDp(context, 40f)
        val parentLeftPadding = if (supportsOffValue == true) binding.sliderTrackViewStart.x else binding.sliderTrackView.x //UiHelper.getPxFromDp(context, 8f)

        if (value < 0) {
            positionFromLeft = parentLeftPadding - handleViewWidth/2 + startTrackWidth * 0f
        } else {
            positionFromLeft = parentLeftPadding - handleViewWidth/2 + (startTrackWidth + mainTrackWidth * value)
        }

        return positionFromLeft
    }




    /**
     * Starts animations to either turn the view into the touch-down state (with the bigger handle
     * etc) or the touch-up state (with the regular handle etc).
     *
     * Updates [isUiInTouchDownState].
     * */
    private fun updateAnimationsForWhetherTouchIsCurrentlyDown() {

        val updateHandle = { it: SliderViewSingleOrGroupHandleDetails ->
            if (it.currentViewStateAnimatedInto == SliderViewSingleOrGroupHandleDetails.AnimatableState.REMOVED
                    || it.currentViewStateAnimatedInto == SliderViewSingleOrGroupHandleDetails.AnimatableState.HIDDEN) {
                // Do nothing
            } else if (isTouchCurrentlyDown
                    && hasBeenMovedEnoughInXDirectionToBeValidSide
                    && currentlyHoveredOverSingleOrGroupHandle == it) {
                it.animateToHoveredOverForMergeState()
            } else if (isTouchCurrentlyDown
                    && hasBeenMovedEnoughInXDirectionToBeValidSide
                    && (currentlyHeldSliderViewSingleOrGroupHandleDetails == null
                            || currentlyHeldSliderViewSingleOrGroupHandleDetails == it)) {
                if (currentlyHoveredOverSingleOrGroupHandle != null) {
                    it.animateToHoveringOverForMergeState()
                } else {
                    it.animateToCurrentlyBeingHeldStateIfAppropriate()
                }
            } else {
                it.animateToNormalStateIfAppropriate()
            }
        }

        handleDetailsMap.values.forEach(updateHandle)
        groupHandles.forEach(updateHandle)
    }

    /**
     * Determines whether handles should be merged or unmerged with a group.
     * Should be called after a handle value is changed.
     * */
    private fun updateWhetherHandlesShouldBeMergedOrUnmerged(shouldRunWhenTouchCurrentlyDown: Boolean = false) {
        // Don't run if the touch is currently down - if this function were to be run while the
        // user was touching the view, then groups would be merged/unmerged prematurely:
        if (isTouchCurrentlyDown && !shouldRunWhenTouchCurrentlyDown) return

        // Unmerge entire groups if any of the handles in them are outside of the group:
        groupHandles.toMutableList().forEach groupHandlesLoop@ { groupHandle ->
            var lastIndividualHandleValue: Float? = null
            groupHandle.handlesInsideGroup.forEach { individualHandle ->
                val currentValue = individualHandle.sliderViewHandle.value.get()
                if (lastIndividualHandleValue != null
                        && currentValue?.minus(lastIndividualHandleValue!!)?.absoluteValue ?: 0f
                        > getChangeInValueCorrespondingToSizeInDp(X_DISTANCE_TO_AUTO_UNMERGE_MERGED_HANDLES_DP)) {
                    unmergeEntireGroup(groupHandle)
                    return@groupHandlesLoop
                }
                lastIndividualHandleValue = currentValue
            }
        }

        // Merge handles together if they are close enough:
        handleDetailsMap.values.forEach { handleDetails1 ->
            handleDetailsMap.values.forEach { handleDetails2 ->
                if (handleDetails1 != handleDetails2
                        && (handleDetails1.groupHandleDetails != handleDetails2.groupHandleDetails
                                || handleDetails1.groupHandleDetails == null)) {

                    val distanceBetweenHandles = handleDetails1.sliderViewHandle.value.get()
                            ?.minus(handleDetails2.sliderViewHandle.value.get() ?: 0f)
                            ?.absoluteValue ?: 0f
                    if (distanceBetweenHandles < getChangeInValueCorrespondingToSizeInDp(X_DISTANCE_TO_AUTO_MERGE_HANDLES_DP)) {
                        mergeTwoHandles(handleDetails1, handleDetails2)
                    }
                }
            }
        }
    }

    private fun updateUiPositionOfGroupToMatchCurrentPosition(groupHandleDetails: SliderViewGroupHandleDetails) {
        val value = groupHandleDetails.getCurrentHandleValue() ?: 0f
        val positionFromLeft = getPositionFromLeftInPixelsToDisplayViewAt(value)

        (groupHandleDetails.view.root.layoutParams as MarginLayoutParams).leftMargin = positionFromLeft.toInt()
        groupHandleDetails.view.root.requestLayout()
        groupHandleDetails.view.onMoveLabelTextView.text = if (value < 0) "" else String.format(onMoveTextStringFormat, value.times(onMoveTextMaximumAmount))
    }

    private fun updateUiOfHandleToMatchCurrentPosition(singleHandleDetails: SliderViewSingleHandleDetails) {

        if (singleHandleDetails.groupHandleDetails != null) {
            updateUiPositionOfGroupToMatchCurrentPosition(singleHandleDetails.groupHandleDetails!!)
            return
        }

        val value = singleHandleDetails.sliderViewHandle.value.get() ?: 0f
        val positionFromLeft = getPositionFromLeftInPixelsToDisplayViewAt(value)

        Log.v("tinge", "SliderView updateUiOfHandleToMatchCurrentPosition setting left to $positionFromLeft (${if (Thread.currentThread() == Looper.getMainLooper().thread) "on UI thread" else "NOT ON UI THREAD"}) (${handleDetailsMap.size} handles total)")

        (singleHandleDetails.sliderHandleView.root.layoutParams as MarginLayoutParams).leftMargin = positionFromLeft.toInt()
        singleHandleDetails.sliderHandleView.root.requestLayout()

        singleHandleDetails.sliderHandleView.onMoveLabelTextView.text = if (value < 0) "" else String.format(onMoveTextStringFormat, value.times(onMoveTextMaximumAmount))
    }

    private fun updateHandleColor(singleHandleDetails: SliderViewSingleHandleDetails) {
        if (singleHandleDetails.groupHandleDetails != null) {
            singleHandleDetails.groupHandleDetails?.updateHandleDrawableForCurrentColor()
        } else {
            singleHandleDetails.updateHandleDrawableForCurrentColor()
        }
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

        handleDetailsMap.values.forEach {
            if (it.currentViewStateAnimatedInto == SliderViewSingleOrGroupHandleDetails.AnimatableState.NORMAL) return@forEach
            val value = it.getCurrentHandleValue()
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
