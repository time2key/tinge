package com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller

import androidx.databinding.ObservableField

/**
 * Represents an observable event that happens on a controller.
 * For example, there could be an event when any property of a light being changed.
 * */
class ControllerLiveEvent {

    /**
     * Called when this event has been staged to happen.
     *
     * *Hubs should not call this directly, they should call [onEventStagedToHappen]*
     *
     * For example, if this event corresponded with any property of a light being changed, this
     * would be called when any value was staged to be changed.
     *
     * Note that sometimes [valueFromHubUpdatedLiveEvent] can be called for a change without this
     * ever being called - this can happen if a change happened remotely which was never staged. If
     * you want to ensure that you get every change, you should subscribe to
     * [stagedValueOrValueFromHubUpdatedLiveEvent].
     * */
    val stagedValueUpdatedLiveEvent = ObservableField<Any>()

    /**
     * Called when this event has actually happened on the hub.
     *
     * *Hubs should not call this directly, they should call [onEventHappenedOnHub]*
     *
     * For example, if this event corresponded with any property of a light being changed, this
     * would be called when any value was actually updated on the hub.
     * */
    val valueFromHubUpdatedLiveEvent = ObservableField<Any>()

    /**
     * Called when this event has been staged to happen, and when it has actually happened on the
     * hub.
     *
     * *Hubs should not call this directly, they should call [onEventStagedToHappen] or
     * [onEventHappenedOnHub]*
     * */
    val stagedValueOrValueFromHubUpdatedLiveEvent = ObservableField<Any>()


    /**
     * Hubs should call this when this event has been staged to happen.
     * */
    fun onEventStagedToHappen() {
        stagedValueUpdatedLiveEvent.set(0f)
        stagedValueOrValueFromHubUpdatedLiveEvent.set(0f)
    }

    /**
     * Hubs should call this when this event has actually happened on the hub.
     * */
    fun onEventHappenedOnHub() {
        valueFromHubUpdatedLiveEvent.set(0f)
        stagedValueOrValueFromHubUpdatedLiveEvent.set(0f)
    }
}