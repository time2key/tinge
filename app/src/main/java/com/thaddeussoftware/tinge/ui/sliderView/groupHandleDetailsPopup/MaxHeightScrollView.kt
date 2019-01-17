package com.thaddeussoftware.tinge.ui.sliderView.groupHandleDetailsPopup

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView
import com.thaddeussoftware.tinge.R

class MaxHeightScrollView(context: Context, attrs: AttributeSet): ScrollView(context, attrs) {

    var maxHeightPixels: Int = -1

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView)
        maxHeightPixels = attributes.getDimensionPixelSize(R.styleable.MaxHeightScrollView_maxHeight, -1)
        attributes.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        if (maxHeightPixels > 0) {
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(maxHeightPixels, View.MeasureSpec.AT_MOST)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

}