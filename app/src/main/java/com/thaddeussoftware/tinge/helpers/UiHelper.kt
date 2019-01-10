package com.thaddeussoftware.tinge.helpers

import android.content.Context
import android.graphics.*

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

        fun getPxFromDp(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density

        fun getPxFromSp(context: Context, sp: Float): Float = sp * context.resources.displayMetrics.scaledDensity
    }
}