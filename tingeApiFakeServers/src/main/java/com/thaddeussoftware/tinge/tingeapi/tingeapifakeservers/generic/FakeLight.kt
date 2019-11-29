package com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.generic

/**
 * Represents a fake light in a [FakeServer] and holds all the current server state of the light.
 * */
abstract class FakeLight(
        internal val fakeLightWishlist: FakeLightWishlist
) {

    var displayName = fakeLightWishlist.displayName

    var isReachable = fakeLightWishlist.isReachable

    /**
     * Brightness from 0 - 1
     * */
    var brightness = fakeLightWishlist.brightness

    var isOn = fakeLightWishlist.isOn

    /**
     * Hue from 0 - 1
     * */
    var hue = fakeLightWishlist.hue

    /**
     * Saturation from 0 - 1
     * */
    var saturation = fakeLightWishlist.saturation
}