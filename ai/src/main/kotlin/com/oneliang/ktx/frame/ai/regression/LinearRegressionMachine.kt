package com.oneliang.ktx.frame.ai.regression

import com.oneliang.ktx.frame.ai.base.Batching
import com.oneliang.ktx.frame.ai.base.ordinaryLeastSquares
import com.oneliang.ktx.frame.ai.base.ordinaryLeastSquaresDerived
import com.oneliang.ktx.util.logging.LoggerManager

object LinearRegressionMachine {
    private val logger = LoggerManager.getLogger(LinearRegressionMachine::class)

    fun study(batching: Batching, weightArray: Array<Double>, learningRate: Double, times: Int, printPeriod: Int = 500): Array<Double> {
        if (weightArray.isEmpty()) {
            error("weight array is empty")
        }
        val newWeightArray = weightArray.copyOf()
        for (count in 1..times) {
            var totalDataSize = 0L
            var totalCalculateY = 0.0
            var totalY = 0.0
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
                    val calculateY = LinearRegression.linear(xArray, newWeightArray)
                    totalCalculateY += calculateY
                    totalY += y
                    val currentLoss = ordinaryLeastSquares(calculateY, y)
                    //derived, weight gradient descent, sum all weight grad for every x, use for average weight grad
                    xArray.forEachIndexed { index, x ->
                        weightGrad[index] = weightGrad[index] + ordinaryLeastSquaresDerived(x, calculateY, y)
                    }
                    currentLoss
                }
            }
            //update all weight
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

    fun test(batching: Batching, weightArray: Array<Double>) {
        while (true) {
            val result = batching.fetch()
            if (result.finished) {
                logger.warning("Data is empty. Batch may be finished")
                break
            }
            val inputDataList = result.dataList
            inputDataList.forEach { item ->
                val (y, xArray) = item
                val calculateY = LinearRegression.linear(xArray, weightArray)
                logger.debug("calculate y:%s, real y:%s", calculateY, y)
            }
        }
    }
}