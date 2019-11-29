package com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.modularMockServer

import org.intellij.lang.annotations.RegExp

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ServerPath(
        @RegExp val matchingPathRegex: String,
        /**
         * Higher number = higher priority, 0 is the default.
         * */
        val priorityIfMultiplePathsMatch: Int = 0
) {

}