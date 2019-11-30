package com.time2key.modularmockserver

import com.google.auto.service.AutoService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.util.regex.PatternSyntaxException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ExecutableType
import javax.tools.Diagnostic

@AutoService(Processor::class)
class ServerPathAnnotationProcessor: AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(ServerPath::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(set: MutableSet<out TypeElement>, roundEnvironment: RoundEnvironment): Boolean {

        roundEnvironment
                .getElementsAnnotatedWith(ServerPath::class.java)
                .forEach {
                    checkServerPathAnnotatedElementIsValid(it)
                }

        return true
    }

    /**
     * Checks that an element annotated with [ServerPath] is valid.
     *
     * If it is valid, true will be returned.
     *
     * If it is not, a Compiler error will be output for the element describing why it isn't valid,
     * and false will be returned.
     *
     * @return
     * Whether the element was valid.
     * */
    private fun checkServerPathAnnotatedElementIsValid(element: Element): Boolean {
        val typeMirrorOf_DispatcherModule = processingEnv.elementUtils.getTypeElement(DispatcherModule::class.qualifiedName).asType()
        val typeMirrorOf_MockResponse = processingEnv.elementUtils.getTypeElement(MockResponse::class.qualifiedName).asType()
        val typeMirrorOf_String = processingEnv.elementUtils.getTypeElement(String::class.java.name).asType()
        val typeMirrorOf_RecordedRequest = processingEnv.elementUtils.getTypeElement(RecordedRequest::class.qualifiedName).asType()

        // Check element is a function:
        if (element.asType() !is ExecutableType) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR,
                    "Only functions can be annotated with ServerPath",
                    element)
            return false
        }

        // Check element is inside a DispatcherModule class:
        val enclosingElementType = element.enclosingElement.asType()
        if (!processingEnv.typeUtils.isSubtype(enclosingElementType, typeMirrorOf_DispatcherModule)) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR,
                    "Functions annotated with ServerPath must be inside a DispatcherModule class",
                    element)
            return false
        }



        val executableType = element.asType() as ExecutableType
        val serverPathAnnotation = element.getAnnotation(ServerPath::class.java)

        // Check return type of function is MockResponse:
        if (executableType.returnType != typeMirrorOf_MockResponse) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR,
                    "ServerPath-annotated function must have return type MockResponse",
                    element)
            return false
        }

        // Check regex in annotation is valid:
        val matchingPathRegex = try {
            Regex(serverPathAnnotation.matchingPathRegex)
        } catch (e: PatternSyntaxException) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR,
                    "ServerPath annotation has invalid Regex",
                    element)
            return false
        }

        // Check parameters match:
        var doParametersMatch = true
        val totalRegexGroupCount = matchingPathRegex.toPattern().matcher("").groupCount()
        val numberOfParameters = executableType.parameterTypes.size

        if (numberOfParameters - 1 != totalRegexGroupCount) {
            doParametersMatch = false
        } else {
            executableType.parameterTypes.forEachIndexed { index, parameterTypeMirror ->
                if (index == 0 && parameterTypeMirror != typeMirrorOf_RecordedRequest) {
                    doParametersMatch = false
                }
                if (index > 1 && parameterTypeMirror != typeMirrorOf_String) {
                    doParametersMatch = false
                }
            }
        }

        if (!doParametersMatch) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR,
                    "ServerPath-annotated function must take RecordedRequest parameter, and one parameter for each capturing group in the ServerPath regex\n" +
                    "Regex pattern ${matchingPathRegex.pattern} has ${totalRegexGroupCount} capturing groups",
                    element)
        }

        return true
    }



    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}