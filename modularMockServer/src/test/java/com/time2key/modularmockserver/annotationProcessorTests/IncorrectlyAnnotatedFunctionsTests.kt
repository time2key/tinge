package com.time2key.modularmockserver.annotationProcessorTests

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourceSubjectFactory
import com.time2key.modularmockserver.ServerPathAnnotationProcessor
import org.junit.Test

class IncorrectlyAnnotatedFunctionsTests {

    // region General tests

    @Test
    fun invalidRegex_correctFailure() {
        val inputFile = JavaFileObjects.forSourceString(
                "com.test.Module",
                """
                package com.test;
                
                import com.time2key.modularmockserver.DispatcherModule;
                import com.time2key.modularmockserver.ServerPath;
                import okhttp3.mockwebserver.MockResponse;
                import okhttp3.mockwebserver.RecordedRequest;
                
                public class Module extends DispatcherModule {
                
                    @ServerPath(matchingPathRegex = ".*(")
                    public MockResponse functionWithInvalidRegex(RecordedRequest request) {
                        return new MockResponse();
                    }
                    
                }""")

        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(inputFile)
                .processedWith(ServerPathAnnotationProcessor())
                .failsToCompile()
                .withErrorCount(1)
                .withErrorContaining("ServerPath annotation has invalid Regex")
    }

    // endregion



    // region Wrong visibility modifier tests

    @Test
    fun privateVisibility_correctFailure() {
        val inputFile = JavaFileObjects.forSourceString(
                "com.test.Module",
                """
                package com.test;
                
                import com.time2key.modularmockserver.DispatcherModule;
                import com.time2key.modularmockserver.ServerPath;
                import okhttp3.mockwebserver.MockResponse;
                import okhttp3.mockwebserver.RecordedRequest;
                
                public class Module extends DispatcherModule {
                
                    @ServerPath(matchingPathRegex = ".*")
                    private MockResponse functionWithPrivateVisibility(RecordedRequest request) {
                        return new MockResponse();
                    }
                    
                }""")

        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(inputFile)
                .processedWith(ServerPathAnnotationProcessor())
                .failsToCompile()
                .withErrorCount(1)
                .withErrorContaining("ServerPath-annotated function must have public visibility")
    }

    @Test
    fun protectedVisibility_correctFailure() {
        val inputFile = JavaFileObjects.forSourceString(
                "com.test.Module",
                """
                package com.test;
                
                import com.time2key.modularmockserver.DispatcherModule;
                import com.time2key.modularmockserver.ServerPath;
                import okhttp3.mockwebserver.MockResponse;
                import okhttp3.mockwebserver.RecordedRequest;
                
                public class Module extends DispatcherModule {
                
                    @ServerPath(matchingPathRegex = ".*")
                    protected MockResponse functionWithProtectedVisibility(RecordedRequest request) {
                        return new MockResponse();
                    }
                    
                }""")

        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(inputFile)
                .processedWith(ServerPathAnnotationProcessor())
                .failsToCompile()
                .withErrorCount(1)
                .withErrorContaining("ServerPath-annotated function must have public visibility")
    }

    @Test
    fun defaultVisibility_correctFailure() {
        val inputFile = JavaFileObjects.forSourceString(
                "com.test.Module",
                """
                package com.test;
                
                import com.time2key.modularmockserver.DispatcherModule;
                import com.time2key.modularmockserver.ServerPath;
                import okhttp3.mockwebserver.MockResponse;
                import okhttp3.mockwebserver.RecordedRequest;
                
                public class Module extends DispatcherModule {
                
                    @ServerPath(matchingPathRegex = ".*")
                    MockResponse functionWithDefaultVisibility(RecordedRequest request) {
                        return new MockResponse();
                    }
                    
                }""")

        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(inputFile)
                .processedWith(ServerPathAnnotationProcessor())
                .failsToCompile()
                .withErrorCount(1)
                .withErrorContaining("ServerPath-annotated function must have public visibility")
    }

    //endregion



    // region Testing regex with no capturing groups

    @Test
    fun noCapturingGroups_noParameters_correctFailure() {
        val inputFile = JavaFileObjects.forSourceString(
                "com.test.Module",
                """
                package com.test;
                
                import com.time2key.modularmockserver.DispatcherModule;
                import com.time2key.modularmockserver.ServerPath;
                import okhttp3.mockwebserver.MockResponse;
                import okhttp3.mockwebserver.RecordedRequest;
                
                public class Module extends DispatcherModule {
                
                    @ServerPath(matchingPathRegex = ".*")
                    public MockResponse functionMissingRecordedRequest() {
                        return new MockResponse();
                    }
                    
                }""")

        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(inputFile)
                .processedWith(ServerPathAnnotationProcessor())
                .failsToCompile()
                .withErrorCount(1)
                .withErrorContaining("ServerPath-annotated function must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                        "  Regex pattern .* has 0 capturing groups")

    }

    @Test
    fun noCapturingGroups_wrongParameter_correctFailure() {
        val inputFile = JavaFileObjects.forSourceString(
                "com.test.Module",
                """
                package com.test;
                
                import com.time2key.modularmockserver.DispatcherModule;
                import com.time2key.modularmockserver.ServerPath;
                import okhttp3.mockwebserver.MockResponse;
                import okhttp3.mockwebserver.RecordedRequest;
                
                public class Module extends DispatcherModule {
                
                    @ServerPath(matchingPathRegex = ".*")
                    public MockResponse functionMissingRecordedRequest(String stringArgument) {
                        return new MockResponse();
                    }
                    
                }""")

        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(inputFile)
                .processedWith(ServerPathAnnotationProcessor())
                .failsToCompile()
                .withErrorCount(1)
                .withErrorContaining("ServerPath-annotated function must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                        "  Regex pattern .* has 0 capturing groups")

    }

    @Test
    fun noCapturingGroups_extraStringParameter_correctFailure() {
        val inputFile = JavaFileObjects.forSourceString(
                "com.test.Module",
                """
                package com.test;
                
                import com.time2key.modularmockserver.DispatcherModule;
                import com.time2key.modularmockserver.ServerPath;
                import okhttp3.mockwebserver.MockResponse;
                import okhttp3.mockwebserver.RecordedRequest;
                
                public class Module extends DispatcherModule {
                
                    @ServerPath(matchingPathRegex = ".*")
                    public MockResponse functionMissingRecordedRequest(String stringArgument) {
                        return new MockResponse();
                    }
                    
                }""")

        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(inputFile)
                .processedWith(ServerPathAnnotationProcessor())
                .failsToCompile()
                .withErrorCount(1)
                .withErrorContaining("ServerPath-annotated function must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                        "  Regex pattern .* has 0 capturing groups")

    }

    // endregion



    // region Testing regex with one capturing group

    @Test
    fun oneCapturingGroup_noParameters_correctFailure() {
        val inputFile = JavaFileObjects.forSourceString(
                "com.test.Module",
                """
                package com.test;
                
                import com.time2key.modularmockserver.DispatcherModule;
                import com.time2key.modularmockserver.ServerPath;
                import okhttp3.mockwebserver.MockResponse;
                import okhttp3.mockwebserver.RecordedRequest;
                
                public class Module extends DispatcherModule {
                
                    @ServerPath(matchingPathRegex = "a*(b*)c*")
                    public MockResponse functionMissingRecordedRequestAndString() {
                        return new MockResponse();
                    }
                    
                }""")

        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(inputFile)
                .processedWith(ServerPathAnnotationProcessor())
                .failsToCompile()
                .withErrorCount(1)
                .withErrorContaining("ServerPath-annotated function must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                        "  Regex pattern a*(b*)c* has 1 capturing groups")
    }

    @Test
    fun oneCapturingGroup_missingFirstCapturingGroupParameter_correctFailure() {
        val inputFile = JavaFileObjects.forSourceString(
                "com.test.Module",
                """
                package com.test;
                
                import com.time2key.modularmockserver.DispatcherModule;
                import com.time2key.modularmockserver.ServerPath;
                import okhttp3.mockwebserver.MockResponse;
                import okhttp3.mockwebserver.RecordedRequest;
                
                public class Module extends DispatcherModule {
                
                    @ServerPath(matchingPathRegex = "a*(b*)c*")
                    public MockResponse functionMissingString(RecordedRequest request) {
                        return new MockResponse();
                    }
                    
                }""")

        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(inputFile)
                .processedWith(ServerPathAnnotationProcessor())
                .failsToCompile()
                .withErrorCount(1)
                .withErrorContaining("ServerPath-annotated function must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                        "  Regex pattern a*(b*)c* has 1 capturing groups")
    }

    @Test
    fun oneCapturingGroup_missingRecordedRequestParameter_correctFailure() {
        val inputFile = JavaFileObjects.forSourceString(
                "com.test.Module",
                """
                package com.test;
                
                import com.time2key.modularmockserver.DispatcherModule;
                import com.time2key.modularmockserver.ServerPath;
                import okhttp3.mockwebserver.MockResponse;
                import okhttp3.mockwebserver.RecordedRequest;
                
                public class Module extends DispatcherModule {
                
                    @ServerPath(matchingPathRegex = "a*(b*)c*")
                    public MockResponse functionMissingRecordedRequest(String string) {
                        return new MockResponse();
                    }
                    
                }""")

        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(inputFile)
                .processedWith(ServerPathAnnotationProcessor())
                .failsToCompile()
                .withErrorCount(1)
                .withErrorContaining("ServerPath-annotated function must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                        "  Regex pattern a*(b*)c* has 1 capturing groups")
    }

    @Test
    fun oneCapturingGroup_extraStringParameter_correctFailure() {
        val inputFile = JavaFileObjects.forSourceString(
                "com.test.Module",
                """
                package com.test;
                
                import com.time2key.modularmockserver.DispatcherModule;
                import com.time2key.modularmockserver.ServerPath;
                import okhttp3.mockwebserver.MockResponse;
                import okhttp3.mockwebserver.RecordedRequest;
                
                public class Module extends DispatcherModule {
                
                    @ServerPath(matchingPathRegex = "a*(b*)c*")
                    public MockResponse functionWithExtraStringParameter(RecordedRequest recordedRequest, String group1, String group2) {
                        return new MockResponse();
                    }
                    
                }""")

        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(inputFile)
                .processedWith(ServerPathAnnotationProcessor())
                .failsToCompile()
                .withErrorCount(1)
                .withErrorContaining("ServerPath-annotated function must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                        "  Regex pattern a*(b*)c* has 1 capturing groups")
    }

    @Test
    fun oneCapturingGroup_wrongFirstParameter_correctFailure() {
        val inputFile = JavaFileObjects.forSourceString(
                "com.test.Module",
                """
                package com.test;
                
                import com.time2key.modularmockserver.DispatcherModule;
                import com.time2key.modularmockserver.ServerPath;
                import okhttp3.mockwebserver.MockResponse;
                import okhttp3.mockwebserver.RecordedRequest;
                
                public class Module extends DispatcherModule {
                
                    @ServerPath(matchingPathRegex = "a*(b*)c*")
                    public MockResponse functionWithIntInsteadOfRecordedRequest(int recordedRequest, String group) {
                        return new MockResponse();
                    }
                    
                }""")

        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(inputFile)
                .processedWith(ServerPathAnnotationProcessor())
                .failsToCompile()
                .withErrorCount(1)
                .withErrorContaining("ServerPath-annotated function must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                        "  Regex pattern a*(b*)c* has 1 capturing groups")
    }

    @Test
    fun oneCapturingGroup_secondParameterIsSupertype_correctFailure() {
        val inputFile = JavaFileObjects.forSourceString(
                "com.test.Module",
                """
                package com.test;
                
                import com.time2key.modularmockserver.DispatcherModule;
                import com.time2key.modularmockserver.ServerPath;
                import okhttp3.mockwebserver.MockResponse;
                import okhttp3.mockwebserver.RecordedRequest;
                
                public class Module extends DispatcherModule {
                
                    @ServerPath(matchingPathRegex = "a*(b*)c*")
                    public MockResponse functionWithObjectInsteadOfString(RecordedRequest recordedRequest, Object group) {
                        return new MockResponse();
                    }
                    
                }""")

        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(inputFile)
                .processedWith(ServerPathAnnotationProcessor())
                .failsToCompile()
                .withErrorCount(1)
                .withErrorContaining("ServerPath-annotated function must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                        "  Regex pattern a*(b*)c* has 1 capturing groups")
    }

    @Test
    fun oneCapturingGroup_wrongSecondParameter_correctFailure() {
        val inputFile = JavaFileObjects.forSourceString(
                "com.test.Module",
                """
                package com.test;
                
                import com.time2key.modularmockserver.DispatcherModule;
                import com.time2key.modularmockserver.ServerPath;
                import okhttp3.mockwebserver.MockResponse;
                import okhttp3.mockwebserver.RecordedRequest;
                
                public class Module extends DispatcherModule {
                
                    @ServerPath(matchingPathRegex = "a*(b*)c*")
                    public MockResponse functionWithRecordedRequestInsteadOfString(RecordedRequest recordedRequest, RecordedRequest group) {
                        return new MockResponse();
                    }
                    
                }""")

        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(inputFile)
                .processedWith(ServerPathAnnotationProcessor())
                .failsToCompile()
                .withErrorCount(1)
                .withErrorContaining("ServerPath-annotated function must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                        "  Regex pattern a*(b*)c* has 1 capturing groups")
    }

    @Test
    fun oneCapturingGroup_parametersInWrongOrder_correctFailure() {
        val inputFile = JavaFileObjects.forSourceString(
                "com.test.Module",
                """
                package com.test;
                
                import com.time2key.modularmockserver.DispatcherModule;
                import com.time2key.modularmockserver.ServerPath;
                import okhttp3.mockwebserver.MockResponse;
                import okhttp3.mockwebserver.RecordedRequest;
                
                public class Module extends DispatcherModule {
                
                    @ServerPath(matchingPathRegex = "a*(b*)c*")
                    public MockResponse functionWithParametersInWrongOrder(String group, RecordedRequest recordedRequest) {
                        return new MockResponse();
                    }
                    
                }""")

        Truth.assert_()
                .about(JavaSourceSubjectFactory.javaSource())
                .that(inputFile)
                .processedWith(ServerPathAnnotationProcessor())
                .failsToCompile()
                .withErrorCount(1)
                .withErrorContaining("ServerPath-annotated function must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                        "  Regex pattern a*(b*)c* has 1 capturing groups")
    }

    // endregion
}