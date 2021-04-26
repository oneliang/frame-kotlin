package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.layer.OutputLayer
import com.oneliang.ktx.frame.ai.loss.crossEntropyLoss
import com.oneliang.ktx.frame.ai.loss.crossEntropyLoss1
import com.oneliang.ktx.pojo.DoubleWrapper
import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.math.matrix.transpose

class OutputLayerImpl(typeCount: Int) : OutputLayer<Array<Double>, Array<Double>, Array<Array<Double>>>(typeCount) {
    companion object {
        private val logger = LoggerManager.getLogger((OutputLayerImpl::class))
        private const val SUM_KEY = "sum"
    }

    private val correctProbability = Array(typeCount) { Array(typeCount) { 0.0 } }

    init {
        for (type in 0 until typeCount) {
            this.correctProbability[type][type] = 1.0//one hot encode
        }
    }

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double, training: Boolean): Array<Double> {
//        if (!training) {//test
        val correctYType = y.toInt()
        logger.info("calculate y:%s, real y:%s, calculate probability:%s", inputNeuron[correctYType], y, inputNeuron.toJson())
//        }
        return inputNeuron
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double) {
        val loss = Array(this.typeCount) { 0.0 }
        val correctYType = y.toInt()

        singleIteration(this.typeCount) { typeIndex ->
            loss[typeIndex] = inputNeuron[typeIndex] - this.correctProbability[correctYType][typeIndex]
        }
//        val calculateYProbability = inputNeuron[correctYType]
        this.sumLoss.operate(SUM_KEY, create = {
            DoubleWrapper(crossEntropyLoss(inputNeuron, this.correctProbability[correctYType]))
        }, update = {
            DoubleWrapper(it.value + crossEntropyLoss(inputNeuron, this.correctProbability[correctYType]))
        })
        this.inputNeuronLoss[dataId] = loss.transpose()//[*][1]
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) {

    }

    override fun initializeLayerModelDataImpl(data: String) {
    }

    override fun saveLayerModelDataImpl(): String {
        return Constants.String.BLANK
    }
}