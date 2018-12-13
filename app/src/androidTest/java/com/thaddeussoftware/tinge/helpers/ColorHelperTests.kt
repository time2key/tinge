package com.thaddeussoftware.tinge.helpers

import android.graphics.Color
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Test

class ColorHelperTests {

    private val ACCEPTABLE_DEVIANCE = 0.002f

    //region changeOpacityOfColor

    @Test
    fun changeOpacityOfColor_opaqueRed_transparent_transparentReturned() {
        // Act:
        val result = ColorHelper.changeOpacityOfColor(0xff_ff0000.toInt(), 0f)
        // Assert:
        assertEquals(0x00_ff0000.toInt(), result)
    }

    @Test
    fun changeOpacityOfColor_translucentGreen_opaque_opaqueGreenReturned() {
        // Act:
        val result = ColorHelper.changeOpacityOfColor(0xaa_00ff00.toInt(), 1f)
        // Assert:
        assertEquals(0xff_00ff00.toInt(), result)
    }

    @Test
    fun changeOpacityOfColor_transparentBlue_translucent_translucentBlueReturned() {
        // Act:
        val result = ColorHelper.changeOpacityOfColor(0x000000ff.toInt(), 0.5f)
        // Assert:
        assertEquals(0x7f_0000ff.toInt(), result)
    }

    //endregion


    //region hueFromColor

    @Test
    fun hueFromColor_red_0Or1() {
        // Act:
        val result = ColorHelper.hueFromColor(0xff_ff0000.toInt())
        // Assert:
        assertTrue(result == 0f || result == 1f)
    }

    @Test
    fun hueFromColor_translucentBlue_twoThirds() {
        // Act:
        val result = ColorHelper.hueFromColor(0xaa_0000ff.toInt())
        // Assert:
        assertEquals(2f/3f, result)
    }

    @Test
    fun hueFromColor_darkCyan_half() {
        // Act:
        val result = ColorHelper.hueFromColor(0xff_00aaaa.toInt())
        // Assert:
        assertEquals(0.5f, result)
    }

    //endRegion


    //region saturationFromColor

    @Test
    fun saturationFromColor_white_0() {
        assertEquals(0f, ColorHelper.saturationFromColor(0xff_ffffff.toInt()))
    }

    @Test
    fun saturationFromColor_translucentRed_1() {
        assertEquals(1f, ColorHelper.saturationFromColor(0xaa_ff0000.toInt()))
    }

    @Test
    fun saturationFromColor_paleRed_half() {
        assertEquals(0.5f, ColorHelper.saturationFromColor(0xff_ff7f7f.toInt()), ACCEPTABLE_DEVIANCE)
    }

    //endregion


    //region valueBrightnessFromColor

    @Test
    fun valueBrightnessFromColor_translucentWhite_1() {
        assertEquals(1f, ColorHelper.valueBrightnessFromColor(0xaa_ffffff.toInt()))
    }

    @Test
    fun valueBrightnessFromColor_black_0() {
        assertEquals(0f, ColorHelper.valueBrightnessFromColor(0xff_000000.toInt()))
    }

    @Test
    fun valueBrightnessFromColor_medGrey_half() {
        assertEquals(0.5f, ColorHelper.valueBrightnessFromColor(0xff_7f7f7f.toInt()), ACCEPTABLE_DEVIANCE)
    }

    @Test
    fun valueBrightnessFromColor_red_1() {
        assertEquals(1f, ColorHelper.valueBrightnessFromColor(0xff_ff0000.toInt()))
    }

    @Test
    fun valueBrightnessFromColor_translucentHalfGreen_half() {
        assertEquals(0.5f, ColorHelper.valueBrightnessFromColor(0x22_007f00.toInt()), ACCEPTABLE_DEVIANCE)
    }

    //endregion



    //region colorFromHsv

    @Test
    fun colorFromHsv_red_red() {
        assertEquals(0xff_ff0000.toInt(), ColorHelper.colorFromHsv(0f, 1f, 1f))
    }

    @Test
    fun colorFromHsv__saturation0_value1__white() {
        assertEquals(0xff_ffffff.toInt(), ColorHelper.colorFromHsv(0f, 0f, 1f))
    }

    @Test
    fun colorFromHsv__saturation0_value0_halfAlpha__black_halfAlpha() {
        assertEquals(0x7f_000000.toInt(), ColorHelper.colorFromHsv(0.43f, 0f, 0f, 0.5f))
    }

    @Test
    fun colorFromHsv_murkyGreen_murkyGreen() {
        assertEquals(0xff_2fa17d.toInt(), ColorHelper.colorFromHsv(161f/360f, 0.71f, 0.63f))
    }

    //endregion


    //region mergeColorsPreservingSaturationAndValue

    @Test
    fun mergeColorsPreservingSaturationAndValue__blue_green_halfway__cyan() {
        assertEquals(
                0xff_00ffff.toInt(),
                ColorHelper.mergeColorsPreservingSaturationAndValue(0xff_00ff00.toInt(), 0xff_0000ff.toInt(), 0.5f))
    }

    @Test
    fun mergeColorsPreservingSaturationAndValue__transparentWhite_black_halfway__grey() {
        assertEquals(
                0x7f_808080.toInt(),
                ColorHelper.mergeColorsPreservingSaturationAndValue(0x00_ffffff.toInt(), 0xff_000000.toInt(), 0.5f))
    }

    @Test
    fun mergeColorsPreservingSaturationAndValue__transparentBlack_white_0__transparent() {
        assertEquals(
                0x00_000000.toInt(),
                ColorHelper.mergeColorsPreservingSaturationAndValue(0x00_000000, 0xff_ffffff.toInt(), 0f))
    }

    @Test
    fun mergeColorsPreservingSaturationAndValue__red_green_1__green() {
        assertEquals(
                0xff_00ff00.toInt(),
                ColorHelper.mergeColorsPreservingSaturationAndValue(0xff_ff0000.toInt(), 0xff_00ff00.toInt(), 1f))
    }

    //endregion
}