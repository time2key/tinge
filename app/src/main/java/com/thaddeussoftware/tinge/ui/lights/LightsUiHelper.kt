package com.thaddeussoftware.tinge.ui.lights

import android.databinding.Observable
import android.databinding.ObservableField
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.ControllerInternalStageableProperty

object LightsUiHelper {

    fun bindObservableBrightnessViewModelPropertyToController(
            brightnessViewModelProperty: ObservableField<Float?>,
            isOnStageableProperty: ControllerInternalStageableProperty<Boolean?>,
            brightnessStageableProperty: ControllerInternalStageableProperty<Float?>
    ) {

        // When UI property changed, controller should be updated:
        brightnessViewModelProperty.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            var isPropertyBeingChangedByCode = false

            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (isPropertyBeingChangedByCode) return
                isPropertyBeingChangedByCode = true

                val value = brightnessViewModelProperty.get()
                if (value == null) {
                    // Do nothing
                } else if (value < 0) {
                    isOnStageableProperty.stageValue(false)
                } else if (value >= 0f){
                    isOnStageableProperty.stageValue(true)
                    brightnessStageableProperty.stageValue(value?:0f)
                }
                isPropertyBeingChangedByCode = false
            }
        })

        // When controller isOn property changed, UI property should be updated:
        isOnStageableProperty.lastValueRetrievedFromHubObservable.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (isOnStageableProperty.lastValueRetrievedFromHub == false) {
                    brightnessViewModelProperty.set(-1f)
                } else if (isOnStageableProperty.lastValueRetrievedFromHub == true) {
                    brightnessViewModelProperty.set(brightnessStageableProperty.stagedValueOrLastValueFromHub)
                } else {
                    brightnessViewModelProperty.set(null)
                }
            }
        })

        // When controller brightness property changed, UI property should be updated:
        brightnessStageableProperty.lastValueRetrievedFromHubObservable.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (isOnStageableProperty.lastValueRetrievedFromHub == false) {
                    brightnessViewModelProperty.set(-1f)
                } else if (isOnStageableProperty.lastValueRetrievedFromHub == true) {
                    brightnessViewModelProperty.set(brightnessStageableProperty.stagedValueOrLastValueFromHub)
                } else {
                    brightnessViewModelProperty.set(null)
                }
            }
        })

        brightnessViewModelProperty.set( if (isOnStageableProperty.stagedValueOrLastValueFromHub == false) -1f else if (isOnStageableProperty.stagedValueOrLastValueFromHub == true) brightnessStageableProperty.stagedValueOrLastValueFromHub else null)
    }

}