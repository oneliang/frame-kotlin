package com.oneliang.ktx.frame.ai.activation

import kotlin.math.exp

fun sigmoid(value: Float): Float {
    return 1 / (1 + exp(-value))//1 / (1 + Math.E.pow(-value))
}

fun sigmoid(value: Double): Double {
    return 1 / (1 + exp(-value))//1 / (1 + Math.E.pow(-value))
}

fun sigmoidDerived(value: Float): Float {
    val a = sigmoid(value)
    val b = 1 - sigmoid(value)
    return a * b
}

fun sigmoidDerived(value: Double): Double {
    val a = sigmoid(value)
    val b = 1 - sigmoid(value)
    return a * b
}