package com.time2key.modularmockserver.reflectionTests

import com.time2key.modularmockserver.DispatcherModule
import com.time2key.modularmockserver.MultiModuleDispatcher
import com.time2key.modularmockserver.ServerPath
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.lang.RuntimeException

class IncorrectlyAnnotatedFunctionsTests {

    var multiModuleDispatcher = MultiModuleDispatcher()

    @Before
    fun setup() {
        multiModuleDispatcher.canUseReflectionFallback = true
    }

    @Test
    fun invalidRegex_correctFailure() {
        // Arrange:
        class Module: DispatcherModule() {
            @ServerPath(".*(")
            fun functionWithInvalidRegex(request: RecordedRequest): MockResponse {
                return MockResponse()
            }
        }

        // Act & Assert:
        try {
            multiModuleDispatcher.addModule(Module())

            fail("Exception should have been thrown")
        } catch (e: RuntimeException) {
            assertEquals(
                    "ServerPath annotation for function functionWithInvalidRegex has invalid Regex",
                    e.message)
        }
    }

    @Test
    fun noCapturingGroups_noParameters_correctFailure() {
        // Arrange:
        class Module: DispatcherModule() {
            @ServerPath(".*")
            fun functionMissingRecordedRequest(): MockResponse {
                return MockResponse()
            }
        }

        // Act & Assert:
        try {
            multiModuleDispatcher.addModule(Module())

            fail("Exception should have been thrown")
        } catch (e: RuntimeException) {
            assertEquals(
                    "ServerPath-annotated function functionMissingRecordedRequest must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                            "Regex pattern .* has 0 capturing groups\n" +
                            "Expected arguments (RecordedRequest) - received arguments ()",
                    e.message)
        }
    }

    @Test
    fun noCapturingGroups_wrongParameter_correctFailure() {
        // Arrange:
        class Module: DispatcherModule() {
            @ServerPath(".*")
            fun functionMissingRecordedRequest(stringArgument: String): MockResponse {
                return MockResponse()
            }
        }

        // Act & Assert:
        try {
            multiModuleDispatcher.addModule(Module())

            fail("Exception should have been thrown")
        } catch (e: RuntimeException) {
            assertEquals(
                    "ServerPath-annotated function functionMissingRecordedRequest must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                            "Regex pattern .* has 0 capturing groups\n" +
                            "Expected arguments (RecordedRequest) - received arguments (String)",
                    e.message)
        }
    }

    @Test
    fun noCapturingGroups_extraParameter_correctFailure() {
        // Arrange:
        class Module: DispatcherModule() {
            @ServerPath(".*")
            fun functionMissingRecordedRequest(request: RecordedRequest, extraParameter: String): MockResponse {
                return MockResponse()
            }
        }

        // Act & Assert:
        try {
            multiModuleDispatcher.addModule(Module())

            fail("Exception should have been thrown")
        } catch (e: RuntimeException) {
            assertEquals(
                    "ServerPath-annotated function functionMissingRecordedRequest must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                            "Regex pattern .* has 0 capturing groups\n" +
                            "Expected arguments (RecordedRequest) - received arguments (RecordedRequest, String)",
                    e.message)
        }
    }
}