package com.oneliang.ktx.frame.ai.function

import com.oneliang.ktx.util.common.sumByDoubleIndexed
import com.oneliang.ktx.util.math.matrix.multiply

fun linear(xArray: Array<Double>, weightArray: Array<Double> = emptyArray()): Double {
    if (xArray.isEmpty()) {
        error("x array can not be empty, it have one x at least")
    }
    val fixWeightArray = when {
        weightArray.isEmpty() -> {
            Array(xArray.size) { 0.0 }
        }
        xArray.size == weightArray.size -> {
            weightArray
        }
        else -> {
            error("x array size must be equal weight array size, x array size:%s, weight array size:%s".format(xArray.size, weightArray.size))
        }
    }
    return xArray.sumByDoubleIndexed { index, item ->
        fixWeightArray[index] * item
    }
}

fun linear(xArray: Array<Double>, weightArray: Array<Array<Double>>): Array<Double> {
    if (xArray.isEmpty()) {
        error("x array can not be empty, it have one x at least")
    }
    if (weightArray.isEmpty()) {
        error("weight array can not be empty, it have one weight at least")
    }
    val fixWeightArray = when (xArray.size) {
        weightArray.size -> {
            weightArray
        }
        else -> {
            error("x array size must be equal weight array size, x array size:%s, weight array size:%s".format(xArray.size, weightArray.size))
        }
    }
    return xArray.multiply(fixWeightArray)
}