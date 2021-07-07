package com.oneliang.ktx.frame.script.engine

interface FunctionEngine {

    fun eval(script: String)

    fun invokeFunction(name: String, vararg args: Any?): Any?
}