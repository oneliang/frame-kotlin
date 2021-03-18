package com.oneliang.ktx.frame.test.ai

import com.oneliang.ktx.frame.ai.regression.LogisticRegressionMachine

fun main() {
//    val weightArray = Array(3) { 0.0 }
    val weightArray = Array(2) { 0.0 }
//    val learningRate = 0.00000000001
    val learningRate = 0.01
    val times = 10000
    val batching = TestLogisticRegressionBatching(100)
//    val batching = TestStableDataBatching(100)
    val newWeightArray = LogisticRegressionMachine.study(batching, weightArray, learningRate, times, 100)
    batching.reset()
    LogisticRegressionMachine.test(batching, newWeightArray)
}