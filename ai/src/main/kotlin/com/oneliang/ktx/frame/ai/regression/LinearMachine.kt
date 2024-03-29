package com.oneliang.ktx.frame.ai.regression

import com.oneliang.ktx.frame.ai.base.Batching
import com.oneliang.ktx.frame.ai.function.linear
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquares
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.util.common.sumByFloat
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager

object LinearMachine {
    private val logger = LoggerManager.getLogger(LinearMachine::class)

    fun study(
        batching: Batching<Pair<Float, Array<Float>>>,
        weights: Array<Array<Float>>,
        learningRate: Float,
        times: Int,
        printPeriod: Int = 500,
        activationFunction: (xDatas: Array<Float>, newWeights: Array<Array<Float>>) -> Array<Float> = { xArray, newWeightArray -> linear(xArray, newWeightArray) },
        lossFunction: (calculateY: Array<Float>, y: Float) -> Float = { calculateY, y -> ordinaryLeastSquares(calculateY[0], y) },
        gradientFunction: (x: Float, calculateY: Array<Float>, y: Float, typeIndex: Int) -> Float = { x, calculateY, y, _ -> ordinaryLeastSquaresDerived(x, calculateY[0], y) }
    ): Array<Array<Float>> {
        if (weights.isEmpty()) {
            error("weight array is empty")
        }
        val newWeightArray = weights.copyOf()
        for (count in 1..times) {
            var totalDataSize = 0L
            val weightGrad = Array(newWeightArray.size) { Array(newWeightArray[0].size) { 0.0f } }
            var totalLoss = 0.0
            while (true) {
                val result = batching.fetch()
                if (result.finished) {
                    batching.reset()
                    break
                }
                val inputDataList = result.dataList
                totalDataSize += inputDataList.size
                totalLoss += inputDataList.sumByFloat { item ->
                    val (y, xArray) = item
                    val calculateY = activationFunction(xArray, newWeightArray)
                    val currentLoss = lossFunction(calculateY, y)
                    //derived, weight gradient descent, sum all weight grad for every x, use for average weight grad
                    xArray.forEachIndexed { xIndex, x ->
                        for (position in weightGrad[xIndex].indices) {
                            weightGrad[xIndex][position] += gradientFunction(x, calculateY, y, position)
                        }
                    }
                    currentLoss
                }
            }
            //update all weight, gradient descent
            newWeightArray.forEachIndexed { index, weight ->
                for (type in weight.indices) {
                    newWeightArray[index][type] = weight[type] - (learningRate * weightGrad[index][type]) / totalDataSize
                }
            }
            if (count % printPeriod == 0) {
                logger.debug("times:%s, total loss:%s, average loss:%s, weight array:%s", count, totalLoss, totalLoss / totalDataSize, newWeightArray.toJson())
            }
        }
        logger.debug("newest weight array:%s", newWeightArray.toJson())
        return newWeightArray
    }

    fun test(
        batching: Batching<Pair<Float, Array<Float>>>, weights: Array<Array<Float>>,
        activationFunction: (xDatas: Array<Float>, newWeights: Array<Array<Float>>) -> Array<Float> = { xArray, newWeightArray -> linear(xArray, newWeightArray) },
        loggerMessageFunction: (calculateY: Array<Float>, y: Float) -> String = { calculateY, y -> "calculate y:%s, real y:%s".format(calculateY.toJson(), y) }
    ) {
        while (true) {
            val result = batching.fetch()
            if (result.finished) {
                logger.warning("Data is empty. Batch may be finished")
                break
            }
            val inputDataList = result.dataList
            inputDataList.forEach { item ->
                val (y, xArray) = item
                val calculateY = activationFunction(xArray, weights)
                logger.debug(loggerMessageFunction(calculateY, y))
            }
        }
    }
}