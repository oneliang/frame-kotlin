package com.oneliang.ktx.frame.ai.activation

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

private enum class NormalizationType(val value: Int) {
    L1(0),
    L2(1)
}

private fun normalization(xArray: Array<Float>, normalizationType: NormalizationType): Array<Float> {
    val (sum, denominator) = when (normalizationType) {
        NormalizationType.L1 -> {
            var sum = 0.0f
            for (x in xArray) {
                sum += abs(x)
            }
            sum to sum
        }
        NormalizationType.L2 -> {
            var sum = 0.0f
            for (x in xArray) {
                sum += abs(x)
            }
            sum to sqrt(sum)
        }
    }
    val resultArray = Array(xArray.size) { 0.0f }
    xArray.forEachIndexed { index, value ->
        resultArray[index] = value / denominator
    }
    return resultArray
}

fun l1Normalization(xArray: Array<Float>): Array<Float> = normalization(xArray, NormalizationType.L1)

fun l2Normalization(xArray: Array<Float>): Array<Float> = normalization(xArray, NormalizationType.L2)