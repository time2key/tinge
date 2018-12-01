package com.thaddeussoftware.tinge.ui.lights.lightView

import android.arch.lifecycle.ViewModel
import android.databinding.Observable
import android.databinding.ObservableField
import android.graphics.Color
import android.view.View
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightController
import com.thaddeussoftware.tinge.helpers.UiHelper

/**
 * Created by thaddeusreason on 09/02/2018.
 */
class LightViewModel(
        val lightController: LightController
): ViewModel() {

    val isColorSupported:Boolean? = null

    val isInColorMode = lightController.isInColorMode.stagedValueOrLastValueFromHubObservable

    val hue = lightController.hue.stagedValueOrLastValueFromHubObservable
    val saturation = lightController.saturation.stagedValueOrLastValueFromHubObservable
    val brightness = lightController.brightness.stagedValueOrLastValueFromHubObservable

    /**The amount that the white slider should be at*/
    val whiteTemperature = ObservableField<Float>()

    val isExpanded = ObservableField<Boolean>(false)

    val color = ObservableField<Int>(0)

    val colorForPreviewImageView = ObservableField<Int>(0)

    val colorForBackgroundView = ObservableField<Int>(0)

    val displayName = lightController.displayName.stagedValueOrLastValueFromHubObservable

    init {
        updateColorsFromHsv()

        hue.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateColorsFromHsv()
            }
        })
        saturation.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateColorsFromHsv()
            }
        })
        brightness.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateColorsFromHsv()
            }
        })
    }

    fun onExpandContractButtonClicked(view: View) {
        isExpanded.set(! (isExpanded.get() ?: false))
    }

    fun onColorTabClicked(view: View) {
        isInColorMode.set(true)
    }

    fun onWhiteTabClicked(view: View) {
        isInColorMode.set(false)
    }

    fun onHueSliderChanged(newValue:Float) {
        hue.set(newValue)
        //updateColorsFromHsv()
        whiteTemperature.set(-1f)
    }

    fun onSaturationSliderChanged(newValue:Float) {
        saturation.set(newValue)
        //updateColorsFromHsv()
        whiteTemperature.set(-1f)
    }

    fun onBrightnessSliderChanged(newValue:Float) {
        brightness.set(newValue)
        //updateColorsFromHsv()
    }

    fun onWhiteSliderChanged(newValue: Float) {
        whiteTemperature.set(newValue)
        updateColorsFromTemperature()
    }

    private fun updateColorsFromHsv() {
        lightController.applyChanges().subscribe(
                {
                    val i = 0
                },
                {
                    val i = 0
                }
        )
        color.set(getColorFromHsv(hue.get() ?: 0f, saturation.get() ?: 0f, brightness.get() ?: 0f))
        colorForPreviewImageView.set(getColorFromHsv(hue.get() ?: 0f, saturation.get() ?: 0f, 0.5f + 0.5f * (brightness.get() ?: 0f)))
        colorForBackgroundView.set(
                UiHelper.getFadedBackgroundColourFromLightColour(hue.get(), saturation.get(), brightness.get()))
    }

    private fun getColorFromHsv(h:Float, s:Float, v:Float) = Color.HSVToColor(floatArrayOf(h*360f, s, v))


    private fun updateColorsFromTemperature() {
        val color = getColorFromWhiteAmount((whiteTemperature.get() ?: 0f).toDouble())
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hue.set(hsv[0]/360f)
        saturation.set(hsv[1])
        updateColorsFromHsv()
    }

    fun getColorFromWhiteAmount(whiteAmount: Double): Int {
        val temperature = (2000.0+6500.0*whiteAmount)/100.0
        var red = 0.0
        var green = 0.0
        var blue = 0.0

        if (temperature <= 66) {
            red = 255.0
        } else {
            red = temperature - 60.0
            red = 329.698727446 * (Math.pow(red,-0.1332047592))
            red = Math.min(255.0, Math.max(0.0, red))
        }

        if (temperature <= 66) {
            green = temperature
            green = 99.4708025861 * Math.log(green) - 161.1195681661
        } else {
            green = temperature - 60.0
            green = 288.1221695283 * (Math.pow(green,-0.0755148492))
        }
        green = Math.min(255.0, Math.max(0.0, green))

        if (temperature >= 66) {
            blue = 255.0
        } else if (temperature <= 19) {
            blue = 0.0
        } else {
            blue = temperature - 10
            blue = 138.5177312231 * Math.log(blue) - 305.0447927307
        }
        blue = Math.min(255.0, Math.max(0.0, blue))

        return Color.argb(255, red.toInt(), green.toInt(), blue.toInt())
    }

    fun refreshToMatchController() {
        displayName.set(lightController.displayName.stagedValueOrLastValueFromHub)
        isInColorMode.set(lightController.isInColorMode.stagedValueOrLastValueFromHub)
        hue.set(lightController.hue.stagedValueOrLastValueFromHub)


    }
}