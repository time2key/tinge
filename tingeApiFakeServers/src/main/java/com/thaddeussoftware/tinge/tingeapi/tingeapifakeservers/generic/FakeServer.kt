package com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.generic

/**
 * Represents a fake server.
 * */
abstract class FakeServer {

    abstract val isServerStarted: Boolean

    /**
     * Adds a fake light to this fake server.
     *
     * @param fakeLightDescriptor
     * Describes the fake light to add. This can directly be of type [FakeLightDescriptor], or
     * it can be of a subtype specific to the type of fake server this is.
     *
     * @return
     * The fake light instance that was added.
     * */
    abstract fun addFakeLightToServer(fakeLightDescriptor: FakeLightDescriptor): FakeLight
}