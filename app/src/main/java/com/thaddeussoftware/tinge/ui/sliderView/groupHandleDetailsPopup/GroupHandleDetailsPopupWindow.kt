package com.thaddeussoftware.tinge.ui.sliderView.groupHandleDetailsPopup

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import com.thaddeussoftware.tinge.databinding.ViewSliderPopupBinding
import com.thaddeussoftware.tinge.databinding.ViewSliderPopupHandleBinding
import com.thaddeussoftware.tinge.helpers.UiHelper
import com.thaddeussoftware.tinge.ui.sliderView.SliderViewHandle
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.animation.DecelerateInterpolator
import com.thaddeussoftware.tinge.R


class GroupHandleDetailsPopupWindow(
        private val context: Context,
        handleList: Collection<SliderViewHandle>,
        /**
         * Will be called when the user clicks the unlink button next to a specific handle to let
         * the SliderView know to unlink the given handle.
         *
         * Will return true if the SliderView moves the handle right, or false if the SliderView
         * moves the handle left. This allows this popup window to animate the view dissapearing
         * in the correct direction.
         * */
        private val unlinkHandleAndReturnUnlinkDirection: (SliderViewHandle) -> Boolean
): PopupWindow(context) {

    private val binding = ViewSliderPopupBinding.inflate(LayoutInflater.from(context))

    private val popupWidth = UiHelper.getPxFromDp(context, 240f)

    private var handlesLeftInGroup = handleList.size

    private val popupHandlesDetailsMap = LinkedHashMap<SliderViewHandle, SliderPopupDetails>()

    private class SliderPopupDetails(
            val binding: ViewSliderPopupHandleBinding
    ) {
        var hasBeenUnlinked = false
    }

    init {
        contentView = binding.root
        setBackgroundDrawable(ColorDrawable(0x00_00000))
        setWindowLayoutMode(
                0, WRAP_CONTENT)
        width = popupWidth.toInt()

        isOutsideTouchable = true

        handleList.forEach { sliderViewHandle ->
            val details = SliderPopupDetails(ViewSliderPopupHandleBinding.inflate(LayoutInflater.from(context),
                    binding.handleListLinearLayout, true))

            details.binding.leftBorderView.setBackgroundColor(sliderViewHandle.color.get() ?: 0)
            details.binding.handleNameTextView.text = sliderViewHandle.displayName
            details.binding.unlinkImageView.setOnClickListener {
                unlinkHandle(sliderViewHandle)
            }

            popupHandlesDetailsMap.put(sliderViewHandle, details)
        }

        updateTopText()

        binding.ungroupHandlesButton.setOnClickListener {
            var hasFirstRemainingHandleBeenFound = false

            popupHandlesDetailsMap.forEach { entry ->
                if (!entry.value.hasBeenUnlinked) {
                    if (!hasFirstRemainingHandleBeenFound) {
                        hasFirstRemainingHandleBeenFound = true
                    } else {
                        unlinkHandle(entry.key)
                    }
                }
            }
        }
    }

    private fun updateTopText() {
        val handlesLeft = popupHandlesDetailsMap.values.count { !it.hasBeenUnlinked }

        binding.topTextView.text = "${handlesLeft} handles in group:"
    }

    private fun unlinkHandle(sliderViewHandle: SliderViewHandle) {
        val handleDetails = popupHandlesDetailsMap[sliderViewHandle]
        if (handleDetails == null || handleDetails.hasBeenUnlinked) return

        val wasUnlinkedRight = unlinkHandleAndReturnUnlinkDirection(sliderViewHandle)
        handlesLeftInGroup -= 1
        handleDetails.hasBeenUnlinked = true

        handleDetails.binding.root.animate()
                .translationX(0.7f*handleDetails.binding.root.width.toFloat() * ( if (wasUnlinkedRight) 1f else -1f))
                .alpha(0f)
                .setInterpolator(DecelerateInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        handleDetails.binding.root.visibility = View.GONE

                        if (handlesLeftInGroup <= 1) {
                            dismiss()
                        } else {
                            updateTopText()
                        }
                    }
                })
    }

    fun showAt(view: View, xPositionOnScreen: Float, yPositionOnScreen: Float) {

        val showBelow = yPositionOnScreen < context.resources.displayMetrics.heightPixels/2

        animationStyle = if (showBelow) R.style.GroupHandleDetailsPopupWindow_Animations_FromTop
                else R.style.GroupHandleDetailsPopupWindow_Animations_FromBottom

        binding.root.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))

        binding.topTriangleView.visibility = View.INVISIBLE
        binding.bottomTriangleView.visibility = View.INVISIBLE

        if (showBelow) {
            showAtLocation(view, Gravity.LEFT or Gravity.TOP,
                    (xPositionOnScreen - popupWidth/2).toInt(),
                    (yPositionOnScreen + UiHelper.getPxFromDp(context, 10f)).toInt())
        } else {
            showAtLocation(view, Gravity.LEFT or Gravity.TOP,
                    (xPositionOnScreen - popupWidth/2).toInt(),
                    (yPositionOnScreen - UiHelper.getPxFromDp(context, 10f)).toInt() - binding.root.measuredHeight)//( -yPositionOnScreen + context.resources.displayMetrics.heightPixels).toInt())
        }

        binding.root.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                binding.root.removeOnLayoutChangeListener(this)

                val location = IntArray(2)
                binding.roundedRectCardView.getLocationOnScreen(location)
                val xPositionOfPopupWindow = location[0]

                binding.topTriangleView.translationX =
                        xPositionOnScreen - xPositionOfPopupWindow - UiHelper.getPxFromDp(context, 8f)
                binding.bottomTriangleView.translationX =
                        (xPositionOnScreen - xPositionOfPopupWindow - UiHelper.getPxFromDp(context, 8f))

                if (showBelow) {
                    binding.topTriangleView.visibility = View.VISIBLE
                } else {
                    binding.bottomTriangleView.visibility = View.VISIBLE
                }
            }
        })

    }
}