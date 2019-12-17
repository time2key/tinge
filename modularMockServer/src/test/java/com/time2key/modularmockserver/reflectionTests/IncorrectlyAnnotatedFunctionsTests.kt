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



    // region General tests

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


    // endregion



    //region Wrong visibility modifier tests

    @Test
    fun privateVisibility_correctFailure() {
        // Arrange:
        class Module: DispatcherModule() {
            @ServerPath(".*")
            private fun functionWithPrivateVisibility(request: RecordedRequest): MockResponse {
                return MockResponse()
            }
        }

        // Act & Assert:
        try {
            multiModuleDispatcher.addModule(Module())

            fail("Exception should have been thrown")
        } catch (e: RuntimeException) {
            assertEquals(
                    "ServerPath-annotated function functionWithPrivateVisibility must have public visibility",
                    e.message)
        }
    }

    @Test
    fun protectedVisibility_correctFailure() {
        // Arrange:
        class Module: DispatcherModule() {
            @ServerPath(".*")
            protected fun functionWithPrivateVisibility(request: RecordedRequest): MockResponse {
                return MockResponse()
            }
        }

        // Act & Assert:
        try {
            multiModuleDispatcher.addModule(Module())

            fail("Exception should have been thrown")
        } catch (e: RuntimeException) {
            assertEquals(
                    "ServerPath-annotated function functionWithPrivateVisibility must have public visibility",
                    e.message)
        }
    }

    @Test
    fun internalVisibility_correctFailure() {
        // Arrange:
        class Module: DispatcherModule() {
            @ServerPath(".*")
            internal fun functionWithPrivateVisibility(request: RecordedRequest): MockResponse {
                return MockResponse()
            }
        }

        // Act & Assert:
        try {
            multiModuleDispatcher.addModule(Module())

            fail("Exception should have been thrown")
        } catch (e: RuntimeException) {
            assertEquals(
                    "ServerPath-annotated function functionWithPrivateVisibility must have public visibility",
                    e.message)
        }
    }

    //endregion



    // region Testing regex with no capturing groups

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
    fun noCapturingGroups_extraStringParameter_correctFailure() {
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

    // endregion



    //region Testing regex with one capturing group

    @Test
    fun oneCapturingGroup_noParameters_correctFailure() {
        // Arrange:
        class Module: DispatcherModule() {
            @ServerPath("a*(b*)c*")
            fun functionMissingRecordedRequestAndString(): MockResponse {
                return MockResponse()
            }
        }

        // Act & Assert:
        try {
            multiModuleDispatcher.addModule(Module())

            fail("Exception should have been thrown")
        } catch (e: RuntimeException) {
            assertEquals(
                    "ServerPath-annotated function functionMissingRecordedRequestAndString must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                            "Regex pattern a*(b*)c* has 1 capturing groups\n" +
                            "Expected arguments (RecordedRequest, String) - received arguments ()",
                    e.message)
        }
    }

    @Test
    fun oneCapturingGroup_missingFirstCapturingGroupParameter_correctFailure() {
        // Arrange:
        class Module: DispatcherModule() {
            @ServerPath("a*(b*)c*")
            fun functionMissingString(recordedRequest: RecordedRequest): MockResponse {
                return MockResponse()
            }
        }

        // Act & Assert:
        try {
            multiModuleDispatcher.addModule(Module())

            fail("Exception should have been thrown")
        } catch (e: RuntimeException) {
            assertEquals(
                    "ServerPath-annotated function functionMissingString must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                            "Regex pattern a*(b*)c* has 1 capturing groups\n" +
                            "Expected arguments (RecordedRequest, String) - received arguments (RecordedRequest)",
                    e.message)
        }
    }

    @Test
    fun oneCapturingGroup_missingRecordedRequestParameter_correctFailure() {
        // Arrange:
        class Module: DispatcherModule() {
            @ServerPath("a*(b*)c*")
            fun functionMissingRecordedRequest(string: String): MockResponse {
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
                            "Regex pattern a*(b*)c* has 1 capturing groups\n" +
                            "Expected arguments (RecordedRequest, String) - received arguments (String)",
                    e.message)
        }
    }

    @Test
    fun oneCapturingGroup_extraStringParameter_correctFailure() {
        // Arrange:
        class Module: DispatcherModule() {
            @ServerPath("a*(b*)c*")
            fun functionWithExtraStringParameter(recordedRequest: RecordedRequest, group1: String, group2: String): MockResponse {
                return MockResponse()
            }
        }

        // Act & Assert:
        try {
            multiModuleDispatcher.addModule(Module())

            fail("Exception should have been thrown")
        } catch (e: RuntimeException) {
            assertEquals(
                    "ServerPath-annotated function functionWithExtraStringParameter must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                            "Regex pattern a*(b*)c* has 1 capturing groups\n" +
                            "Expected arguments (RecordedRequest, String) - received arguments (RecordedRequest, String, String)",
                    e.message)
        }
    }

    @Test
    fun oneCapturingGroup_wrongFirstParameter_correctFailure() {
        // Arrange:
        class Module: DispatcherModule() {
            @ServerPath("a*(b*)c*")
            fun functionWithStringInsteadOfRecordedRequest(recordedRequest: Int, group: String): MockResponse {
                return MockResponse()
            }
        }

        // Act & Assert:
        try {
            multiModuleDispatcher.addModule(Module())

            fail("Exception should have been thrown")
        } catch (e: RuntimeException) {
            assertEquals(
                    "ServerPath-annotated function functionWithStringInsteadOfRecordedRequest must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                            "Regex pattern a*(b*)c* has 1 capturing groups\n" +
                            "Expected arguments (RecordedRequest, String) - received arguments (Int, String)",
                    e.message)
        }
    }

    @Test
    fun oneCapturingGroup_wrongSecondParameter_correctFailure() {
        // Arrange:
        class Module: DispatcherModule() {
            @ServerPath("a*(b*)c*")
            fun functionWithAnyInsteadOfString(recordedRequest: RecordedRequest, group: Any): MockResponse {
                return MockResponse()
            }
        }

        // Act & Assert:
        try {
            multiModuleDispatcher.addModule(Module())

            fail("Exception should have been thrown")
        } catch (e: RuntimeException) {
            assertEquals(
                    "ServerPath-annotated function functionWithAnyInsteadOfString must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                            "Regex pattern a*(b*)c* has 1 capturing groups\n" +
                            "Expected arguments (RecordedRequest, String) - received arguments (RecordedRequest, Any)",
                    e.message)
        }
    }

    @Test
    fun oneCapturingGroup_parametersInWrongOrder_correctFailure() {
        // Arrange:
        class Module: DispatcherModule() {
            @ServerPath("a*(b*)c*")
            fun functionWithParametersInWrongOrder(group: String, recordedRequest: RecordedRequest): MockResponse {
                return MockResponse()
            }
        }

        // Act & Assert:
        try {
            multiModuleDispatcher.addModule(Module())

            fail("Exception should have been thrown")
        } catch (e: RuntimeException) {
            assertEquals(
                    "ServerPath-annotated function functionWithParametersInWrongOrder must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                            "Regex pattern a*(b*)c* has 1 capturing groups\n" +
                            "Expected arguments (RecordedRequest, String) - received arguments (String, RecordedRequest)",
                    e.message)
        }
    }

    //endregion
}