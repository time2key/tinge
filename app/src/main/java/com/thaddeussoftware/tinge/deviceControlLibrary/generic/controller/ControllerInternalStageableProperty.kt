package com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller

import android.databinding.Observable
import android.databinding.ObservableField

/**
 * Cachable property that classes implementing controllers ([HubController] etc) can use internally
 * to keep track of the values of properties which are staged.
 *
 * @param T
 * The type of value stored in this property.
 * */
class ControllerInternalStageableProperty<T>(
        initialValueReturnedFromHub: T? = null
) {

    var lastValueRetrievedFromHub: T? = initialValueReturnedFromHub
        private set

    var stagedValue: T? = null
        private set

    val stagedValueOrLastValueFromHub: T?
        get() = stagedValue ?: lastValueRetrievedFromHub


    fun setValueRetrievedFromHub(value: T?, discardStagedValueIfPresent: Boolean = false) {
        lastValueRetrievedFromHub = value
        if (discardStagedValueIfPresent) { stagedValue = null }
        updateObservableValuesToMatchActualValues()
    }

    fun stageValue(value: T) {
        stagedValue = value
        updateObservableValuesToMatchActualValues()
    }

    fun discardStagedValue() {
        stagedValue = null
        updateObservableValuesToMatchActualValues()
    }




    val lastValueRetrievedFromHubObservable = ObservableField<T?>(initialValueReturnedFromHub)

    val stagedValueObservable = ObservableField<T?>()

    val stagedValueOrLastValueFromHubObservable = ObservableField<T?>(initialValueReturnedFromHub)

    private var observableValuesAreBeingManuallyChangedIgnoreCallbacks = false


    init {
        stagedValueObservable.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (observableValuesAreBeingManuallyChangedIgnoreCallbacks) return
                if (stagedValue != stagedValueObservable.get() && stagedValueObservable.get() != null) {
                    stageValue(stagedValueObservable.get()!!)
                }
            }
        })

        stagedValueOrLastValueFromHubObservable.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (observableValuesAreBeingManuallyChangedIgnoreCallbacks) return
                if (stagedValue != stagedValueOrLastValueFromHubObservable.get() && stagedValueOrLastValueFromHubObservable.get() != null) {
                    stageValue(stagedValueOrLastValueFromHubObservable.get()!!)
                }
            }
        })

        lastValueRetrievedFromHubObservable.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (observableValuesAreBeingManuallyChangedIgnoreCallbacks) return
                if (lastValueRetrievedFromHub != lastValueRetrievedFromHubObservable.get()) {
                    setValueRetrievedFromHub(lastValueRetrievedFromHubObservable.get()!!)
                }
            }
        })
    }

    private fun updateObservableValuesToMatchActualValues() {
        observableValuesAreBeingManuallyChangedIgnoreCallbacks = true

        if (lastValueRetrievedFromHubObservable.get() != lastValueRetrievedFromHub) {
            lastValueRetrievedFromHubObservable.set(lastValueRetrievedFromHub)
        }
        if (stagedValueObservable.get() != stagedValue) {
            stagedValueObservable.set(stagedValue)
        }
        if (stagedValueOrLastValueFromHubObservable.get() != stagedValueOrLastValueFromHub) {
            stagedValueOrLastValueFromHubObservable.set(stagedValueOrLastValueFromHub)
        }

        observableValuesAreBeingManuallyChangedIgnoreCallbacks = false
    }

}