package com.time2key.modularmockserver

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.lang.RuntimeException

class MultiModuleDispatcher: Dispatcher() {

    private val dispatcherModules = ArrayList<DispatcherModule>()

    fun addModule(dispatcherModule: DispatcherModule) {
        dispatcherModule.setupAnnotatedFunctions()
        dispatcherModules.add(dispatcherModule)
    }


    override fun dispatch(request: RecordedRequest): MockResponse {

        val highestPriorityMatchingCalls =  ArrayList<DispatcherModule.AnnotatedFunctionToCall>()
        var highestPriorityFound: Int? = null

        dispatcherModules.forEach { dispatcherModule ->
            dispatcherModule.annotatedFunctionsInInstance.forEach { annotatedFunction ->
                if (annotatedFunction.matchingPathRegex.matches(request.path)) {
                    if (annotatedFunction.priorityIfMultiplePathsMatch == highestPriorityFound
                            || highestPriorityFound == null) {
                        // Priority of found call is same as current priority
                        highestPriorityMatchingCalls.add(annotatedFunction)
                        highestPriorityFound = annotatedFunction.priorityIfMultiplePathsMatch
                    } else if (annotatedFunction.priorityIfMultiplePathsMatch > highestPriorityFound!!) {
                        // Priority of found call is greater than current priority
                        highestPriorityMatchingCalls.clear()
                        highestPriorityMatchingCalls.add(annotatedFunction)
                        highestPriorityFound = annotatedFunction.priorityIfMultiplePathsMatch
                    }
                }
            }
        }

        if (highestPriorityMatchingCalls.size == 0) {
            val stringBuilder = StringBuilder()
            stringBuilder.append("No modules have been added matching call with path ${request.path}\n")

            stringBuilder.append("There are ${dispatcherModules.size} modules added:\n")
            dispatcherModules.forEach { dispatcherModule ->
                stringBuilder.append(dispatcherModule::class.qualifiedName)
                stringBuilder.append("\n")
            }

            throw RuntimeException(stringBuilder.toString())
        } else if (highestPriorityMatchingCalls.size > 1) {
            val stringBuilder = StringBuilder()
            stringBuilder.append("Multiple matches found for path ${request.path}\n")

            stringBuilder.append("There are ${highestPriorityMatchingCalls.size} matches:\n")
            highestPriorityMatchingCalls.forEach { annotatedFunction ->
                stringBuilder.append("fun ")
                stringBuilder.append(annotatedFunction.function.name)
                stringBuilder.append("() - ")
                stringBuilder.append(annotatedFunction.matchingPathRegex.pattern)
            }

            throw RuntimeException(stringBuilder.toString())
        } else {
            return highestPriorityMatchingCalls.first().function.call(
                    highestPriorityMatchingCalls.first().dispatcherModule,
                    request)
        }
    }
}