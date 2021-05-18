package com.oneliang.ktx.frame.test.ai

import com.oneliang.ktx.frame.ai.regression.LinearMachine

fun main() {
    val weightArray = Array(2) { Array(1) { 0.0f } }
//    val weightArray = Array(17) { Array(1) { 0.0 } }
//    val learningRate = 0.00000000001
    val learningRate = 0.0001f
    val times = 10
//    val batching = TestTrendBatching(100)
//    val batching = TestStableDataBatching(100)
    val batching = TestBatching(100)
    val newWeightArray = LinearMachine.study(batching, weightArray, learningRate, times, 1)
    batching.reset()
    LinearMachine.test(batching, newWeightArray)
}