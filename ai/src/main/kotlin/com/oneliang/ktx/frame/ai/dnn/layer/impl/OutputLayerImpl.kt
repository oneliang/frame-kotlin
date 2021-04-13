package com.oneliang.ktx.frame.ai.dnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.printToMatrix
import com.oneliang.ktx.frame.ai.dnn.layer.OutputLayer
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquares
import com.oneliang.ktx.pojo.DoubleWrapper
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.json.toJson
import java.util.concurrent.ConcurrentHashMap

class OutputLayerImpl(neuronCount: Int) : OutputLayer<Array<Double>, Array<Double>>(neuronCount) {
    companion object {
        private const val SUM_KEY = "sum"
    }

    //coroutine concurrent, use for input data
    var loss = ConcurrentHashMap<Long, Array<Double>>()

    //coroutine concurrent, use for all data in layer
    var sumLoss = AtomicMap<String, DoubleWrapper>()

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double, training: Boolean): Array<Double> {
        inputNeuron.printToMatrix(this.neuronCount)
        return inputNeuron
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double) {//get 0 for test, only out put one
        val loss = this.loss.getOrPut(dataId) { arrayOf(inputNeuron[0] - y) }!!
        sumLoss.operate(SUM_KEY, create = {
            DoubleWrapper(loss.sumByDouble {
                ordinaryLeastSquares(it)
            })
        }, update = { oldDoubleWrapper ->
            DoubleWrapper(oldDoubleWrapper.value + loss.sumByDouble {
                ordinaryLeastSquares(it)
            })
        })
    }

    override fun forwardResetImpl(dataId: Long) {
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) {
    }

    override fun initializeLayerModelDataImpl(data: String) {
    }

    override fun saveLayerModelDataImpl(): String {
        return Constants.String.BLANK
    }
}