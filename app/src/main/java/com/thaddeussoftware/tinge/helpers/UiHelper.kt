package com.thaddeussoftware.tinge.helpers

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable

class UiHelper private constructor() {
    companion object {

        /**
         * Takes in a bitmap photograph of a light taken with the camera and white washes it
         * slightly, so that when a multiply tint is applied to it in the UI, it looks more
         * colourful.
         * */
        fun whiteTintBitmapPhotographOfLight(bitmapImage: Bitmap): Bitmap {
            val canvas = Canvas()
            val returnValue = Bitmap.createBitmap(bitmapImage.width, bitmapImage.height, Bitmap.Config.ARGB_8888)
            canvas.setBitmap(returnValue)

            val paint = Paint()
            paint.isFilterBitmap = false
            canvas.drawBitmap(bitmapImage, 0f, 0f, paint)
            canvas.drawColor(0x0affffff.toInt(), PorterDuff.Mode.SRC_ATOP)//0x08

            return returnValue
        }

        /**
         * Gets a faded background colour to use in the UI, given the current colour of a light
         * */
        fun getFadedBackgroundColourFromLightColour(hue: Float?, saturation: Float?, brightness: Float?): Int {
            return getColorFromHsv(
                    hue ?: 0f,
                    (saturation?: 0f) * 0.15f * (0.3f + 0.7f*(brightness ?: 0f)),//0.1f  0.4
                    1.0f)
        }

        private fun getColorFromHsv(h:Float, s:Float, v:Float) = Color.HSVToColor(floatArrayOf(h*360f, s, v))

        fun getPxFromDp(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density

    }
}