package com.oneliang.ktx.frame.test.util

import com.oneliang.ktx.frame.expression.ExpressionException

/**
 * The Class Factorial.
 */
object Factorial {
    /**
     * cal.
     * @param m the m
     * @param valueZero the value zero
     * @return the int
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    fun cal(m: Int, valueZero: Boolean): Int {
        if (m < 0) {
            throw ExpressionException("the number must be greater than 0")
        }
        var result = 1
        if (m == 0) {
            result = if (valueZero) {
                0
            } else {
                1
            }
        } else {
            for (i in m downTo 1) {
                result *= i
            }
        }
        return result
    }
}