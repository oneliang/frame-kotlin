package com.oneliang.ktx.frame.ai.regression

import com.oneliang.ktx.frame.ai.base.Batching
import com.oneliang.ktx.frame.ai.function.linear
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquares
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.util.logging.LoggerManager

object LinearMachine {
    private val logger = LoggerManager.getLogger(LinearMachine::class)

    fun study(
        batching: Batching,
        weightArray: Array<Double>,
        learningRate: Double,
        times: Int,
        printPeriod: Int = 500,
        activationFunction: (calculateY: Double) -> Double = { it },
        lossFunction: (calculateY: Double, y: Double) -> Double = { calculateY, y -> ordinaryLeastSquares(calculateY, y) },
        gradientFunction: (x: Double, calculateY: Double, y: Double) -> Double = { x, calculateY, y -> ordinaryLeastSquaresDerived(x, calculateY, y) }
    ): Array<Double> {
        if (weightArray.isEmpty()) {
            error("weight array is empty")
        }
        val newWeightArray = weightArray.copyOf()
        for (count in 1..times) {
            var totalDataSize = 0L
            val weightGrad = Array(newWeightArray.size) { 0.0 }
            var totalLoss = 0.0
            while (true) {
                val result = batching.fetch()
                if (result.finished) {
                    batching.reset()
                    break
                }
                val inputDataList = result.dataList
                totalDataSize += inputDataList.size
                totalLoss += inputDataList.sumByDouble { item ->
                    val (y, xArray) = item
                    val calculateY = activationFunction(linear(xArray, newWeightArray))
                    val currentLoss = lossFunction(calculateY, y)
                    //derived, weight gradient descent, sum all weight grad for every x, use for average weight grad
                    xArray.forEachIndexed { index, x ->
                        weightGrad[index] += gradientFunction(x, calculateY, y)
                    }
                    currentLoss
                }
            }
            //update all weight, gradient descent
            newWeightArray.forEachIndexed { index, weight ->
                newWeightArray[index] = weight - (learningRate * weightGrad[index]) / totalDataSize
            }
            if (count % printPeriod == 0) {
                logger.debug("times:%s, total loss:%s, weight array:%s", count, totalLoss, newWeightArray.joinToString())
            }
        }
        logger.debug("newest weight array:%s", newWeightArray.joinToString())
        return newWeightArray
    }

    fun test(batching: Batching, weightArray: Array<Double>, activationFunction: (calculateY: Double) -> Double = { it }) {
        while (true) {
            val result = batching.fetch()
            if (result.finished) {
                logger.warning("Data is empty. Batch may be finished")
                break
            }
            val inputDataList = result.dataList
            inputDataList.forEach { item ->
                val (y, xArray) = item
                val calculateY = activationFunction(linear(xArray, weightArray))
                logger.debug("calculate y:%s, real y:%s", calculateY, y)
            }
        }
    }
}