package com.time2key.modularmockserver.internal

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

/**
 * Wrapper around a function inside a [DispatcherModule] that is annotated with [ServerPath]
 * */
abstract class ServerPathAnnotatedFunction() {

    /**
     * The simple class name of the [DispatcherModule] subclass that this belongs to. This is
     * used to provide a helpful error message if something goes wrong.
     * */
    abstract val dispatcherModuleSimpleClassName: String

    /**
     * The simple name of the [ServerPath]-annotated function that this corresponds to. This is
     * used to provide a helpful error message if something goes wrong.
     * */
    abstract val simpleFunctionName: String

    abstract val matchingPathRegex: Regex

    /**
     * Higher number = higher priority, 0 is the default.
     * */
    abstract val priorityIfMultiplePathsMatch: Int

    abstract fun evaluateRequest(request: RecordedRequest): MockResponse
}