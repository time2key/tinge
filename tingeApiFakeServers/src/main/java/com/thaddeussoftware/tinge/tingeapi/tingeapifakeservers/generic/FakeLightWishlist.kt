package com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.generic

/**
 * A wishlist / descriptor for a [FakeLight] to be added to a [FakeServer] with
 * [FakeServer.addFakeLightToServer].
 *
 * This allows a [FakeLight] to be described without having to know about the implementation of the
 * [FakeServer] it is being added to.
 *
 * Any properties not set will use default values - defaults to On / Full brightness / White.
 *
 * This base class only contains properties that are generic to any fake light regardless of
 * manufacturer. System / manufacturer-specific subclasses of [FakeLightWishlist] will exist to
 * allow system - specific properties to be defined.
 * */
class FakeLightWishlist {

    val displayName: String = ""

    val isReachable: Boolean = false

    /**
     * Brightness from 0 - 1
     * */
    var brightness: Float = 1f

    var isOn: Boolean = true

    /**
     * Hue from 0 - 1
     * */
    var hue: Float = 0f

    /**
     * Saturation from 0 - 1
     * */
    var saturation: Float = 0f


}