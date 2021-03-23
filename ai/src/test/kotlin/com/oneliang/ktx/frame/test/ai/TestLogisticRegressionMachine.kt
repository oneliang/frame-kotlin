package com.oneliang.ktx.frame.test.ai

import com.oneliang.ktx.frame.ai.activation.sigmoid
import com.oneliang.ktx.frame.ai.function.linear
import com.oneliang.ktx.frame.ai.regression.LinearMachine

fun main() {
//    val weightArray = Array(3) { 0.0 }
    val weightArray = Array(2) { 0.0 }
//    val learningRate = 0.00000000001
    val learningRate = 0.01
    val times = 10000
    val batching = TestLogisticRegressionBatching(100)
//    val batching = TestStableDataBatching(100)
    val newWeightArray = LinearMachine.study(
        batching, weightArray, learningRate, times, 100,
        activationFunction = { xArray, newWeightArray -> sigmoid(linear(xArray, newWeightArray)) })
    batching.reset()
    LinearMachine.test(batching, newWeightArray) { sigmoid(it) }
}