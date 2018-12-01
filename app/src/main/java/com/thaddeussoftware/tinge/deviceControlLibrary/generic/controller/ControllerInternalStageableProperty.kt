package com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller

import android.databinding.Observable
import android.databinding.ObservableField

/**
 * Cachable property that classes implementing controllers ([HubController] etc) can use internally
 * to keep track of the values of properties which are staged.
 *
 * @param T
 * The type of value stored in this property.
 *
 * @param onValueStaged
 * Lambda that will be notified when a value is staged on this instance.
 *
 * This can be useful to set up a ControllerInternalStageableProperty that applies to a whole
 * group of other ControllerInternalStageableProperties, so that when a value is staged on the
 * group property, it is automatically staged on all other properties.
 * */
class ControllerInternalStageableProperty<T>(
        initialValueReturnedFromHub: T? = null,
        private val onValueStaged: ((newValue: T?) -> Unit)? = null
) {

    var lastValueRetrievedFromHub: T? = initialValueReturnedFromHub
        private set

    var stagedValue: T? = null
        private set

    val stagedValueOrLastValueFromHub: T?
        get() = stagedValue ?: lastValueRetrievedFromHub

    fun setStagedValueApplied() {
        lastValueRetrievedFromHub = stagedValueOrLastValueFromHub
        stagedValue = null
        updateObservableValuesToMatchActualValues()
    }

    fun setValueRetrievedFromHub(value: T?, discardStagedValueIfPresent: Boolean = false) {
        lastValueRetrievedFromHub = value
        if (discardStagedValueIfPresent) { stagedValue = null }
        updateObservableValuesToMatchActualValues()
    }

    fun stageValue(value: T) {
        stagedValue = value
        updateObservableValuesToMatchActualValues()
        onValueStaged?.invoke(value)
    }

    fun discardStagedValue() {
        stagedValue = null
        updateObservableValuesToMatchActualValues()
        onValueStaged?.invoke(null)
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