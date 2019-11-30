package com.time2key.modularmockserver

import com.google.auto.service.AutoService
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import kotlin.reflect.KClass

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

                    val enclosingClass = it.enclosingElement
                    if (!processingEnv.typeUtils.isSubtype(
                                    enclosingClass.asType(),
                                    processingEnv.elementUtils.getTypeElement(DispatcherModule::class.qualifiedName).asType())) {
                        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR,
                                "Functions annotated with ServerPath must be inside a DispatcherModule class",
                                it)
                    }
                }

        return true
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}