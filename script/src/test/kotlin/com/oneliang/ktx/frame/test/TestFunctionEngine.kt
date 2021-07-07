package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.script.engine.FunctionEngineManager

fun main() {
    val functionEngineManager = FunctionEngineManager()
    val functionEngine = functionEngineManager.getEngineByName(FunctionEngineManager.EngineName.KOTLIN)
    val className = "com.oneliang.ktx.frame.test.InitializeKt"
    functionEngine.eval("${className}::initialize")
    val value = functionEngine.invokeFunction("initialize")
    println(value)
}