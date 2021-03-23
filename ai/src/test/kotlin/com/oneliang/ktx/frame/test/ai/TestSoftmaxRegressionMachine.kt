package com.oneliang.ktx.frame.test.ai

import com.oneliang.ktx.frame.ai.activation.softmax
import com.oneliang.ktx.frame.ai.loss.likelihood
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.frame.ai.regression.LinearMachine
import com.oneliang.ktx.util.json.toJson

fun main() {
    val weightArray = Array(3) { Array(2) { 1.0 } }
    val learningRate = 0.04
    val times = 10000
    val batching = TestSoftmaxRegressionBatching(100)
//    val batching = TestStableDataBatching(100)
    val typeCount = weightArray[0].size
    val correctProbability = Array(typeCount) { Array(typeCount) { 0.0 } }
    for (type in 0 until typeCount) {
        correctProbability[type][type] = 1.0
    }
    val activationFunction: (xArray: Array<Double>, newWeightArray: Array<Array<Double>>) -> Array<Double> = { xArray, newWeightArray ->
        softmax(xArray, newWeightArray)
    }
    val newWeightArray = LinearMachine.study(batching, weightArray, learningRate, times, 100,
        activationFunction = activationFunction, lossFunction = { calculateY, y ->
            val correctYType = y.toInt()
            val calculateYProbability = calculateY[correctYType]
            likelihood(calculateYProbability)
        }, gradientFunction = { x, calculateY, y, typeIndex ->
            val correctYType = y.toInt()
            ordinaryLeastSquaresDerived(x, calculateY[typeIndex], correctProbability[correctYType][typeIndex])
        })
    batching.reset()
    LinearMachine.test(batching, newWeightArray, activationFunction = activationFunction, loggerMessageFunction = { calculateY, y ->
        val correctY = y.toInt()
        val calculateProbability = calculateY[correctY]
        "calculate probability:%s, real probability:%s, real y:%s, :%s".format(calculateProbability, correctProbability[correctY][correctY], y, calculateY.toJson())
    })
}