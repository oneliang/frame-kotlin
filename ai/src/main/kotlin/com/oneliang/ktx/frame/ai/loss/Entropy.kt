package com.oneliang.ktx.frame.ai.loss

import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.common.sumByFloat
import com.oneliang.ktx.util.json.toJson
import kotlin.math.ln

fun informationEntropy(floats: Array<Float>): Float {
    return -floats.sumByFloat {
        it * ln(it)
    }
}

fun informationEntropy(doubles: Array<Double>): Double {
    return -doubles.sumByDouble {
        it * ln(it)
    }
}

fun relativeEntropy(calculateY: Array<Float>, realY: Array<Float>): Float {
    if (calculateY.isEmpty() && realY.isEmpty()) {
        return 0.0f
    }
    if (calculateY.size != realY.size) {
        error("size is not match, calculate y size:%s, real y size:%s".format(calculateY.size, realY.size))
    }
    var result = 0.0f
    singleIteration(calculateY.size) { index ->
        val calculate = calculateY[index]
        val real = realY[index]
        result += if (real == 0.0f) {
            0.0f
        } else {
            real * ln(real / calculate)
        }
    }
    return result
}

fun relativeEntropy(calculateY: Array<Double>, realY: Array<Double>): Double {
    if (calculateY.isEmpty() && realY.isEmpty()) {
        return 0.0
    }
    if (calculateY.size != realY.size) {
        error("size is not match, calculate y size:%s, real y size:%s".format(calculateY.size, realY.size))
    }
    var result = 0.0
    singleIteration(calculateY.size) { index ->
        val calculate = calculateY[index]
        val real = realY[index]
        result += if (real == 0.0) {
            0.0
        } else {
            real * ln(real / calculate)
        }
    }
    return result
}

fun crossEntropyLoss(calculateY: Array<Float>, realY: Array<Float>): Float {
    if (calculateY.isEmpty() && realY.isEmpty()) {
        return 0.0f
    }
    if (calculateY.size != realY.size) {
        error("size is not match, calculate y size:%s, real y size:%s".format(calculateY.size, realY.size))
    }
    var result = 0.0f
    singleIteration(calculateY.size) { index ->
        val calculate = calculateY[index]
        val real = realY[index]
        result += if (real == 0.0f) {
            0.0f
        } else {
            real * ln(calculate)
        }
    }
    return -result
}

fun crossEntropyLoss(calculateY: Array<Double>, realY: Array<Double>): Double {
    if (calculateY.isEmpty() && realY.isEmpty()) {
        return 0.0
    }
    if (calculateY.size != realY.size) {
        error("size is not match, calculate y size:%s, real y size:%s".format(calculateY.size, realY.size))
    }
    var result = 0.0
    singleIteration(calculateY.size) { index ->
        val calculate = calculateY[index]
        val real = realY[index]
        result += if (real == 0.0) {
            0.0
        } else {
            real * ln(calculate)
        }
    }
    return -result
}

fun crossEntropyLoss1(calculateY: Array<Double>, realY: Array<Double>): Array<Double> {
    if (calculateY.isEmpty() && realY.isEmpty()) {
        return emptyArray()
    }
    if (calculateY.size != realY.size) {
        error("size is not match, calculate y size:%s, real y size:%s".format(calculateY.size, realY.size))
    }
    val results = Array(calculateY.size) { 0.0 }
    singleIteration(calculateY.size) { index ->
        val calculate = calculateY[index]
        val real = realY[index]
        results[index] = if (real == 0.0) {
            0.0
        } else {
            -real * ln(calculate)
        }
    }
    return results
}

fun main() {
    val a = arrayOf(0.5, 0.2, 0.3)
    val b = arrayOf(1.0, 0.0, 0.0)
    val c = arrayOf(0.7, 0.2, 0.1)
    println(informationEntropy(a))
    println(relativeEntropy(c, b))
    println(crossEntropyLoss(c, b))
    println(crossEntropyLoss1(c, b).toJson())
}