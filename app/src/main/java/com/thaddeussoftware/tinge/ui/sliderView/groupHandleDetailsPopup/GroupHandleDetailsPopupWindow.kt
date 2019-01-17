package com.thaddeussoftware.tinge.ui.sliderView.groupHandleDetailsPopup

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import com.thaddeussoftware.tinge.databinding.ViewSliderPopupBinding
import com.thaddeussoftware.tinge.databinding.ViewSliderPopupHandleBinding
import com.thaddeussoftware.tinge.helpers.UiHelper
import com.thaddeussoftware.tinge.ui.sliderView.SliderViewHandle

class GroupHandleDetailsPopupWindow(
        private val context: Context,
        handleList: Collection<SliderViewHandle>,
        unlinkHandle: (SliderViewHandle) -> Unit,
        unlinkAllHandles: () -> Unit
): PopupWindow(context) {

    private val binding = ViewSliderPopupBinding.inflate(LayoutInflater.from(context))

    private val popupWidth = UiHelper.getPxFromDp(context, 240f)

    private var handlesLeftInGroup = handleList.size

    init {
        contentView = binding.root
        setBackgroundDrawable(ColorDrawable(0x00_00000))
        setWindowLayoutMode(
                0, WRAP_CONTENT)
        width = popupWidth.toInt()
        //height = popupMaxHeight.toInt()

        //binding.root.layoutParams.height = MATCH_PARENT
        //binding.root.layoutParams.width = MATCH_PARENT

        isOutsideTouchable = true

        binding.topTextView.text = "${handleList.size} handles in group:"

        handleList.forEach { sliderViewHandle ->
            val itemBinding = ViewSliderPopupHandleBinding.inflate(LayoutInflater.from(context),
                    binding.handleListLinearLayout, true)
            itemBinding.leftBorderView.setBackgroundColor(sliderViewHandle.color.get() ?: 0)
            itemBinding.handleNameTextView.text = sliderViewHandle.displayName
            itemBinding.unlinkImageView.setOnClickListener {
                unlinkHandle(sliderViewHandle)
                handlesLeftInGroup -= 1
                binding.handleListLinearLayout.removeView(itemBinding.root)
                if (handlesLeftInGroup <= 1) {
                    this.dismiss()
                }
            }
        }

        binding.ungroupHandlesButton.setOnClickListener {
            unlinkAllHandles()
            this.dismiss()
        }
    }

    fun showAt(view: View, xPositionOnScreen: Float, yPositionOnScreen: Float) {

        val showBelow = yPositionOnScreen < context.resources.displayMetrics.heightPixels/2

        binding.root.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))

        binding.topTriangleView.visibility = View.INVISIBLE
        binding.bottomTriangleView.visibility = View.INVISIBLE

        if (showBelow) {
            showAtLocation(view, Gravity.LEFT or Gravity.TOP,
                    (xPositionOnScreen - popupWidth/2).toInt(),
                    (yPositionOnScreen).toInt())
        } else {
            showAtLocation(view, Gravity.LEFT or Gravity.TOP,
                    (xPositionOnScreen - popupWidth/2).toInt(),
                    (yPositionOnScreen).toInt() - binding.root.measuredHeight)//( -yPositionOnScreen + context.resources.displayMetrics.heightPixels).toInt())
        }

        binding.root.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                binding.root.removeOnLayoutChangeListener(this)

                val location = IntArray(2)
                binding.roundedRectCardView.getLocationOnScreen(location)
                val xPositionOfPopupWindow = location[0]

                (binding.topTriangleView.layoutParams as ViewGroup.MarginLayoutParams).leftMargin =
                        (xPositionOnScreen - xPositionOfPopupWindow - UiHelper.getPxFromDp(context, 8f)).toInt()
                (binding.bottomTriangleView.layoutParams as ViewGroup.MarginLayoutParams).leftMargin =
                        (xPositionOnScreen - xPositionOfPopupWindow - UiHelper.getPxFromDp(context, 8f)).toInt()

                if (showBelow) {
                    binding.topTriangleView.visibility = View.VISIBLE
                } else {
                    binding.bottomTriangleView.visibility = View.VISIBLE
                }
            }
        })

    }
}