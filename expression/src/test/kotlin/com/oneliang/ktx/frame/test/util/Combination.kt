package com.oneliang.ktx.frame.test.util

import com.oneliang.ktx.frame.expression.ExpressionException

/**
 * The Class Combination.
 */
object Combination {
    /**
     * calc.
     * @param m the m
     * @param n the n
     * @return the double
     * @throws ExpressionException the calculator exception
     */
    @Throws(ExpressionException::class)
    fun calc(m: Int, n: Int): Double {
        if (n < 0) {
            throw ExpressionException("n cannot be <0")
        }
        return if (m == 0) {
            0.0
        } else {
            (Factorial.cal(m, false).toDouble()
                    / (Factorial.cal(m - n, false) * Factorial.cal(n, false)).toDouble())
        }
    }
}