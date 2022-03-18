package com.oneliang.ktx.frame.ai.function

import com.oneliang.ktx.util.common.sumByDoubleIndexed
import com.oneliang.ktx.util.common.sumByFloatIndexed
import com.oneliang.ktx.util.math.matrix.multiply

fun linear(xDatas: Array<Float>, weights: Array<Float> = emptyArray()): Float {
    if (xDatas.isEmpty()) {
        error("x array can not be empty, it have one x at least")
    }
    val fixWeightArray = when {
        weights.isEmpty() -> {
            Array(xDatas.size) { 0.0f }
        }
        xDatas.size == weights.size -> {
            weights
        }
        else -> {
            error("x array size must be equal weight array size, x array size:%s, weight array size:%s".format(xDatas.size, weights.size))
        }
    }
    return xDatas.sumByFloatIndexed { index, item ->
        fixWeightArray[index] * item
    }
}

fun linear(xDatas: Array<Double>, weights: Array<Double> = emptyArray()): Double {
    if (xDatas.isEmpty()) {
        error("x array can not be empty, it have one x at least")
    }
    val fixWeightArray = when {
        weights.isEmpty() -> {
            Array(xDatas.size) { 0.0 }
        }
        xDatas.size == weights.size -> {
            weights
        }
        else -> {
            error("x array size must be equal weight array size, x array size:%s, weight array size:%s".format(xDatas.size, weights.size))
        }
    }
    return xDatas.sumByDoubleIndexed { index, item ->
        fixWeightArray[index] * item
    }
}

fun linear(xDatas: Array<Float>, weights: Array<Array<Float>>): Array<Float> {
    if (xDatas.isEmpty()) {
        error("x array can not be empty, it have one x at least")
    }
    if (weights.isEmpty()) {
        error("weight array can not be empty, it have one weight at least")
    }
    val fixWeightArray = when (xDatas.size) {
        weights.size -> {
            weights
        }
        else -> {
            error("x array size must be equal weight array size, x array size:%s, weight array size:%s".format(xDatas.size, weights.size))
        }
    }
    return xDatas.multiply(fixWeightArray)
}

fun linear(xDatas: Array<Double>, weights: Array<Array<Double>>): Array<Double> {
    if (xDatas.isEmpty()) {
        error("x array can not be empty, it have one x at least")
    }
    if (weights.isEmpty()) {
        error("weight array can not be empty, it have one weight at least")
    }
    val fixWeights = when (xDatas.size) {
        weights.size -> {
            weights
        }
        else -> {
            error("x array size must be equal weight array size, x array size:%s, weight array size:%s".format(xDatas.size, weights.size))
        }
    }
    return xDatas.multiply(fixWeights)
}