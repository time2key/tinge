package com.thaddeussoftware.tinge.ui.lights

import android.graphics.*
import android.graphics.drawable.Drawable

class WeightedStripedColorDrawable(
        val sizeOf1DpInPixels: Float
): Drawable() {
    override fun setAlpha(alpha: Int) {}

    override fun getOpacity(): Int {return PixelFormat.TRANSLUCENT}

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    private val paint = Paint()

    init {
        paint.isAntiAlias = true
    }

    override fun draw(canvas: Canvas) {
        var totalFixedWidthDp = 0f
        var totalWeight = 0f
        val colorsListCopy = weightedColors.toList()

        colorsListCopy.forEach {
            totalFixedWidthDp += it.fixedWidthDp
            totalWeight += it.weight
        }

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.OVERLAY)

        val widthToDistributeAccordingToWeight = (bounds.width() - totalFixedWidthDp*sizeOf1DpInPixels)
        var currentWidth = 0f
        colorsListCopy.forEachIndexed { i, it ->
            paint.color = it.color
            val widthOfColor = it.fixedWidthDp*sizeOf1DpInPixels +
                    widthToDistributeAccordingToWeight * it.weight / totalWeight
            //canvas.drawRect(currentWidth, 0f, currentWidth + widthOfColor, bounds.height().toFloat(), paint)

            val xShiftY0 = - bounds.height() * 0.15f//* sizeOf1DpInPixels*16
            val xShiftY1 = bounds.height() * 0.15f//* sizeOf1DpInPixels*16

            val path = Path()
            // Top left:
            path.moveTo(currentWidth + xShiftY0, 0f)
            // Top right:
            if (i == colorsListCopy.size - 1) {
                path.lineTo(bounds.width().toFloat(), 0f)
            } else {
                path.lineTo(currentWidth + widthOfColor + xShiftY0, 0f)
            }
            // Bottom right:
            path.lineTo(currentWidth + widthOfColor + xShiftY1, bounds.height().toFloat())
            // Bottom left:
            if (i == 0) {
                path.lineTo(0f, bounds.height().toFloat())
            } else {
                path.lineTo(currentWidth + xShiftY1, bounds.height().toFloat())
            }
            canvas.drawPath(path, paint)

            currentWidth += widthOfColor
        }
    }

    var weightedColors = ArrayList<GlassToolbarWeightedColor>()
        set(value) {
            field = value
            invalidateSelf()
        }


    data class GlassToolbarWeightedColor(val color: Int, val fixedWidthDp: Float, val weight: Float)
}