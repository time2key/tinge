package com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.generic

/**
 * Describes a [FakeLight] to be added to a [FakeServer].
 *
 * This base class contains properties that are generic to any fake light in any fake server
 * (regardless of manufacturer).
 *
 * System-specific fake light classes will exist that allow system-specific properties
 * to be defined.
 * */
class FakeLightDescriptor {

    val displayName: String = ""

    val isReachable: Boolean = false
}