package com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.generic

/**
 * A wishlist / descriptor for a [FakeLight] to be added to a [FakeServer] with
 * [FakeServer.addFakeLightToServer].
 *
 * This allows a [FakeLight] to be described without having to know about the implementation of the
 * [FakeServer] it is being added to.
 *
 * Any properties not set will use default values.
 *
 * This base class only contains properties that are generic to any fake light regardless of
 * manufacturer. System / manufacturer-specific subclasses of [FakeLightWishlist] will exist to
 * allow system - specific properties to be defined.
 * */
class FakeLightWishlist {

    val displayName: String = ""

    val isReachable: Boolean = false
}