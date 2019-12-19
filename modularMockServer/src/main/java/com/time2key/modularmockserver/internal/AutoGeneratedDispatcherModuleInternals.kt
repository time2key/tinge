package com.time2key.modularmockserver.internal

import com.time2key.modularmockserver.DispatcherModule

/**
 * Base class for the internals of a dispatcher module that is auto generated by
 * [ServerPathAnnotationProcessor].
 *
 * Each [DispatcherModule] in the project will have one of these classes auto generated for it,
 * which will then be accessed through [BaseAutoGeneratedClassMapper].
 * */
abstract class AutoGeneratedDispatcherModuleInternals {

    abstract fun getServerPathAnnotatedFunctions(dispatcherModule: DispatcherModule): ArrayList<ServerPathAnnotatedFunction>

    /**
     * A [ServerPathAnnotatedFunction] that has been auto generated at compile time.
     * */
    protected abstract class AutoGeneratedServerPathAnnotatedFunction(
            override val dispatcherModuleSimpleClassName: String,
            override val simpleFunctionName: String,
            matchingPathRegexString: String,
            override val priorityIfMultiplePathsMatch: Int
    ): ServerPathAnnotatedFunction() {

        override val matchingPathRegex = Regex(matchingPathRegexString)

    }
}