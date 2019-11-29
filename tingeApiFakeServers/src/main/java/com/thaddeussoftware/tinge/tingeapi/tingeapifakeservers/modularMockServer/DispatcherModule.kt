package com.thaddeussoftware.tinge.tingeapi.tingeapifakeservers.modularMockServer

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.util.regex.PatternSyntaxException
import kotlin.RuntimeException
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

abstract class DispatcherModule {

    /**
     * Cached list of all functions in this instance which are annotated with [ServerPath]
     *
     * Setup by [setupAnnotatedFunctions]
     * */
    val annotatedFunctionsInInstance = ArrayList<AnnotatedFunctionToCall>()

    /**
     * This is called once to populate [annotatedFunctionsInInstance]
     * */
    fun setupAnnotatedFunctions() {
        annotatedFunctionsInInstance.clear()
        this::class.members.forEach { member ->
            member.annotations
                    .filterIsInstance<ServerPath>()
                    .forEach {  serverPathAnnotation ->

                        assertFunctionIsValidForAnnotation(member, serverPathAnnotation)

                        annotatedFunctionsInInstance.add(
                                AnnotatedFunctionToCall(
                                        this,
                                        serverPathAnnotation,
                                        member as KFunction<MockResponse>))
                    }
        }
    }

    /**
     * Asserts that a given function has a valid signature corresponding to a given [ServerPath]
     * annotation.
     *
     * If it does not, an exception is thrown describing what is wrong.
     * */
    private fun assertFunctionIsValidForAnnotation(member: KCallable<*>, annotation: ServerPath) {
        if (member.returnType.classifier != MockResponse::class) {
            val actualReturnTypeString = (member.returnType.classifier as? KClass<*>)?.simpleName ?: "(unknown)"
            throw RuntimeException("ServerPath-annotated function ${member.name} has return type ${actualReturnTypeString} - should be MockResponse")
        }

        val matchingPathRegex = try {
             Regex(annotation.matchingPathRegex)
        } catch (e: PatternSyntaxException) {
            throw RuntimeException("ServerPath annotation for function ${member.name} has invalid Regex", e)
        }

        var doParametersMatch = true
        val totalGroupCount = matchingPathRegex.toPattern().matcher("").groupCount()

        if (member.parameters.size - 2 != totalGroupCount) {
            doParametersMatch = false
        } else {
            member.parameters.forEachIndexed { index, kParameter ->
                if (index == 0) {
                    // Do nothing when index == 0 as this index corresponds to the instance that the
                    // function is on (the DispatcherModule instance) rather than an actual parameter.
                    return@forEachIndexed
                }
                if (index == 1 && kParameter.type.classifier != RecordedRequest::class) {
                    doParametersMatch = false
                }
                if (index > 1 && kParameter.type.classifier != String::class) {
                    doParametersMatch = false
                }
            }
        }

        if (!doParametersMatch) {
            val stringBuilder = StringBuilder()

            stringBuilder.append("ServerPath-annotated function ${member.name} should take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n")
            stringBuilder.append("Regex pattern ${matchingPathRegex.pattern} has ${totalGroupCount} capturing groups\n")
            stringBuilder.append("Expected arguments (RecordedRequest")
            for (i in 0 until totalGroupCount - 1) {
                stringBuilder.append(", String")
            }
            stringBuilder.append(") - received arguments (")
            member.parameters.forEachIndexed { index, kParameter ->
                if (index == 0) {
                    // Do nothing when index == 0 as this index corresponds to the instance that the
                    // function is on (the DispatcherModule instance) rather than an actual parameter.
                    return@forEachIndexed
                }
                if (index > 1) stringBuilder.append(", ")
                stringBuilder.append((kParameter.type.classifier as? KClass<*>)?.simpleName ?: "(unknown)")
            }
            stringBuilder.append(")")

            throw RuntimeException(stringBuilder.toString())
        }
    }

    /**
     * Holds a reference to a function annotated with [ServerPath]
     * */
    class AnnotatedFunctionToCall(
            val dispatcherModule: DispatcherModule,
            serverPath: ServerPath,
            val function: KFunction<MockResponse>
    ) {
        val priorityIfMultiplePathsMatch = serverPath.priorityIfMultiplePathsMatch
        val matchingPathRegex = Regex(serverPath.matchingPathRegex)
    }
}