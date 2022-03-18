package com.oneliang.ktx.frame.test.ai

import com.oneliang.ktx.frame.ai.activation.sigmoid
import com.oneliang.ktx.frame.ai.function.linear
import com.oneliang.ktx.frame.ai.regression.LinearMachine

fun main() {
//    val weightArray = Array(3) { 0.0 }
    val weights = Array(2) { Array(1) { 0.0f } }
//    val learningRate = 0.00000000001
    val learningRate = 0.01f
    val times = 10000
    val batching = TestLogisticRegressionBatching(100)
//    val batching = TestStableDataBatching(100)
    val activationFunction: (xDatas: Array<Float>, newWeights: Array<Array<Float>>) -> Array<Float> = { xDatas, newWeightArray ->
        val calculateY = linear(xDatas, newWeightArray)
        calculateY.forEachIndexed { index, d ->
            calculateY[index] = sigmoid(d)
        }
        calculateY
    }
    val newWeights = LinearMachine.study(batching, weights, learningRate, times, 100, activationFunction = activationFunction)
    batching.reset()
    LinearMachine.test(batching, newWeights, activationFunction)
}