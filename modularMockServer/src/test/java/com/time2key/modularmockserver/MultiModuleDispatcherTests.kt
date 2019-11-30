package com.time2key.modularmockserver

import junit.framework.Assert.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.lang.RuntimeException

class MultiModuleDispatcherTests {

    class NumbersDispatcherModule: DispatcherModule() {

        @ServerPath(".*1.*")
        fun matchAnythingWith1CharacterIn(request: RecordedRequest): MockResponse {
            return MockResponse().setBody("1")
        }

        @ServerPath(".*2.*")
        fun matchAnythingWith2CharacterIn(request: RecordedRequest): MockResponse {
            return MockResponse().setBody("2")
        }
    }

    lateinit var multiModuleDispatcher: MultiModuleDispatcher

    @Before
    fun setup() {
        multiModuleDispatcher = MultiModuleDispatcher()
        multiModuleDispatcher.addModule(NumbersDispatcherModule())
    }

    @Test
    fun pathWith1In_matches() {
        // Arrange:
        val request = Mockito.mock(RecordedRequest::class.java)
        Mockito.`when`(request.path).thenReturn("asg/1/sdgs")

        // Act:
        val response = multiModuleDispatcher.dispatch(request)

        // Assert:
        assertEquals("1", response.body.readUtf8())
    }

    @Test
    fun pathWith2In_matches() {
        // Arrange:
        val request = Mockito.mock(RecordedRequest::class.java)
        Mockito.`when`(request.path).thenReturn("asg/2/sdgs")

        // Act:
        val response = multiModuleDispatcher.dispatch(request)

        // Assert:
        assertEquals("2", response.body.readUtf8())
    }

    @Test
    fun pathWithout1Or2In_correctFailure() {
        // Arrange:
        val request = Mockito.mock(RecordedRequest::class.java)
        Mockito.`when`(request.path).thenReturn("asg/sdgs")

        // Act & Assert:
        try {
            multiModuleDispatcher.dispatch(request)

            fail("Exception should have been thrown")
        } catch (e: Exception) {
            assertTrue(e is RuntimeException)
            assertEquals(
                    "No modules have been added matching call with path asg/sdgs\n" +
                            "There are 1 modules added:\n" +
                            "com.time2key.modularmockserber.MultiModuleDispatcherTests.NumbersDispatcherModule\n",
                    e.message)
        }
    }
}