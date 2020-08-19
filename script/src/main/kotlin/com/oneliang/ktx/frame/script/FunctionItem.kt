package com.oneliang.ktx.frame.script

class FunctionItem(val code: String,
                   val name: String,
                   val parameter: String,
                   val parameterType: ParameterType,
                   val javaScriptFunction: String,
                   val returnCode: String,
                   val functionType: FunctionType,
                   val order: Int) {
    enum class ParameterType(val value: String) {
        JSON_OBJECT("JSON_OBJECT"), STRING("STRING")
    }

    enum class FunctionType(val value: String) {
        PROCESS("PROCESS"), RESULT("RESULT")
    }
}