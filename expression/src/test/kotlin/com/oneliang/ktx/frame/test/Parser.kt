package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.expression.ExpressionException
import com.oneliang.ktx.frame.test.function.Complex
import com.oneliang.ktx.frame.test.function.ComplexFunction
import com.oneliang.ktx.frame.test.function.FunctionX
import com.oneliang.ktx.frame.test.function.FunctionXs
import com.oneliang.ktx.frame.test.util.Point

/**
 * The Class Parser.
 */
object Parser {
    /**
     * Eval
     *
     * This is a parser eval. The real parser of a function is within the Fuction
     *
     * FunctionX: functions with one var. Example 1+2*x --> it is more optimized
     *
     * FunctionXs: functions with several vars. Example: 1+2*x+3*y...
     *
     * ComplexFunction: Complex functions with several vars: one var or n vars. Example: 1+x+y +j
     *
     * @param function the function: 1+2*x+j...
     * @param values the values x=10, y=20
     *
     * @return the parser result: complex or real value
     */
    fun eval(function: String, vararg values: Point): ParserResult {
        val result = ParserResult()
        if (function.isNotEmpty()) {
            if (pointIsComplex(*values) || function.toLowerCase().contains("j")) { // Complex
                val complexFunction = ComplexFunction(function)
                val valuesList = pointToComplexValue(*values)
                val varsList = pointToVar(*values)
                try {
                    result.complexValue = complexFunction.getValue(valuesList, varsList)
                } catch (e: ExpressionException) {
                    e.printStackTrace()
                }
            } else {
                try {
                    if (values.size == 1) {
                        val f_x = FunctionX(function)
                        if (values[0].stringValue.isNotEmpty()) {
                            val evaluatedValue = eval(values[0].stringValue)
                            result.value = f_x.getF_xo(evaluatedValue.value)
                        } else {
                            result.value = f_x.getF_xo(values[0].doubleValue)
                        }
                    } else if (values.size > 1) {
                        val f_xs = FunctionXs(function)
                        val valuesList = pointToValue(*values)
                        val varsList = pointToVar(*values)
                        result.value = f_xs.getValue(valuesList, varsList)
                    } else {
                        val f_x = FunctionX(function)
                        result.value = f_x.getF_xo(0.0)
                    }
                } catch (e: ExpressionException) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    /**
     * Eval.
     * @param function the function
     * @param vars the vars
     * @param values the values
     * @return the double
     */
    fun eval(function: String, vars: Array<String>, values: Array<Double>): Double {
        var result = 0.0
        if (function.isNotBlank()) {
            try {
                if (values.isEmpty()) {
                    val f_x = FunctionX(function)
                    result = f_x.getF_xo(0.0)
                } else if (values.size == 1) {
                    val f_x = FunctionX(function)
                    result = f_x.getF_xo(values[0])
                } else if (vars.size > 1 && values.size > 1) {
                    val f_xs = FunctionXs(function)
                    val valuesList = listOf(*values)
                    val varsList = listOf(*vars)
                    result = f_xs.getValue(valuesList, varsList)
                }
            } catch (e: ExpressionException) {
                e.printStackTrace()
            }
        }
        return result
    }

    /**
     * Eval.
     * @param function the function
     * @return the parser result
     */
    fun eval(function: String): ParserResult {
        var result = ParserResult()
        if (function.isNotBlank()) {
            try {
                if ((function.toLowerCase().contains("j") || function.toLowerCase().contains("i")) && !function.toLowerCase().contains("x")) {
                    result = eval(function, Point("x", Complex(1.0, 0.0)))
                } else if (!function.toLowerCase().contains("x")) {
                    val f_x = FunctionX(function)
                    result.value = f_x.getF_xo(0.0)
                } else {
                    throw ExpressionException("function is not well defined")
                }
            } catch (e: ExpressionException) {
                e.printStackTrace()
            }
        }
        return result
    }

    /**
     * PointToValue.
     *
     * @param values the values
     * @return the list
     */
    private fun pointToValue(vararg values: Point): List<Double> {
        val result = mutableListOf<Double>()
        for (i in values.indices) {
            if (values[i].stringValue.isNotEmpty()) {
                val evaluatedValue = eval(values[i].stringValue)
                result.add(evaluatedValue.value)
            } else {
                result.add(values[i].doubleValue)
            }
        }
        return result
    }

    /**
     * PointToComplexValue.
     * @param values the values
     * @return the list
     */
    private fun pointToComplexValue(vararg values: Point): List<Complex> {
        val result = mutableListOf<Complex>()
        for (i in values.indices) {
            if (values[i].isComplex && (values[i].stringValue.isEmpty())) {
                result.add(values[i].complexValue)
            } else if (values[i].stringValue.isNotEmpty()) {
                val evaluatedValue = eval(values[i].stringValue)
                if (evaluatedValue.isComplex) {
                    result.add(evaluatedValue.complexValue)
                } else {
                    result.add(Complex(evaluatedValue.value, 0.0))
                }
            } else {
                result.add(Complex(values[i].doubleValue, 0.0))
            }
        }
        return result
    }

    /**
     * pointIsComplex.
     *
     * @param values the values
     * @return true, if successful
     */
    private fun pointIsComplex(vararg values: Point): Boolean {
        var result = false
        for (i in values.indices) {
            if (values[i].isComplex && (values[i].stringValue.isEmpty())) {
                result = true
                break
            } else {
                if (values[i].stringValue.isNotEmpty()) {
                    val evaluatedValue = eval(values[i].stringValue)
                    if (evaluatedValue.isComplex) {
                        result = true
                        break
                    }
                }
            }
        }
        return result
    }

    /**
     * PointToVar.
     * @param values the values
     * @return the list
     */
    private fun pointToVar(vararg values: Point): List<String> {
        val result = mutableListOf<String>()
        for (element in values) {
            result.add(element.variable)
        }
        return result
    }
}