package com.thaddeussoftware.tinge.ui.sliderView

import android.content.Context
import androidx.databinding.*
import android.util.AttributeSet
import android.widget.FrameLayout
import com.thaddeussoftware.tinge.databinding.ViewSliderBinding
import android.graphics.drawable.GradientDrawable
import android.os.Looper
import android.util.Log
import android.view.*
import com.thaddeussoftware.tinge.R
import com.thaddeussoftware.tinge.helpers.CollectionComparisonHelper
import com.thaddeussoftware.tinge.helpers.ColorHelper
import com.thaddeussoftware.tinge.helpers.UiHelper
import com.thaddeussoftware.tinge.ui.sliderView.groupHandleDetailsPopup.GroupHandleDetailsPopupWindow
import com.thaddeussoftware.tinge.ui.sliderView.inner.SliderViewGroupHandleDetails
import com.thaddeussoftware.tinge.ui.sliderView.inner.SliderViewSingleHandleDetails
import com.thaddeussoftware.tinge.ui.sliderView.inner.SliderViewSingleOrGroupHandleDetails
import kotlin.math.absoluteValue


/**
 * This view is a custom implementation of SeekBar which provides the ability to have multiple
 * handles which work with data binding, and will be grouped together automatically when moved
 * close together.
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
        /**
         * If the 'Min' and 'Max' animations are faded back in, the animation takes this long in ms.
         * */
        const val ANIMATION_DURATION_MIN_MAX_LABELS_IN_MS = 400L
        /**
         * If the 'Min' and 'Max' labels are faded out, the animation takes this long in ms.
         * */
        const val ANIMATION_DURATION_MIN_MAX_LABELS_OUT_MS = 200L
        /**
         * If the 'Min' and 'Max' labels are faded out as a result of a single click (touching then
         * releasing quickly), the animation takes this long in ms.
         * */
        const val ANIMATION_DURATION_MIN_MAX_LABELS_OUT_SINGLE_CLICK_MS = 50L

        /**
         * If the currently held handle is moved above this value (from 0-1), the 'Max' text is
         * faded out (if present), so it does not overlap with the text below the handle.
         * */
        const val AMOUNT_SLID_TO_HIDE_MAX_LABEL = 0.8f
        /**
         * If the currently held handle is moved below this value (from 0-1), and the 'Max' text
         * has been faded out, it is faded back in.
         * */
        const val AMOUNT_SLID_TO_RESHOW_MAX_LABEL = 0.75f
        /**
         * If the currently held handle is moved below this value (from 0-1), the 'Min' text is
         * faded out (if present), so it does not overlap with the text below the handle.
         * */
        const val AMOUNT_SLID_TO_HIDE_MIN_LABEL = 0.2f
        /**
         * If the currently held handle is moved above this value (from 0-1) and the 'Min' text
         * has been faded out, it is faded back in.
         * */
        const val AMOUNT_SLID_TO_RESHOW_MIN_LABEL = 0.25f

        const val DEFAULT_TRACK_OPACITY = 0.65f

        /**
         * See [onTouchEvent]
         * */
        private val AMOUNT_TO_MOVE_SLIDER_TO_DISABLE_PARENT_SCROLLING_DP = 8f

        /**
         * How close the user needs to click to a specific handle to select it.
         * */
        private val DP_TOLERANCE_TO_SELECT_HANDLE = 16f


        /**
         * If the touch is within this distance of the center of the handle, and the user single
         * clicks on the handle, don't move the handle to the touch.
         *
         * Applies where several handles are grouped.
         * */
        private val X_DISTANCE_GROUP_HANDLE_TOO_CLOSE_DONT_MOVE_HANDLE_DP = 16f

        /**
         * If the touch is within this distance of the center of the handle, and the user single
         * clicks on the handle, don't move the handle to the touch.
         *
         * Applies where there is a single handle.
         * */
        private val X_DISTANCE_SINGLE_HANDLE_TOO_CLOSE_DONT_MOVE_HANDLE_DP = 0f

        /**
         * How close the user needs to move a handle to another handle for the view to suggest
         * merging them - see [currentlyHoveredOverSingleOrGroupHandle]
         *
         * Only applies if the handle was moved by this view - see [X_DISTANCE_TO_AUTO_MERGE_HANDLES_DP]
         * */
        private val X_DISTANCE_TO_SUGGEST_MERGING_HANDLES_DP = 8f

        /**
         * How far away the user needs to move the currently held handle away from
         * [currentlyHoveredOverSingleOrGroupHandle] in order to cancel merging
         *
         * Only applies if the handle was moved by this view
         * */
        private val X_DISTANCE_TO_CANCEL_MERGING_HANDLES_DP = 9f

        /**
         * If handle values are changed, but not by this SliderView, how close two handles need
         * to be moved together to display them as merged.
         * */
        private val X_DISTANCE_TO_AUTO_MERGE_HANDLES_DP = 6f

        /**
         * If handle values are changed, but not by this SliderView, how close two handles need
         * to be moved apart to display them as no longer merged.
         * */
        private val X_DISTANCE_TO_AUTO_UNMERGE_MERGED_HANDLES_DP = 7f

        /**
         * If the unmerge button is pressed for a handle that is merged, it is moved this far in dp
         * away from where it was.
         * */
        private val DISTANCE_TO_MOVE_HANDLE_OUT_OF_GROUP_DP = 16f
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
     * Text label shown below the slider view at the off position (if [supportsOffValue] is true).
     * */
    var offValueText: String? = null
        set(value) {
            field = value
            binding.offTextView.visibility =
                    if (value.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.offTextView.text = value
        }

    /**
     * Text label shown below the slider view at the start / minimum position.
     * */
    var startValueText: String? = null
        set(value) {
            field = value
            binding.onLeftTextView.visibility =
                    if (value.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.onLeftTextView.text = value
        }

    /**
     * Text label shown below the slider view at the end / maximum position.
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
     * The current slid amount of the handle (0-1) will be multiplied by this value, and then
     * formatted by [onMoveTextStringFormat].
     *
     * So e.g. if the slider represents percent / out of 100, this value should be 100.
     * */
    var onMoveTextMaximumAmount: Float = 1f

    /**
     * The 'on move' text shows a label up below the handle when the slider handle is moved.
     *
     * This value determines the format that the value will be shown in.
     * See also [onMoveTextMaximumAmount].
     * */
    var onMoveTextStringFormat: String = ""

    /**
     * Determines if handles should wrap around from the start value to the end value if multiple
     * handles are moved at once. Useful for e.g. a hue slider.
     * */
    var wrapsAroundIfMultipleHandlesMoved: Boolean = false

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
     *
     * This value must be set using data binding on android so changes are propagated to this view.
     * */
    var handles: ObservableList<SliderViewHandle>? = null
        set(value) {
            // Set will be called by data binding whenever list is changed.

            field = value

            if (value != null) {
                CollectionComparisonHelper.compareCollectionsAndIdentifyMissingElements(
                        value,
                        handleDetailsMap.keys,
                        { item1, item2 -> item1 == item2 },
                        { addSliderViewHandle(it) },
                        { removeSliderViewHandle(it) }
                )
            }
        }

    private fun removeSliderViewHandle(sliderViewHandle: SliderViewHandle) {
        val handleDetails = handleDetailsMap[sliderViewHandle]
        handleDetailsMap.remove(sliderViewHandle)
        if (handleDetails?.groupHandleDetails != null) {
            handleDetails.groupHandleDetails?.handlesInsideGroup?.remove(handleDetails)
            if (handleDetails.groupHandleDetails?.handlesInsideGroup?.size ?: 0 <= 1) {
                unmergeEntireGroup(handleDetails.groupHandleDetails!!)
            } else {
                handleDetails.groupHandleDetails?.updateHandleDrawableForCurrentColor()
                handleDetails.groupHandleDetails?.animateOutThenBackIn()
            }
        }
        handleDetails?.animateOutThenRemoveFromLayout()

        handleDetails?.valueObservablePropertyChangedCallback?.let {
            sliderViewHandle.value.removeOnPropertyChangedCallback(it)
        }
        handleDetails?.colorObservablePropertyChangedCallback?.let {
            sliderViewHandle.color.removeOnPropertyChangedCallback(it)
        }

        updateWhetherHandlesShouldBeMergedOrUnmerged(false)
    }

    private fun addSliderViewHandle(sliderViewHandle: SliderViewHandle) {
        val handleDetails = SliderViewSingleHandleDetails(sliderViewHandle, context, binding.frameLayout)
        handleDetailsMap[sliderViewHandle] = handleDetails
        updateUiPositionOfHandleToMatchCurrentHandleValue(handleDetails)
        updateUiDrawableOfHandleToMatchCurrentHandleColor(handleDetails)
        updateWhetherHandlesShouldBeMergedOrUnmerged()
        if (handleDetails.groupHandleDetails == null) { handleDetails.animateIn() }

        handleDetails.valueObservablePropertyChangedCallback = object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (Thread.currentThread() == Looper.getMainLooper().thread) {
                    Log.v("tinge", "SliderView value updated to ${sliderViewHandle.value.get()} - updating UI ("+handles?.size+" handles total)")
                    updateWhetherHandlesShouldBeMergedOrUnmerged()
                    updateUiPositionOfHandleToMatchCurrentHandleValue(handleDetails)
                } else {
                    Log.v("tinge", "SliderView value updated to ${sliderViewHandle.value.get()} - updating UI ("+handles?.size+" handles total (not from ui thread))")
                    post {
                        if (currentlyHeldSliderViewSingleOrGroupHandleDetails == sliderViewHandle
                                || (currentlyHeldSliderViewSingleOrGroupHandleDetails as? SliderViewGroupHandleDetails)
                                        ?.handlesInsideGroup?.any { it.sliderViewHandle == sliderViewHandle } == true) {
                            // The handle that is currently being held by the user was updated by a
                            // background thread.
                            // Don't do anything, or the handle will jump as the user is moving it.
                            Log.e("tinge", "SliderView value updated from non-ui thread as the user was moving it")
                        } else {
                            updateWhetherHandlesShouldBeMergedOrUnmerged()
                            updateUiPositionOfHandleToMatchCurrentHandleValue(handleDetails)
                        }
                    }
                }
            }
        }
        sliderViewHandle.value.addOnPropertyChangedCallback(handleDetails.valueObservablePropertyChangedCallback!!)

        handleDetails.colorObservablePropertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (Thread.currentThread() == Looper.getMainLooper().thread) {
                    updateUiDrawableOfHandleToMatchCurrentHandleColor(handleDetails)
                } else {
                    Log.v("tinge", "SliderView color updated - not from ui thread")
                    post {
                        updateUiDrawableOfHandleToMatchCurrentHandleColor(handleDetails)
                    }
                }
            }
        }
        sliderViewHandle.color.addOnPropertyChangedCallback(handleDetails.colorObservablePropertyChangedCallback!!)
    }

    /**
     * Internal detail stored about each handle.
     *
     * The keys are the same [SliderViewHandle]s stored in [handles], which can be shared between
     * multiple SliderViews.
     *
     * The details specify additional information about each handle in this specific SliderView -
     * see [SliderViewSingleHandleDetails].
     * */
    private val handleDetailsMap = HashMap<SliderViewHandle, SliderViewSingleHandleDetails>()

    private var binding: ViewSliderBinding = ViewSliderBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     * See [updateUiOfMaxAndMinLabelVisibility]
     * */
    private var isMaxValueLabelAnimatedVisible = true
    /**
     * See [updateUiOfMaxAndMinLabelVisibility]
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
     * Sets the track to a given multi-stop gradient. Does not affect handle colors.
     * */
    fun setTrackToColors(vararg colors: Int, offColor: Int = 0xff333333.toInt()) {

        val transparentColour = ColorHelper.changeOpacityOfColor(colors[0], 0.2f)

        val startColors = ArrayList<Int>()
        for (i in 0..20) { startColors.add(offColor) }
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
            // ACTION_CANCEL happens if the user scrolls a parent ScrollView upwards, taking the
            // touch focus away from this view - just cancel all state but don't do anything:

            // Reset the state:
            requestDisallowInterceptTouchEvent(false)
            hasBeenMovedEnoughInXDirectionToBeValidSide = false
            isTouchCurrentlyDown = false
            currentlyHeldSliderViewSingleOrGroupHandleDetails = null
            currentlyHoveredOverSingleOrGroupHandle = null
            previousTouchX = null
            return true
        }
        if (event?.actionMasked == MotionEvent.ACTION_UP) {

            // Move handles according to touch event, same as would happen in an ACTION_MOVE.
            // Note that here, we do not check if hasBeenMovedEnoughInXDirectionToBeValidSlide is
            // true, we always do it on a touch up:
            if (currentlyHeldSliderViewSingleOrGroupHandleDetails != null) {
                val distanceTooCloseDontMoveHandle =
                        if (currentlyHeldSliderViewSingleOrGroupHandleDetails is SliderViewGroupHandleDetails)
                            X_DISTANCE_GROUP_HANDLE_TOO_CLOSE_DONT_MOVE_HANDLE_DP
                        else X_DISTANCE_SINGLE_HANDLE_TOO_CLOSE_DONT_MOVE_HANDLE_DP

                // If touch is outside X_DISTANCE_TOO_CLOSE_DONT_MOVE_HANDLE, move the handle:
                if (currentlyHeldSliderViewSingleOrGroupHandleDetails?.getCurrentHandleValue()
                                ?.minus(getSlidAmountFromTouchEventXPosition(event.x))?.absoluteValue ?: 0f
                        > UiHelper.getPxFromDp(context, distanceTooCloseDontMoveHandle)) {
                    moveSingleOrGroupHandleToTouchEvent(currentlyHeldSliderViewSingleOrGroupHandleDetails!!, event)
                }
            } else {
                moveAllSlidersAroundTouchEvent(event.x, previousTouchX)
                previousTouchX = event.x
            }

            if (currentlyHeldSliderViewSingleOrGroupHandleDetails != null
                    && currentlyHeldSliderViewSingleOrGroupHandleDetails is SliderViewGroupHandleDetails
                    && !hasBeenMovedEnoughInXDirectionToBeValidSide) {

                val popup = GroupHandleDetailsPopupWindow(context,
                        (currentlyHeldSliderViewSingleOrGroupHandleDetails!! as SliderViewGroupHandleDetails)
                                .handlesInsideGroup.map { it.sliderViewHandle })
                        { handle ->
                            moveHandleOutOfGroupAndReturnUnlinkDirection(handleDetailsMap[handle]!!)
                        }

                val location = IntArray(2)
                getLocationOnScreen(location)
                val x = location[0]
                val y = location[1]

                popup.showAt(this,
                        x + getPositionFromLeftInPixelsToDisplayViewAt(
                                currentlyHeldSliderViewSingleOrGroupHandleDetails?.getCurrentHandleValue() ?: 0.5f).toInt() + UiHelper.getPxFromDp(context, 20f),
                        y + UiHelper.getPxFromDp(context, 20f))
            }

            // If the user was hovering the current handle over another handle, merge them:
            if (currentlyHeldSliderViewSingleOrGroupHandleDetails != null
                    && currentlyHoveredOverSingleOrGroupHandle != null) {
                currentlyHeldSliderViewSingleOrGroupHandleDetails?.setCurrentHandleValue(
                        currentlyHoveredOverSingleOrGroupHandle?.getCurrentHandleValue() ?: 0f)
                mergeTwoHandlesIntoGroup(currentlyHeldSliderViewSingleOrGroupHandleDetails!!, currentlyHoveredOverSingleOrGroupHandle!!)
                currentlyHoveredOverSingleOrGroupHandle = null
            }

            // If the user did not drag the sliders enough in the x direction, perform a single
            // click animation on slider views (as no animation will have been played on them):
            val shouldPerformSingleClickAnimationOnHandles = !hasBeenMovedEnoughInXDirectionToBeValidSide
            if (shouldPerformSingleClickAnimationOnHandles) {
                if (currentlyHeldSliderViewSingleOrGroupHandleDetails != null) {
                    if (currentlyHeldSliderViewSingleOrGroupHandleDetails is SliderViewSingleHandleDetails
                            && (currentlyHeldSliderViewSingleOrGroupHandleDetails as? SliderViewSingleHandleDetails)?.groupHandleDetails == null) {
                        animateInThenOutForSingleClick(currentlyHeldSliderViewSingleOrGroupHandleDetails!!)
                    } else if (currentlyHeldSliderViewSingleOrGroupHandleDetails is SliderViewGroupHandleDetails) {
                        animateInThenOutForSingleClick(currentlyHeldSliderViewSingleOrGroupHandleDetails!!)
                    }
                } else {
                    // Only apply animation to each handle if all handles are off or the handle is on:
                    val handlesInOnState = handleDetailsMap.values.sumBy { if (it.getCurrentHandleValue() ?: -1f >= 0f) 1 else 0 }
                    handleDetailsMap.values.forEach {
                        if (it.groupHandleDetails != null) return@forEach
                        if (handlesInOnState == 0 || it.getCurrentHandleValue() ?: -1f >= 0f) {
                            animateInThenOutForSingleClick(it)
                        }
                    }
                    groupHandles.forEach {
                        if (handlesInOnState == 0 || it.getCurrentHandleValue() ?: -1f >= 0f) {
                            animateInThenOutForSingleClick(it)
                        }
                    }
                }
            }

            // Reset the state:
            requestDisallowInterceptTouchEvent(false)
            hasBeenMovedEnoughInXDirectionToBeValidSide = false
            isTouchCurrentlyDown = false
            currentlyHeldSliderViewSingleOrGroupHandleDetails = null
            currentlyHoveredOverSingleOrGroupHandle = null
            previousTouchX = null

            // If single click animation was performed, return now so that
            // updateAnimationsForWhetherTouchIsCurrentlyDown do not override animations started:
            if (shouldPerformSingleClickAnimationOnHandles) {
                return true
            }
        }
        if (event?.actionMasked == MotionEvent.ACTION_DOWN) {
            isTouchCurrentlyDown = true
            initialTouchDownX = event.x

            // Determine and set [currentlyHeldSliderViewSingleOrGroupHandleDetails]:
            val slidAmountOfTouchX = getSlidAmountFromTouchEventXPosition(event.x)
            val slidAmountOfTouchXMinusTolerance = getSlidAmountFromTouchEventXPosition(event.x - UiHelper.getPxFromDp(context, DP_TOLERANCE_TO_SELECT_HANDLE))
            val slidAmountOfTouchXPlusTolerance = getSlidAmountFromTouchEventXPosition(event.x + UiHelper.getPxFromDp(context, DP_TOLERANCE_TO_SELECT_HANDLE))
            handleDetailsMap.values.forEach {
                val handleValue = it.sliderViewHandle.value.get() ?: 0f

                if (handleValue in slidAmountOfTouchXMinusTolerance..slidAmountOfTouchXPlusTolerance) {

                    if (currentlyHeldSliderViewSingleOrGroupHandleDetails != null
                            && (currentlyHeldSliderViewSingleOrGroupHandleDetails?.getCurrentHandleValue()?: 0f
                                - slidAmountOfTouchX).absoluteValue
                            < (handleValue - slidAmountOfTouchX).absoluteValue) {
                        // Ensure that if there are multiple handles within touch tolerance, the
                        // closest one to the users touch is selected:
                        return@forEach
                    }

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

            // If touch has been moved enough in the x direction, disallow parent scrolling:
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

    /**
     * Called by android. Position of every handle must be re-updated when view size changes.
     * */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            handleDetailsMap.forEach {  entry ->
                updateUiPositionOfHandleToMatchCurrentHandleValue(entry.value)
            }
            groupHandles.forEach {
                updateUiPositionOfGroupToMatchCurrentGroupHandleValue(it)
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

    /**
     * Moves a single handle out of a group. Its position will be set to the nearest position it
     * can be that is outside of all groups.
     *
     * If the handle is unlinked in the right direction, true is returned, otherwise false is
     * returned.
     * */
    private fun moveHandleOutOfGroupAndReturnUnlinkDirection(handle: SliderViewSingleHandleDetails): Boolean {

        // This function attempts to find the closest point to move the handle to that is
        // sufficient distance from all the other added points.

        // First, a list of all points to consider are made, which are points just far enough away
        // from each slider handle:

        val pointsToConsider = ArrayList<Float>()

        handles?.forEach {

            if (it == handle.sliderViewHandle) { return@forEach }

            val changeInValue = getChangeInSlidAmountCorrespondingToSizeInDp(DISTANCE_TO_MOVE_HANDLE_OUT_OF_GROUP_DP)
            if (it.value.get() ?: 0f < 0f) {
                pointsToConsider.add(0f)
            } else {
                val lowerPoint = (it.value.get() ?: 0f) - changeInValue
                val upperPoint = (it.value.get() ?: 0f) + changeInValue
                if (lowerPoint >= 0f) pointsToConsider.add(lowerPoint)
                if (upperPoint <= 1f) pointsToConsider.add(upperPoint)
            }
        }

        // Remove any points to consider if they are too close to another slider handle:

        pointsToConsider.removeAll { pointToConsider ->
            var shouldRemovePoint: Boolean = false
            handles?.forEach {

                if (it == handle.sliderViewHandle) { return@forEach }

                val changeInValue = getChangeInSlidAmountCorrespondingToSizeInDp(DISTANCE_TO_MOVE_HANDLE_OUT_OF_GROUP_DP)-0.001f
                val currentValue = it.value.get() ?: 0f
                if (pointToConsider > currentValue - changeInValue
                        && pointToConsider < currentValue + changeInValue) {
                    shouldRemovePoint = true
                }
            }
            return@removeAll shouldRemovePoint
        }

        // Find the closest point to the current handle point:

        val closestPointToConsider = pointsToConsider.minBy {  pointToConsider ->
            ((handle.getCurrentHandleValue() ?: 0f) - pointToConsider).absoluteValue
        }

        // Move the handle to the closest point to consider:

        val oldHandlePosition = handle.getCurrentHandleValue()

        handle.setCurrentHandleValue(closestPointToConsider ?: 0f)
        updateWhetherHandlesShouldBeMergedOrUnmerged(true)

        return closestPointToConsider ?: 0f > oldHandlePosition ?: 0f
    }

    private fun mergeTwoHandlesIntoGroup(handle1: SliderViewSingleOrGroupHandleDetails, handle2: SliderViewSingleOrGroupHandleDetails) {

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

        updateUiPositionOfHandleToMatchCurrentHandleValue(singleHandle1)
        updateUiPositionOfHandleToMatchCurrentHandleValue(singleHandle2)
        updateUiDrawableOfHandleToMatchCurrentHandleColor(singleHandle1)
        updateUiDrawableOfHandleToMatchCurrentHandleColor(singleHandle2)
    }

    private fun unmergeEntireGroup(groupHandleDetails: SliderViewGroupHandleDetails) {
        groupHandleDetails.handlesInsideGroup.forEach {
            it.groupHandleDetails = null
        }

        groupHandles.remove(groupHandleDetails)
        groupHandleDetails.animateOutThenRemoveFromLayout()

        groupHandleDetails.handlesInsideGroup.forEach {
            it.animateIn()
            updateUiPositionOfHandleToMatchCurrentHandleValue(it)
            updateUiDrawableOfHandleToMatchCurrentHandleColor(it)
        }

        groupHandleDetails.handlesInsideGroup.clear()
    }

    /**
     * Moves a single handle to a given touch event - all other handles will remain where they are.
     * */
    private fun moveSingleOrGroupHandleToTouchEvent(handleDetails: SliderViewSingleOrGroupHandleDetails, event: MotionEvent) {
        var newSlidAmount = getSlidAmountFromTouchEventXPosition(event.x)

        if (currentlyHoveredOverSingleOrGroupHandle != null) {
            val differenceBetweenHandles = handleDetails.getCurrentHandleValue()
                    ?.minus(currentlyHoveredOverSingleOrGroupHandle?.getCurrentHandleValue() ?: 0f)?.absoluteValue ?: 0f
            if (differenceBetweenHandles > getChangeInSlidAmountCorrespondingToSizeInDp(X_DISTANCE_TO_CANCEL_MERGING_HANDLES_DP)) {
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
                if (differenceBetweenHandles < getChangeInSlidAmountCorrespondingToSizeInDp(X_DISTANCE_TO_SUGGEST_MERGING_HANDLES_DP)) {
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

    private fun getChangeInSlidAmountCorrespondingToSizeInDp(sizeInDp: Float): Float
            = UiHelper.getPxFromDp(context, sizeInDp) / binding.sliderTrackView.width.toFloat()

    private fun getPositionFromLeftInPixelsToDisplayViewAt(slidAmount: Float): Float {
        var positionFromLeft = 0f
        val startTrackWidth = if (supportsOffValue == true) binding.sliderTrackViewStart.width else 0
        val mainTrackWidth = binding.sliderTrackView.width

        val handleViewWidth = UiHelper.getPxFromDp(context, 40f)
        val parentLeftPadding = if (supportsOffValue == true) binding.sliderTrackViewStart.x else binding.sliderTrackView.x //UiHelper.getPxFromDp(context, 8f)

        if (slidAmount < 0) {
            positionFromLeft = parentLeftPadding - handleViewWidth/2 + startTrackWidth * 0f
        } else {
            positionFromLeft = parentLeftPadding - handleViewWidth/2 + (startTrackWidth + mainTrackWidth * slidAmount)
        }

        return positionFromLeft
    }




    /**
     * Iterates through every handle in this view (single handles and group handles) and updates
     * their animations according to whether they are currently touched, being hovered over to
     * be merged, etc.
     * */
    private fun updateAnimationsForWhetherTouchIsCurrentlyDown() {
        val handlesInOnState = handleDetailsMap.values.sumBy { if (it.getCurrentHandleValue() ?: -1f >= 0f) 1 else 0 }
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
                            || currentlyHeldSliderViewSingleOrGroupHandleDetails == it)
                    // Only apply if all handles are off or this handle is on:
                    && (handlesInOnState == 0 || it.getCurrentHandleValue() ?: -1f >= 0f)) {
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
     * Iterates through every handle and determines which handles should be merged and unmerged
     * into groups.
     *
     * Should be called after a handle value is changed externally / not as a result of the user
     * sliding the handle in this view.
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
                        > getChangeInSlidAmountCorrespondingToSizeInDp(X_DISTANCE_TO_AUTO_UNMERGE_MERGED_HANDLES_DP)) {
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
                    if (distanceBetweenHandles < getChangeInSlidAmountCorrespondingToSizeInDp(X_DISTANCE_TO_AUTO_MERGE_HANDLES_DP)) {
                        mergeTwoHandlesIntoGroup(handleDetails1, handleDetails2)
                    }
                }
            }
        }
    }

    /**
     * Called by [updateUiPositionOfHandleToMatchCurrentHandleValue]
     * */
    private fun updateUiPositionOfGroupToMatchCurrentGroupHandleValue(groupHandleDetails: SliderViewGroupHandleDetails) {
        val value = groupHandleDetails.getCurrentHandleValue() ?: 0f
        val positionFromLeft = getPositionFromLeftInPixelsToDisplayViewAt(value)

        (groupHandleDetails.view.root.layoutParams as MarginLayoutParams).leftMargin = positionFromLeft.toInt()
        groupHandleDetails.view.root.requestLayout()
        groupHandleDetails.view.onMoveLabelTextView.text = if (value < 0) "" else String.format(onMoveTextStringFormat, value.times(onMoveTextMaximumAmount))
    }

    private fun updateUiPositionOfHandleToMatchCurrentHandleValue(singleHandleDetails: SliderViewSingleHandleDetails) {

        if (singleHandleDetails.groupHandleDetails != null) {
            updateUiPositionOfGroupToMatchCurrentGroupHandleValue(singleHandleDetails.groupHandleDetails!!)
            return
        }

        val value = singleHandleDetails.sliderViewHandle.value.get() ?: 0f
        val positionFromLeft = getPositionFromLeftInPixelsToDisplayViewAt(value)

        Log.v("tinge", "SliderView updateUiPositionOfHandleToMatchCurrentHandleValue setting left to $positionFromLeft (${if (Thread.currentThread() == Looper.getMainLooper().thread) "on UI thread" else "NOT ON UI THREAD"}) (${handleDetailsMap.size} handles total)")

        (singleHandleDetails.sliderHandleView.root.layoutParams as MarginLayoutParams).leftMargin = positionFromLeft.toInt()
        singleHandleDetails.sliderHandleView.root.requestLayout()

        singleHandleDetails.sliderHandleView.onMoveLabelTextView.text = if (value < 0) "" else String.format(onMoveTextStringFormat, value.times(onMoveTextMaximumAmount))
    }

    private fun updateUiDrawableOfHandleToMatchCurrentHandleColor(singleHandleDetails: SliderViewSingleHandleDetails) {
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
     *
     * Note that these labels are also updated outside of this method in
     * [animateInThenOutForSingleClick].
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
