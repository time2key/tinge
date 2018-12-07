package com.thaddeussoftware.tinge.ui.lights.groupView

import android.annotation.SuppressLint
import android.databinding.Observable
import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import android.view.View
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightGroupController
import com.thaddeussoftware.tinge.helpers.CollectionComparisonHelper
import com.thaddeussoftware.tinge.helpers.UiHelper
import com.thaddeussoftware.tinge.ui.lights.InnerLightViewModel
import com.thaddeussoftware.tinge.ui.lights.lightView.LightViewModel
import io.reactivex.android.schedulers.AndroidSchedulers

class GroupViewModel(
        val lightGroupController: LightGroupController
): InnerLightViewModel() {
    override val isColorSupported = ObservableField<Boolean>(true)

    override val isInColorMode = ObservableField<Boolean?>(true)

    override val hue = lightGroupController.uniformHueOfAllLightsInGroupOrNull.stagedValueOrLastValueFromHubObservable
    override val saturation = lightGroupController.uniformSaturationOfAllLightsInGroupOrNull.stagedValueOrLastValueFromHubObservable
    override val brightness = lightGroupController.uniformBrightnessOfAllLightsInGroupOrNull.stagedValueOrLastValueFromHubObservable


    override val whiteTemperature = ObservableField<Float?>(0f)

    override val colorForPreviewImageView = ObservableField<Int>(0xffffffff.toInt())

    override val displayName = lightGroupController.name.stagedValueOrLastValueFromHubObservable

    override val secondaryInformation = ObservableField<String?>("")

    val meanBrightness = lightGroupController.averageBrightnessOfAllLightsInGroup.stagedValueOrLastValueFromHubObservable

    val meanHue = lightGroupController.averageHueOfAllLightsInGroup.stagedValueOrLastValueFromHubObservable

    val meanSaturation = lightGroupController.averageSaturationOfAllLightsInGroup.stagedValueOrLastValueFromHubObservable

    override val isExpanded = ObservableField<Boolean>(false)

    override val colorForBackgroundView = ObservableField<Int>(0)



    /**
     * View models of each of the lights that should be shown on screen
     * */
    var individualLightViewModels = ObservableArrayList<LightViewModel>()

    init {
        refreshListOfLightsToMatchController()


        meanBrightness.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                setupColorForBackgroundView()
                refreshSecondaryText()
            }
        })
        meanSaturation.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                setupColorForBackgroundView()
                refreshSecondaryText()
            }
        })
        meanHue.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                setupColorForBackgroundView()
                refreshSecondaryText()
            }
        })

        hue.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                applyChanges()
            }
        })
        saturation.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                applyChanges()
            }
        })
        brightness.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                applyChanges()
            }
        })

        setupColorForBackgroundView()
    }

    private fun setupColorForBackgroundView() {
        colorForBackgroundView.set(
                UiHelper.getFadedBackgroundColourFromLightColour(
                        meanHue.get(), meanSaturation.get(), meanBrightness.get()))
    }

    private fun refreshSecondaryText() {
        //TODO use strings file

        val totalLights = lightGroupController.lightsNotInSubgroup.size
        var lightsReachable = 0
        var lightsOn = 0

        lightGroupController.lightsNotInSubgroup.forEach {
            lightsReachable += if (it.isReachable) 1 else 0
            lightsOn += if (it.isOn.stagedValueOrLastValueFromHub == true) 1 else 0
        }

        var secondaryInformationString = "$totalLights lights - "

        if (lightsReachable < totalLights) {
            secondaryInformationString += "${totalLights - lightsReachable} unreachable - "
        }

        secondaryInformationString += if (lightsOn == 0) "all off" else if (lightsOn == totalLights) "all on" else "$lightsOn on"

        secondaryInformation.set(secondaryInformationString)
    }

    fun refreshListOfLightsToMatchController() {
        CollectionComparisonHelper.compareCollectionsAndIdentifyMissingElements(
                individualLightViewModels,
                lightGroupController.lightsInGroupOrSubgroups,
                { lightViewModel, lightController ->
                    lightViewModel.lightController == lightController
                },
                {
                    //if (it.lightController.hubController == hubController) {
                    individualLightViewModels.remove(it)
                    //}
                },
                {
                    individualLightViewModels.add(LightViewModel(it))
                },
                { lightViewModel, _ ->
                    lightViewModel.refreshToMatchController()
                }
        )
        refreshSecondaryText()
    }

    @SuppressLint("CheckResult")
    fun networkRefreshListOfLights() {
        lightGroupController
                .refresh(LightGroupController.DataInGroupType.LIGHTS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    refreshListOfLightsToMatchController()
                }
    }

    override fun onExpandContractButtonClicked(view: View) {
        isExpanded.set(! (isExpanded.get() ?: false))
    }

    override fun onBrightnessSliderChanged(newValue:Float) {
        brightness.set(newValue)
        meanBrightness.set(newValue)
    }

    override fun onHueSliderChanged(newValue: Float) {
        hue.set(newValue)
        meanHue.set(newValue)
    }

    override fun onSaturationSliderChanged(newValue: Float) {
        saturation.set(newValue)
        meanSaturation.set(newValue)
    }

    override fun onWhiteSliderChanged(newValue: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onColorTabClicked(view: View) {
        isInColorMode.set(true)
    }

    override fun onWhiteTabClicked(view: View) {
        isInColorMode.set(false)
    }


    private fun applyChanges() {
        lightGroupController.applyChanges().subscribe(
                {
                },
                {
                }
        )
    }
}