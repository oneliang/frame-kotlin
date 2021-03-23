package com.oneliang.ktx.frame.test.ai

import com.oneliang.ktx.frame.ai.regression.LinearMachine

fun main() {
    val weightArray = Array(3) { Array(1) { 0.0 } }
//    val weightArray = Array(17) { Array(1) { 0.0 } }
//    val learningRate = 0.00000000001
    val learningRate = 0.1
    val times = 1000
//    val batching = TestTrendBatching(100)
    val batching = TestStableDataBatching(100)
    val newWeightArray = LinearMachine.study(batching, weightArray, learningRate, times, 1000)
    batching.reset()
    LinearMachine.test(batching, newWeightArray)
}