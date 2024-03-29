package com.thaddeussoftware.tinge.helpers

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import kotlin.math.roundToInt

object ColorHelper {

    /**
     * @param opacity
     * Opacity from 0 (transparent) to 1 (opaque)
     * */
    fun changeOpacityOfColor(color: Int, opacity: Float) =
            (color and 0xffffff) or ((opacity * 255f).toInt().shl(24))


    /**
     * @return Red from 0 - 1
     * */
    fun redFromColor(color: Int): Float {
        return Color.red(color)/255f
    }

    /**
     * @return Green from 0 - 1
     * */
    fun greenFromColor(color: Int): Float {
        return Color.green(color)/255f
    }

    /**
     * @return Blue from 0 - 1
     * */
    fun blueFromColor(color: Int): Float {
        return Color.blue(color)/255f
    }

    /**
     * @return Hue from 0 - 1
     * */
    fun hueFromColor(color: Int): Float {
        val floatArray = FloatArray(3)
        Color.colorToHSV(color, floatArray)
        return floatArray[0]/360f
    }

    /**
     * @Return Saturation from 0 - 1
     * */
    fun saturationFromColor(color: Int): Float {
        val floatArray = FloatArray(3)
        Color.colorToHSV(color, floatArray)
        return floatArray[1]
    }

    /**
     * @Return Value (brightness) from 0 - 1
     * */
    fun valueBrightnessFromColor(color: Int): Float {
        val floatArray = FloatArray(3)
        Color.colorToHSV(color, floatArray)
        return floatArray[2]
    }

    /**
     * @param hue
     * Hue from 0 - 1
     * @param saturation
     * Saturation from 0 - 1
     * @param value
     * Value (brightness) from 0 - 1
     * */
    fun colorFromHsv(hue: Float, saturation: Float, value: Float, alpha: Float = 1f)
            = (alpha*255f).toInt().shl(24) or
                (0xffffff and Color.HSVToColor(floatArrayOf(hue*360f, saturation, value)))

    /**
     * @param red
     * Red from 0 - 1
     * @param green
     * Green from 0 - 1
     * @param blue
     * Blue from 0 - 1
     * */
    fun colorFromRgb(red: Float, green: Float, blue: Float, alpha: Float = 1f): Int {
        return Color.argb(
                (alpha * 255f).roundToInt(),
                (red * 255f).roundToInt(),
                (green * 255f).roundToInt(),
                (blue * 255f).roundToInt())
    }

    /**
     * Merge two colors, preserving the saturation and value of the resulting returned color.
     *
     * This function merges the two colours using a standard RGB-based merge, but then sets the
     * saturation and value to the correct values in between the saturation and value of the
     * initial colors.
     *
     * This method of merging colors can result in smoother gradients when merging between
     * conflicting colous - e.g. mixing Green and Red by RGB will result in a greeny brown,
     * but mixing them using this method will result in a bright yellow.
     * */
    fun mergeColorsPreservingSaturationAndValue(color1: Int, color2: Int, mergeAmount: Float): Int
        = colorFromHsv(
            hueFromColor(ColorUtils.blendARGB(color1, color2, mergeAmount)),
            saturationFromColor(color1) * (1f - mergeAmount) + saturationFromColor(color2) * mergeAmount,
            valueBrightnessFromColor(color1) * (1f - mergeAmount) + valueBrightnessFromColor(color2) * mergeAmount,
            color1.shr(24).and(0xff).toFloat() * (1f-mergeAmount) / 255f + color2.shr(24).and(0xff).toFloat() * mergeAmount / 255f
        )

}