package com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller

import androidx.databinding.Observable
import androidx.databinding.ObservableField

/**
 * Represents a property of any type on the Tinge API, such as hue of a light etc.
 *
 * Stores the [lastValueRetrievedFromHub], as well as the [stagedValue].
 *
 * @param T
 * The type of value stored in this property.
 *
 * @param onValueStaged
 * Lambda that will be notified when a value is staged on this instance.
 *
 * This can be useful to set up a [ControllerInternalStageableProperty] that applies to other
 * properties also, so that when a value is staged on one property, other properties are updated.
 * */
class ControllerInternalStageableProperty<T>(
        initialValueReturnedFromHub: T? = null,
        private val onValueStaged: ((newValue: T?) -> Unit)? = null,
        private val onValueUpdatedFromHub: ((newValue: T?) -> Unit)? = null
) {

    /**
     * The last value for this property that has been applied to the hub.
     *
     * Automatically kept in sync with [lastValueRetrievedFromHubObservable].
     * */
    var lastValueRetrievedFromHub: T? = initialValueReturnedFromHub
        private set

    /**
     * The cached value for this property that has not been uploaded to the hub yet, or null if
     * uploaded.
     *
     * Automatically in sync with [stagedValueObservable].
     * */
    var stagedValue: T? = null
        private set

    /**
     * [stagedValue] if it exists, otherwise [lastValueRetrievedFromHub]
     *
     * Automatically kept in sync with [stagedValueOrLastValueFromHubObservable].
     * */
    val stagedValueOrLastValueFromHub: T?
        get() = stagedValue ?: lastValueRetrievedFromHub

    /**
     * Observable version of [lastValueRetrievedFromHub], so you can subscribe to changes in this
     * property.
     *
     * Automatically kept in sync with
     * */
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

    fun setValueRetrievedFromHub(value: T?) {
        lastValueRetrievedFromHub = value
        if (stagedValue == lastValueRetrievedFromHub) { stagedValue = null }
        updateObservableValuesToMatchActualValues()
        onValueUpdatedFromHub?.invoke(value)
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