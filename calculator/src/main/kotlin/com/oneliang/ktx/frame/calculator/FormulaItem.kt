package com.oneliang.ktx.frame.calculator

class FormulaItem(val code: String,
                  val name: String,
                  val parameter: String,
                  val parameterType: ParameterType,
                  val javaScriptFunction: String,
                  val returnCode: String,
                  val formulaType: FormulaType,
                  val order: Int) {
    enum class ParameterType(val value: String) {
        JSON_OBJECT("JSON_OBJECT"), STRING("STRING")
    }

    enum class FormulaType(val value: String) {
        PROCESS("PROCESS"), RESULT("RESULT")
    }
}