package com.oneliang.ktx.frame.script.engine

class FunctionEngineManager {

    enum class EngineName(val value: String) {
        JS("js"), JVM("jvm")
    }

    fun getEngineByName(engineName: EngineName, classLoader: ClassLoader? = null): FunctionEngine {
        return when (engineName) {
            EngineName.JS -> {
                JavaScriptFunctionEngine()
            }
            EngineName.JVM -> {
                JvmFunctionEngine(classLoader)
            }
        }
    }
}