package com.time2key.modularmockserver

import com.google.auto.service.AutoService
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.util.regex.Matcher
import java.util.regex.PatternSyntaxException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
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

        val allDispatcherModules = ArrayList<Element>()
        var areAllAnnotationsValid = true

        roundEnvironment
                .getElementsAnnotatedWith(ServerPath::class.java)
                .forEach {
                    if (!checkServerPathAnnotatedElementIsValid(it)) {
                        areAllAnnotationsValid = false
                    }
                    if (!allDispatcherModules.contains(it.enclosingElement)) {
                        allDispatcherModules.add(it.enclosingElement)
                    }
                }

        if (areAllAnnotationsValid) {
            allDispatcherModules.forEachIndexed { index, element ->
                generateClassForDispatcherModuleClass(element, index)
            }
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
        if (element.asType() !is ExecutableType
                && element.kind == ElementKind.METHOD) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR,
                    "Only functions can be annotated with ServerPath",
                    element)
            return false
        }

        // Check element is inside a DispatcherModule class:
        val enclosingElementType = element.enclosingElement.asType()
        if (!processingEnv.typeUtils.isSubtype(enclosingElementType, typeMirrorOf_DispatcherModule)) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR,
                    "ServerPath-annotated function must be inside a DispatcherModule class",
                    element)
            return false
        }

        // Check element has public visibility:
        if (!element.modifiers.contains(Modifier.PUBLIC)) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR,
                    "ServerPath-annotated function must have public visibility",
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

    /**
     * @param classElement
     * Element corresponding to a DispatcherModule class
     *
     * @param classIndex
     * Unique id / index corresponding to this generated class. Will be used in the class name to
     * ensure that two classes with the same name don't cause their two autogenerated classes to
     * have the same name which could cause problems.
     * */
    private fun generateClassForDispatcherModuleClass(
            classElement: Element,
            classIndex: Int
    ) {
        // Find the package name of classElement:
        // Keep looking at the enclosing element until a package is found:
        var currentElementLookingAt = classElement
        while (currentElementLookingAt.kind != ElementKind.PACKAGE
                && currentElementLookingAt.enclosingElement != null) {
            currentElementLookingAt = currentElementLookingAt.enclosingElement
        }
        val packageElement = (currentElementLookingAt as? PackageElement)

        val packageNameToUse = if (packageElement?.isUnnamed == false) {
            packageElement.qualifiedName.toString()
        } else {
            DEFAULT_PACKAGE_NAME
        }


        val className = "AutoGenerated_${classIndex}_${classElement.simpleName}"

        val mainClassSpec = TypeSpec
                .classBuilder(className)
                .superclass(AutoGeneratedDispatcherModule::class.java)

        val methodSpec = MethodSpec
                .methodBuilder("getServerPathAnnotatedFunctions")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(DispatcherModule::class.java, "dispatcherModule")
                .returns(ArrayList::class.java)
                .addCode("${classElement.simpleName} castDispatcherModule = (${classElement.simpleName})dispatcherModule;\n")
                .addCode("ArrayList returnValue = new ArrayList();\n")

        classElement.enclosedElements.forEach { enclosedElement ->
            val serverPathAnnotation = enclosedElement.getAnnotationsByType(ServerPath::class.java).firstOrNull()
            if (enclosedElement.kind == ElementKind.METHOD
                    && serverPathAnnotation != null) {

                var parameterString = "request"
                val groupCount = Regex(serverPathAnnotation.matchingPathRegex).toPattern().matcher("").groupCount()
                for (i in 0 until groupCount) {
                    parameterString += ", matchResult.getGroupValues().get(${i + 1})";
                }

                methodSpec.addCode("returnValue.add(new AutoGeneratedDispatcherModule.AutoGeneratedServerPathAnnotatedFunction(\"${classElement.simpleName}\", \"${enclosedElement.simpleName}\", \"${serverPathAnnotation.matchingPathRegex}\", ${serverPathAnnotation.priorityIfMultiplePathsMatch}) {\n" +
                        "    @Override\n" +
                        "    public okhttp3.mockwebserver.MockResponse evaluateRequest(okhttp3.mockwebserver.RecordedRequest request) {\n" +
                        "        kotlin.text.MatchResult matchResult = getMatchingPathRegex().matchEntire(request.getPath());\n" +
                        /*"        Object[] groupValues = new String[matchResult.getGroupValues().getSize() - 1];\n" +
                        "        \n" +
                        "        for (int i = 1; i < groupValues.size; i++) {\n" +
                        "            groupValues[i - 1] = matchResult.getGroupValues().get(i);\n" +
                        "        }\n" +*/
                        "        \n" +
                        "        return castDispatcherModule.${enclosedElement.simpleName}(${parameterString});\n" +
                        "    }\n" +
                        "});\n")

            }
        }

        methodSpec.addCode("return returnValue;\n")

        mainClassSpec.addMethod(methodSpec.build())


        val javaFile = JavaFile.builder(packageNameToUse, mainClassSpec.build()).build()

        val javaFileObject = processingEnv.filer.createSourceFile("${packageNameToUse}.${className}")
        val writer = javaFileObject.openWriter()
        writer.write(javaFile.toString())
        writer.close()
    }



    companion object {
        const val DEFAULT_PACKAGE_NAME = "com.time2key.modularmockserver.generatedclasseswithoutapackage"
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}