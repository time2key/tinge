package com.thaddeussoftware.tinge.ui.lights.lightView

import androidx.databinding.Observable
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import android.view.View
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightController
import com.thaddeussoftware.tinge.helpers.ColorHelper
import com.thaddeussoftware.tinge.helpers.UiHelper
import com.thaddeussoftware.tinge.ui.lights.InnerLightViewModel
import com.thaddeussoftware.tinge.ui.lights.LightsUiHelper
import com.thaddeussoftware.tinge.ui.sliderView.SliderViewHandle

/**
 * Created by thaddeusreason on 09/02/2018.
 */
class LightViewModel(
        val lightController: LightController
): InnerLightViewModel() {

    override val isColorSupported = ObservableField(true)

    override val isInColorMode = lightController.isInColorMode.stagedValueOrLastValueFromHubObservable

    override val doesSupportColorMode = ObservableField<Boolean>(lightController.doesSupportColorMode)

    override val displayName = lightController.displayName.stagedValueOrLastValueFromHubObservable

    override val isReachable = lightController.isReachable


    override val hueHandles = ObservableArrayList<SliderViewHandle>()
    override val saturationHandles = ObservableArrayList<SliderViewHandle>()
    override val brightnessHandles = ObservableArrayList<SliderViewHandle>()
    override val whiteTemperatureHandles = ObservableArrayList<SliderViewHandle>()


    override val isExpanded = ObservableField<Boolean>(false)

    override val showTopRightExpandButton = ObservableField<Boolean>(false)

    override val colorForPreviewImageView = ObservableField<Int>(0)

    override val colorForBackgroundView = ObservableField<Int>(0)

    override val secondaryInformation = ObservableField<String?>("")


    private val hueObservable = lightController.hue.stagedValueOrLastValueFromHubObservable
    private val saturationObservable = lightController.saturation.stagedValueOrLastValueFromHubObservable
    private val brightnessAndIsOnObservable = ObservableField<Float?>()

    init {

        hueHandles.add(object: SliderViewHandle {
            override val displayName
                    = lightController.displayName.lastValueRetrievedFromHub ?: ""
            override val value = hueObservable
            override val color = ObservableField<Int>(0)
        })
        saturationHandles.add(object: SliderViewHandle {
            override val displayName
                    = lightController.displayName.lastValueRetrievedFromHub ?: ""
            override val value = saturationObservable
            override val color = ObservableField<Int>(0)
        })
        brightnessHandles.add(object: SliderViewHandle {
            override val displayName
                    = lightController.displayName.lastValueRetrievedFromHub ?: ""
            override val value = brightnessAndIsOnObservable
            override val color  = ObservableField<Int>(0)
        })

        hueObservable.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateColorsFromHsv()
                updateHueSliderColor()
                updateSaturationSliderColor()
                updateBrightnessSliderColor()
            }
        })
        saturationObservable.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateColorsFromHsv()
                updateHueSliderColor()
                updateSaturationSliderColor()
                updateBrightnessSliderColor()
            }
        })

        LightsUiHelper.bindObservableBrightnessViewModelPropertyToController(
                brightnessAndIsOnObservable, lightController.isOn, lightController.brightness)

        brightnessAndIsOnObservable.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateColorsFromHsv()
                updateHueSliderColor()
                updateSaturationSliderColor()
                updateBrightnessSliderColor()
                updateExpandedFunctionalityVisibility()
            }

        })


        updateColorsFromHsv()
        updateHueSliderColor()
        updateSaturationSliderColor()
        updateBrightnessSliderColor()
        updateExpandedFunctionalityVisibility()
    }

    private fun updateColorsFromHsv() {
        colorForPreviewImageView.set(
                LightsUiHelper.getPreviewImageTintColourFromLightController(lightController))
        colorForBackgroundView.set(
                LightsUiHelper.getFadedBackgroundColourFromLightController(lightController))
    }

    private fun updateHueSliderColor() {
        hueHandles[0].color.set(ColorHelper.colorFromHsv(hueObservable.get() ?: 1f,1f, 1f))
    }

    private fun updateSaturationSliderColor() {
        val color2 = ColorHelper.colorFromHsv(hueObservable.get()?: 0f, 1f, 1f)

        saturationHandles[0].color.set(ColorUtils.blendARGB(0xffeeeeee.toInt(), color2,
                saturationObservable.get()?:1f))
    }

    private fun updateBrightnessSliderColor() {
        if (brightnessAndIsOnObservable.get() ?: 0f < 0) {
            brightnessHandles[0].color.set(0xff444444.toInt())
        } else {
            brightnessHandles[0].color.set(
                    LightsUiHelper.getColorForBrightnessSlider(
                            hueObservable.get() ?: 0f,
                            saturationObservable.get() ?: 1f,
                            brightnessAndIsOnObservable.get() ?: 0f,
                            brightnessAndIsOnObservable.get() ?: -1f >= 0f))
        }
    }

    private fun updateExpandedFunctionalityVisibility() {
        if (brightnessAndIsOnObservable.get() ?: 0f < 0f) {
            showTopRightExpandButton.set(false)
            isExpanded.set(false)
        } else {
            showTopRightExpandButton.set(true)
        }
    }

    override fun onExpandContractButtonClicked(view: View) {
        isExpanded.set(! (isExpanded.get() ?: false))
    }

    override fun onColorTabClicked(view: View) {
        isInColorMode.set(true)
    }

    override fun onWhiteTabClicked(view: View) {
        isInColorMode.set(false)
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
    }
}