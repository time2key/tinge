package com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller

import android.databinding.ObservableField

abstract class ObservableFieldSpy<T>(private val observableFieldSpiedOn: ObservableField<T>): ObservableField<T>() {

    override fun get(): T? {
        return observableFieldSpiedOn.get()
    }

    override fun set(value: T) {
        observableFieldSpiedOn.set(value)
        afterObservableValueChanged(value)
    }

    abstract fun afterObservableValueChanged(newValue: T)
}