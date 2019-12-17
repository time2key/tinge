package com.time2key.modularmockserver.internal

import com.time2key.modularmockserver.ServerPathAnnotationProcessor

/**
 * A 'wormhole' to the autogenerated [BaseAutoGeneratedClassMapper], if it exists.
 *
 * This is statically constructed using reflection
 * */
object AutoGeneratedClassMapperWormhole {

    private val autoGeneratedClassMapper: BaseAutoGeneratedClassMapper? by lazy {
        try {
            val clazz = Class.forName(ServerPathAnnotationProcessor.AUTO_GENERATED_MAPPER_FULLY_QUALIFIED_NAME)
            return@lazy clazz?.newInstance() as? BaseAutoGeneratedClassMapper
        } catch (e: ClassNotFoundException) {
            return@lazy null
        }
    }

    fun getInternalsForClass(clazz: Class<out Any>): AutoGeneratedDispatcherModuleInternals? {
        return autoGeneratedClassMapper?.getInternalsForClass(clazz)
    }
}