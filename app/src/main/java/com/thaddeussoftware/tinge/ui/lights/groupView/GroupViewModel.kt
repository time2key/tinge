package com.thaddeussoftware.tinge.ui.lights.groupView

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableArrayList
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightGroupController
import com.thaddeussoftware.tinge.helpers.CollectionComparisonHelper
import com.thaddeussoftware.tinge.ui.lights.lightView.LightViewModel
import io.reactivex.android.schedulers.AndroidSchedulers

class GroupViewModel(
        val lightGroupController: LightGroupController
): ViewModel() {

    /**
     * View models of each of the lights that should be shown on screen
     * */
    var individualLightViewModels = ObservableArrayList<LightViewModel>()

    init {
        refreshListOfLightsToMatchController()
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
}