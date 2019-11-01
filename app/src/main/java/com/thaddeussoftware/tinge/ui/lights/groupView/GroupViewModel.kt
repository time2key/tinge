package com.thaddeussoftware.tinge.ui.lights.groupView

import androidx.databinding.Observable
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import android.os.Handler
import android.os.Looper
import com.thaddeussoftware.tinge.tingeapi.generic.controller.LightGroupController
import com.thaddeussoftware.tinge.helpers.CollectionComparisonHelper
import com.thaddeussoftware.tinge.helpers.ColorHelper
import com.thaddeussoftware.tinge.ui.lights.InnerLightViewModel
import com.thaddeussoftware.tinge.ui.lights.LightsUiHelper
import com.thaddeussoftware.tinge.ui.lights.lightView.LightViewModel
import com.thaddeussoftware.tinge.ui.sliderView.SliderViewHandle

class GroupViewModel(
        val lightGroupController: LightGroupController
): InnerLightViewModel() {
    override val isColorSupported = ObservableField<Boolean>(true)

    override val isInColorMode = ObservableField<Boolean?>(true)

    override val doesSupportColorMode = ObservableField<Boolean>(true)

    override val hueHandles = ObservableArrayList<SliderViewHandle>()
    override val saturationHandles = ObservableArrayList<SliderViewHandle>()
    override val brightnessHandles = ObservableArrayList<SliderViewHandle>()
    override val whiteTemperatureHandles = ObservableArrayList<SliderViewHandle>()

    override val colorForPreviewImageView = ObservableField<Int>(0xffffffff.toInt())

    override val displayName = lightGroupController.name.stagedValueOrLastValueFromHubObservable

    override val secondaryInformation = ObservableField<String?>("")

    override val isReachable = ObservableField<Boolean>(true)

    val meanBrightness = ObservableField<Float>(0f)
    val meanHue = ObservableField<Float>(0f)
    val meanSaturation = ObservableField<Float>(0f)

    override val isExpanded = ObservableField<Boolean>(false)
    override val showTopRightExpandButton = ObservableField<Boolean>(false)

    override val colorForBackgroundView = ObservableField<Int>(0)



    /**
     * View models of each of the lights that should be shown on screen
     * */
    var individualLightViewModels = ObservableArrayList<LightViewModel>()

    init {

        lightGroupController.onLightPropertyModifiedSingleLiveEvent.stagedValueOrValueFromHubUpdatedLiveEvent
                .addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                updateMeanProperties()
                refreshSecondaryText()
                setupColorForBackgroundView()
            }
        })

        lightGroupController.onLightsOrSubgroupsAddedOrRemovedSingleLiveEvent.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                refreshListOfLightsToMatchController()
                updateMeanProperties()
                refreshSecondaryText()
                setupColorForBackgroundView()
            }
        })

        refreshSecondaryText()
        refreshListOfLightsToMatchController()
        setupColorForBackgroundView()
    }

    /**
     * Updates:
     * * Whether the hue and saturation handle should be shown for this light, based on whether
     * it is reachable and on.
     * * Whether the brightness handle should be shown for this light, based on whether it is
     * reachable.
     * * Whether the top right expanded arrow is shown, based on if any lights are on.
     * */
    private fun onIsReachableOrIsOnChangedForIndividualLight(lightViewModel: LightViewModel) {
        val isOn = lightViewModel.lightController.isOn.stagedValueOrLastValueFromHubObservable.get() ?: false
        val isReachable = lightViewModel.lightController.isReachable.get() ?: false
        if (isOn && isReachable) {
            lightViewModel.hueHandles.forEach {
                if (!hueHandles.contains(it)) { hueHandles.add(it) }
            }
            lightViewModel.saturationHandles.forEach {
                if (!saturationHandles.contains(it)) { saturationHandles.add(it) }
            }
        } else {
            lightViewModel.hueHandles.forEach { hueHandles.remove(it) }
            lightViewModel.saturationHandles.forEach { saturationHandles.remove(it) }
        }
        if (isReachable) {
            lightViewModel.brightnessHandles.forEach {
                if (!brightnessHandles.contains(it)) { brightnessHandles.add(it) }
            }
        } else {
            lightViewModel.brightnessHandles.forEach { brightnessHandles.remove(it) }
        }

        if (hueHandles.size > 0) {
            showTopRightExpandButton.set(true)
        } else {
            showTopRightExpandButton.set(false)
            isExpanded.set(false)
        }
    }

    private fun setupColorForBackgroundView() {
        colorForBackgroundView.set(
                LightsUiHelper.getFadedBackgroundColourFromLightColour(
                        meanHue.get(), meanSaturation.get(), meanBrightness.get(),
                        lightGroupController.lightsNotInSubgroup.any {
                            it.isOn.stagedValueOrLastValueFromHub == true
                        },
                        true))
    }

    private fun updateMeanProperties() {
        var totalRed = 0f
        var totalGreen = 0f
        var totalBlue = 0f

        var totalRedWithFullSat = 0f
        var totalGreenWithFullSat = 0f
        var totalBlueWithFullSat = 0f

        var totalLights = 0


        lightGroupController.lightsNotInSubgroup.forEach {
            if (it.isReachable.get() == true
                    && it.isOn.stagedValueOrLastValueFromHub == true) {

                val hue = it.hue.stagedValueOrLastValueFromHub ?: 0f
                val sat = it.saturation.stagedValueOrLastValueFromHub ?: 0f
                val brightness = it.brightness.stagedValueOrLastValueFromHub ?: 0f

                val color = ColorHelper.colorFromHsv(hue, sat, brightness)
                val red = ColorHelper.redFromColor(color)
                val green = ColorHelper.greenFromColor(color)
                val blue = ColorHelper.blueFromColor(color)
                totalRed += red
                totalGreen += green
                totalBlue += blue

                val colorWithFullSat = ColorHelper.colorFromHsv(hue, 1f, brightness)
                val redWithFullSat = ColorHelper.redFromColor(colorWithFullSat)
                val greenWithFullSat = ColorHelper.greenFromColor(colorWithFullSat)
                val blueWithFullSat = ColorHelper.blueFromColor(colorWithFullSat)
                totalRedWithFullSat += redWithFullSat
                totalGreenWithFullSat += greenWithFullSat
                totalBlueWithFullSat += blueWithFullSat

                totalLights += 1
            }
        }

        if (totalLights == 0) {
            meanHue.set(0f)
            meanSaturation.set(0f)
            meanBrightness.set(0f)
            return
        }

        val averageColorFromRgb = ColorHelper.colorFromRgb(
                totalRed / totalLights,
                totalGreen / totalLights,
                totalBlue / totalLights)

        val averageColorFromRgbWithFullSat = ColorHelper.colorFromRgb(
                totalRedWithFullSat / totalLights,
                totalGreenWithFullSat / totalLights,
                totalBlueWithFullSat / totalLights)


        val hueOfAverageColorFromRgb = ColorHelper.hueFromColor(averageColorFromRgb)
        val satOfAverageColorFromRgb = ColorHelper.saturationFromColor(averageColorFromRgb)
        val brightnessOfAverageColorFromRgb = ColorHelper.valueBrightnessFromColor(averageColorFromRgb)

        val hueOfAverageColorFromRgbWithFullSat = ColorHelper.hueFromColor(averageColorFromRgbWithFullSat)

        meanHue.set(if (satOfAverageColorFromRgb < 0.01f) hueOfAverageColorFromRgbWithFullSat else hueOfAverageColorFromRgb)
        meanSaturation.set(satOfAverageColorFromRgb)
        meanBrightness.set(brightnessOfAverageColorFromRgb)
    }

    private fun refreshSecondaryText() {
        //TODO use strings file

        val totalLights = lightGroupController.lightsNotInSubgroup.size
        var lightsReachable = 0
        var lightsOn = 0

        lightGroupController.lightsNotInSubgroup.forEach {
            lightsReachable += if (it.isReachable.get() == true) 1 else 0
            lightsOn += if (it.isOn.stagedValueOrLastValueFromHub == true) 1 else 0
        }

        var secondaryInformationString = "$totalLights lights - "

        if (lightsReachable == 0) {
            secondaryInformationString += "all unreachable"
        } else if (lightsReachable < totalLights) {
            secondaryInformationString += "${totalLights - lightsReachable} unreachable - "
        }

        if (lightsReachable > 0) {
            secondaryInformationString += if (lightsOn == 0) "all off" else if (lightsOn == totalLights) "all on" else "$lightsOn on"
        }

        secondaryInformation.set(secondaryInformationString)
    }

    fun refreshListOfLightsToMatchController() {
        // ObservableArrayLists must be modified from the UI thread only:
        Handler(Looper.getMainLooper()).post {
            CollectionComparisonHelper.compareCollectionsAndIdentifyMissingElements(
                    individualLightViewModels,
                    lightGroupController.lightsInGroupOrSubgroups,
                    { lightViewModel, lightController ->
                        lightViewModel.lightController == lightController
                    },
                    {
                        individualLightViewModels.remove(it)
                    },
                    {
                        val lightViewModel = LightViewModel(it)
                        individualLightViewModels.add(lightViewModel)

                        // Setup group brightness, hue and saturation handles:
                        // All lights will always be shown in the group brightness slider (but will be in the 'off'
                        // position) if they are off.
                        // Lights will only be shown in the hue and saturation brightness sliders if they are on.
                        lightViewModel.lightController.isOn.stagedValueOrLastValueFromHubObservable.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                                onIsReachableOrIsOnChangedForIndividualLight(lightViewModel)
                            }
                        })
                        lightViewModel.lightController.isReachable.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                                onIsReachableOrIsOnChangedForIndividualLight(lightViewModel)
                            }
                        })
                        onIsReachableOrIsOnChangedForIndividualLight(lightViewModel)
                    },
                    { _, _ ->
                    }
            )
        }
    }

    override fun onExpandContractButtonClicked() {
        isExpanded.set(! (isExpanded.get() ?: false))
    }

    override fun onColorTabClicked() {
        isInColorMode.set(true)
    }

    override fun onWhiteTabClicked() {
        isInColorMode.set(false)
    }

}