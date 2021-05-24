package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.layer.OutputLayer
import com.oneliang.ktx.frame.ai.loss.crossEntropyLoss
import com.oneliang.ktx.pojo.FloatWrapper
import com.oneliang.ktx.util.common.maxOfWithIndexed
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap

class OutputLayerImpl(typeCount: Int) : OutputLayer<Array<Float>, Array<Float>>(typeCount) {
    companion object {
        private val logger = LoggerManager.getLogger((OutputLayerImpl::class))
        private const val SUM_KEY = "sum"
    }

    private var lastSumLoss = Float.MAX_VALUE

    private val correctProbability = Array(typeCount) { Array(typeCount) { 0.0f } }
    private val testDataCorrectMap = ConcurrentHashMap<Long, Float>()
    private val testCalculateYMap = ConcurrentHashMap<Int, Counter>()

    class Counter(var correctCount: Int, var totalCount: Int)

    init {
        for (type in 0 until typeCount) {
            this.correctProbability[type][type] = 1.0f//one hot encode
        }
    }

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Float>, y: Float, training: Boolean): Array<Float> {
        if (!training) {//test
            val correctYType = y.toInt()
            val calculateProbability = inputNeuron[correctYType]
            if (calculateProbability >= 0.9) {
                this.testDataCorrectMap[dataId] = calculateProbability
            }
            val (maxIndex, value) = inputNeuron.maxOfWithIndexed { it.toDouble() }
            val counter = this.testCalculateYMap.getOrPut(correctYType) { Counter(0, 0) }
            if (maxIndex == correctYType) {
                counter.correctCount++
            }
            counter.totalCount++
            logger.info("calculate y[%s] probability:%s, real y:%s, calculate probability:%s", correctYType, calculateProbability, y, inputNeuron.toJson())
        }
        return inputNeuron
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Float>, y: Float) {
        val correctYType = y.toInt()

//        val calculateYProbability = inputNeuron[correctYType]
        val a = crossEntropyLoss(inputNeuron, this.correctProbability[correctYType])
        if (a.isNaN()) {
            error("data id:%s, input:%s".format(dataId, inputNeuron.toJson()))
        }
        this.sumLoss.operate(SUM_KEY, create = {
            FloatWrapper(crossEntropyLoss(inputNeuron, this.correctProbability[correctYType]))
        }, update = {
            FloatWrapper(it.value + crossEntropyLoss(inputNeuron, this.correctProbability[correctYType]))
        })
    }

    override fun checkLossImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float): Boolean {
        val totalLoss = this.sumLoss[SUM_KEY]?.value ?: 0.0f
        val result = totalLoss <= this.lastSumLoss
        if (!result) {
            logger.error("epoch:%s, last total loss:%s, current total loss:%s, total data size:%s", epoch, this.lastSumLoss, totalLoss, totalDataSize)
        }
        this.lastSumLoss = totalLoss
        return result
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) {
        if (epoch % printPeriod == 0) {
            val totalLoss = this.sumLoss[SUM_KEY]?.value ?: 0.0f
            logger.debug("epoch:%s, total loss:%s, average loss:%s", epoch, totalLoss, totalLoss / totalDataSize)
        }
        //reset after update
        this.sumLoss.clear()//reset after update per one time
    }

    override fun initializeLayerModelDataImpl(data: String) {
    }

    override fun saveLayerModelDataImpl(): String {
        return Constants.String.BLANK
    }

    override fun testProcessImpl(totalDataSize: Long) {
        val correctDataSize = this.testDataCorrectMap.size
        logger.debug("(correct/total)=(%s/%s), correct probability:%s", correctDataSize, totalDataSize, correctDataSize.toFloat() / totalDataSize)
        val totalCounter = Counter(0, 0)
        this.testCalculateYMap.forEach { (y, counter) ->
            logger.debug("y:%s, (correct/total)=(%s/%s)", y, counter.correctCount, counter.totalCount)
            totalCounter.correctCount += counter.correctCount
            totalCounter.totalCount += counter.totalCount
        }
        logger.debug("(correct/total)=(%s/%s),correct probability:%s", totalCounter.correctCount, totalCounter.totalCount, totalCounter.correctCount.toFloat() / totalCounter.totalCount)
    }
}