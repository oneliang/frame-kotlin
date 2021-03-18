package com.oneliang.ktx.frame.ai.regression

import com.oneliang.ktx.frame.ai.base.Batching
import com.oneliang.ktx.frame.ai.base.ordinaryLeastSquares
import com.oneliang.ktx.frame.ai.base.ordinaryLeastSquaresDerived
import com.oneliang.ktx.frame.ai.base.sigmoid
import com.oneliang.ktx.util.logging.LoggerManager
import kotlin.math.pow

object LogisticRegressionMachine {
    private val logger = LoggerManager.getLogger(LogisticRegressionMachine::class)

//    // 权值矩阵初始化
//    fun weightArrayInitialize(weightSize: Int): DoubleArray {
//        val weightArray = DoubleArray(weightSize)
//        for (i in 0 until weightSize) {
//            weightArray[i] = 1.0
//        }
//        return weightArray
//    }

    //计算每次迭代后的预测误差
//    fun predictValue(sampleSize: Int, paraNum: Int, feature: Array<DoubleArray>, weightArray: DoubleArray): DoubleArray {
//        val predictValue = DoubleArray(sampleSize)
//        for (i in 0 until sampleSize) {
//            var tmp = 0.0
//            for (j in 0 until paraNum) {
//                tmp += feature[i][j] * weightArray[j]
//            }
//            predictValue[i] = sigmoid(tmp)
//        }
//        return predictValue
//    }

    //计算误差率
//    fun calculateLoss(sampleSize: Int, Label: DoubleArray, predictValueArray: DoubleArray): Double {
//        var totalLoss = 0.0
//        for (i in 0 until sampleSize) {
//            totalLoss += (Label[i] - predictValueArray[i]).pow(2.0)
//        }
//        return totalLoss
//    }

    //LR模型训练
//    fun update(feature: Array<DoubleArray>, Label: DoubleArray, maxCycle: Int, rate: Double): DoubleArray {
//        // 先计算样本个数和特征个数
//        val sampleSize = feature.size
//        val parameterSize: Int = feature[0].size
//        //初始化权重矩阵
//        val weightArray = weightArrayInitialize(parameterSize)
//        // 循环迭代优化权重矩阵
//        for (i in 0 until maxCycle) {
//            // 每次迭代后，样本预测值
//            val predictValueArray = predictValue(sampleSize, parameterSize, feature, weightArray)
//            val totalLoss = calculateLoss(sampleSize, Label, predictValueArray)
//            if (i % 10 == 0) {
//                println("第" + i + "次迭代的预测误差为:" + totalLoss)
//            }
//            //预测值与标签的误差
//            val err = DoubleArray(sampleSize)
//            for (j in 0 until sampleSize) {
//                err[j] = Label[j] - predictValueArray[j]
//            }
//            // 计算权重矩阵的梯度方向
//            val calculateWeightArray = DoubleArray(parameterSize)
//            for (m in 0 until parameterSize) {
//                var tmp = 0.0
//                for (n in 0 until sampleSize) {
//                    tmp += feature[n][m] * err[n]
//                }
//                calculateWeightArray[m] = tmp / sampleSize
//            }
//            for (m in 0 until parameterSize) {
//                weightArray[m] = weightArray[m] + rate * calculateWeightArray[m]
//            }
//        }
//        return weightArray
//    }


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
                    val calculateY = sigmoid(LinearRegression.linear(xArray, newWeightArray))
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
                val calculateY = sigmoid(LinearRegression.linear(xArray, weightArray))
                logger.debug("calculate y:%s, real y:%s", calculateY, y)
            }
        }
    }
}