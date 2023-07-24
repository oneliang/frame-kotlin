package com.oneliang.ktx.frame.knowledge

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.*

object Operator {
    enum class Type(val value: String) {
        PLUS(Constants.Symbol.PLUS),
        MINUS(Constants.Symbol.MINUS),
        MULTIPLY(Constants.Symbol.WILDCARD),
        DIVIDE(Constants.Symbol.SLASH_LEFT),
        GREATER_THAN(Constants.Symbol.GREATER_THAN),
        GREATER_THAN_AND_EQUAL(Constants.Symbol.GREATER_THAN + Constants.Symbol.EQUAL),
        LESS_THAN(Constants.Symbol.LESS_THAN),
        LESS_THAN_AND_EQUAL(Constants.Symbol.LESS_THAN + Constants.Symbol.EQUAL),
        FORMAT("f"),
        DATE_FORMAT("df")
    }

    fun updateParameterValueByOperator(key: String, operator: String, parameterMap: MutableMap<String, Any>): Any {
        val operatorList = operator.split(Constants.Symbol.COLON)
        val operatorKey = operatorList[0]
        return when (operatorKey) {
            Type.PLUS.value -> {
                val parameter0 = parameterMap[key]
                val parameter1 = parameterMap[operatorList[1]]
                if (parameter0 is Number && parameter1 is Number) {
                    parameter0.addNumber(parameter1)
                } else {
                    parameter0.toString() + parameter1.toString()
                }
            }

            Type.MINUS.value -> {
                val parameter0 = parameterMap[key]
                val parameter1 = parameterMap[operatorList[1]]
                if (parameter0 is Number && parameter1 is Number) {
                    parameter0.minusNumber(parameter1)
                } else {
                    error("do not support minus, parameter0:%s, parameter1:%s".format(parameter0, parameter1))
                }
            }

            Type.MULTIPLY.value -> {
                val parameter0 = parameterMap[key]
                val parameter1 = parameterMap[operatorList[1]]
                if (parameter0 is Number && parameter1 is Number) {
                    parameter0.multiplyNumber(parameter1)
                } else {
                    error("do not support multiply, parameter0:%s, parameter1:%s".format(parameter0, parameter1))
                }
            }

            Type.DIVIDE.value -> {
                val parameter0 = parameterMap[key]
                val parameter1 = parameterMap[operatorList[1]]
                if (parameter0 is Number && parameter1 is Number) {
                    parameter0.divideNumber(parameter1)
                } else {
                    error("do not support divide, parameter0:%s, parameter1:%s".format(parameter0, parameter1))
                }
            }

            Type.GREATER_THAN.value -> {
                val parameter0 = parameterMap[key]
                val parameter1 = parameterMap[operatorList[1]]
                if (parameter0 is Number && parameter1 is Number) {
                    if (parameter0 > parameter1) {
                        parameter0
                    } else {
                        parameterMap[operatorList[1]] = parameter0
                        parameter1
                    }
                } else {
                    error("do not support greater than, parameter0:%s, parameter1:%s".format(parameter0, parameter1))
                }
            }

            Type.GREATER_THAN_AND_EQUAL.value -> {
                val parameter0 = parameterMap[key]
                val parameter1 = parameterMap[operatorList[1]]
                if (parameter0 is Number && parameter1 is Number) {
                    if (parameter0 >= parameter1) {
                        parameter0
                    } else {
                        parameterMap[operatorList[1]] = parameter0
                        parameter1
                    }
                } else {
                    error("do not support greater than and equal, parameter0:%s, parameter1:%s".format(parameter0, parameter1))
                }
            }

            Type.LESS_THAN.value -> {
                val parameter0 = parameterMap[key]
                val parameter1 = parameterMap[operatorList[1]]
                if (parameter0 is Number && parameter1 is Number) {
                    if (parameter0 < parameter1) {
                        parameter0
                    } else {
                        parameterMap[operatorList[1]] = parameter0
                        parameter1
                    }
                } else {
                    error("do not support less than, parameter0:%s, parameter1:%s".format(parameter0, parameter1))
                }
            }

            Type.LESS_THAN_AND_EQUAL.value -> {
                val parameter0 = parameterMap[key]
                val parameter1 = parameterMap[operatorList[1]]
                if (parameter0 is Number && parameter1 is Number) {
                    if (parameter0 <= parameter1) {
                        parameter0
                    } else {
                        parameterMap[operatorList[1]] = parameter0
                        parameter1
                    }
                } else {
                    error("do not support less than and equal, parameter0:%s, parameter1:%s".format(parameter0, parameter1))
                }
            }

            Type.FORMAT.value -> {
                val parameter0 = parameterMap[key]
                val parameter1 = operatorList[1]
                ("%$parameter1").format(parameter0)
            }

            Type.DATE_FORMAT.value -> {
                val parameter0 = parameterMap[key]
                val parameter1 = operatorList[1]
                if (parameter0 is Long) {
                    parameter0.toUtilDate().toFormatString(parameter1)
                } else {
                    error("do not support date format, parameter0 is not Long type, parameter0:%s, parameter1:%s".format(parameter0, parameter1))
                }
            }

            else -> {
                Constants.String.BLANK
            }
        }
    }
}