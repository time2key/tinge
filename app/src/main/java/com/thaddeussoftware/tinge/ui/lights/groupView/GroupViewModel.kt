package com.thaddeussoftware.tinge.ui.lights.groupView

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModel
import android.databinding.Observable
import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import android.graphics.Color
import android.view.View
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.ControllerInternalStageableProperty
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightGroupController
import com.thaddeussoftware.tinge.helpers.CollectionComparisonHelper
import com.thaddeussoftware.tinge.helpers.UiHelper
import com.thaddeussoftware.tinge.ui.lights.lightView.LightViewModel
import io.reactivex.android.schedulers.AndroidSchedulers

class GroupViewModel(
        val lightGroupController: LightGroupController
): ViewModel() {

    val displayName =lightGroupController.name.stagedValueOrLastValueFromHubObservable

    val uniformBrightnessOrNull = lightGroupController.uniformBrightnessOfAllLightsInGroupOrNull.stagedValueOrLastValueFromHubObservable
    val meanBrightness = lightGroupController.averageBrightnessOfAllLightsInGroup.stagedValueOrLastValueFromHubObservable

    val uniformHueOrNull = lightGroupController.uniformHueOfAllLightsInGroupOrNull.stagedValueOrLastValueFromHubObservable
    val meanHue = lightGroupController.averageHueOfAllLightsInGroup.stagedValueOrLastValueFromHubObservable

    val uniformSaturationOrNull = lightGroupController.uniformSaturationOfAllLightsInGroupOrNull.stagedValueOrLastValueFromHubObservable
    val meanSaturation = lightGroupController.averageSaturationOfAllLightsInGroup.stagedValueOrLastValueFromHubObservable

    val isExpanded = ObservableField<Boolean>(false)

    /**
     * View models of each of the lights that should be shown on screen
     * */
    var individualLightViewModels = ObservableArrayList<LightViewModel>()

    val colorForBackgroundView = ObservableField<Int>(0)

    init {
        refreshListOfLightsToMatchController()


        meanBrightness.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                setupColorForBackgroundView()
            }
        })
        meanSaturation.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                setupColorForBackgroundView()
            }
        })
        meanHue.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                setupColorForBackgroundView()
            }
        })
        setupColorForBackgroundView()
    }

    private fun setupColorForBackgroundView() {
        colorForBackgroundView.set(
                UiHelper.getFadedBackgroundColourFromLightColour(
                        meanHue.get(), meanSaturation.get(), meanBrightness.get()))
    }

    private fun getColorFromHsv(h:Float, s:Float, v:Float) = Color.HSVToColor(floatArrayOf(h*360f, s, v))

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

    fun onExpandContractButtonClicked(view: View) {
        isExpanded.set(! (isExpanded.get() ?: false))
    }

    fun onBrightnessSliderChanged(newValue:Float) {
        uniformBrightnessOrNull.set(newValue)
        meanBrightness.set(newValue)
        applyChanges()
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