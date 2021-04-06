package com.oneliang.ktx.frame.ai.activation

import com.oneliang.ktx.util.math.matrix.multiply
import kotlin.math.exp

fun softmax(xArray: Array<Double>, weightArray: Array<Array<Double>>, negative: Boolean = false): Array<Double> {
    var totalValue = 0.0
    val valueMatrix = xArray.multiply(weightArray, transform = {
        val value = exp(it)
        totalValue += value
        value
    })
    //update value matrix to probability
    for (index in valueMatrix.indices) {
        valueMatrix[index] = valueMatrix[index] / totalValue * (if (negative) -1.0 else 1.0)
    }
    return valueMatrix
}