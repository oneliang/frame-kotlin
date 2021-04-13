package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.layer.OutputLayer
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.pojo.DoubleWrapper
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import java.util.concurrent.ConcurrentHashMap

class OutputLayerImpl(
    mapDepth: Int = 1,
    x: Int,
    y: Int
) : OutputLayer<Array<Array<Array<Double>>>, Array<Double>>(mapDepth, x, y) {

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Double>>>, y: Double, training: Boolean): Array<Double> {
        return emptyArray()
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Double>>>, y: Double) {
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