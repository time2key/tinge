package com.thaddeussoftware.tinge.ui.lights

import android.databinding.Observable
import android.databinding.ObservableField
import android.util.Log
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
                if (isPropertyBeingChangedByCode) {
                    Log.v("tinge", "brightness slider changed, but isPropertyBeingChangedByCode is true so ignoring")
                    return
                }
                isPropertyBeingChangedByCode = true

                val value = brightnessViewModelProperty.get()
                if (value == null) {
                    // Do nothing
                    Log.v("tinge", "LightsUiHelper brightness slider changed to null ???")
                } else if (value < 0) {
                    isOnStageableProperty.stageValue(false)
                    Log.v("tinge", "LightsUiHelper brightness slider changed to off, setting light controller off ")
                } else if (value >= 0f){
                    Log.v("tinge", "LightsUiHelper brightness slider changed, setting light controller brightness to: "+value)
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
                    Log.v("tinge", "LightsUiHelper controller isOn set to false, updating slider")
                    brightnessViewModelProperty.set(-1f)
                } else if (isOnStageableProperty.lastValueRetrievedFromHub == true) {
                    Log.v("tinge", "LightsUiHelper controller isOn set to true, updating slider")
                    brightnessViewModelProperty.set(brightnessStageableProperty.stagedValueOrLastValueFromHub)
                } else {
                    Log.v("tinge", "LightsUiHelper controller isOn set to null ???")
                    brightnessViewModelProperty.set(null)
                }
            }
        })

        // When controller brightness property changed, UI property should be updated:
        brightnessStageableProperty.lastValueRetrievedFromHubObservable.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (isOnStageableProperty.lastValueRetrievedFromHub == false) {
                    Log.v("tinge", "LightsUiHelper controller brightness changed, isOn is false, updating slider")
                    brightnessViewModelProperty.set(-1f)
                } else if (isOnStageableProperty.lastValueRetrievedFromHub == true) {
                    Log.v("tinge", "LightsUiHelper controller brightness changed, isOn is true, updating slider")
                    brightnessViewModelProperty.set(brightnessStageableProperty.stagedValueOrLastValueFromHub)
                } else {
                    Log.v("tinge", "LightsUiHelper controller brightness changed, isOn is null ???")
                    brightnessViewModelProperty.set(null)
                }
            }
        })

        brightnessViewModelProperty.set( if (isOnStageableProperty.stagedValueOrLastValueFromHub == false) -1f else if (isOnStageableProperty.stagedValueOrLastValueFromHub == true) brightnessStageableProperty.stagedValueOrLastValueFromHub else null)
    }

}