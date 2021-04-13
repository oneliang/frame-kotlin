package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.layer.DropoutLayer
import com.oneliang.ktx.frame.ai.cnn.layer.LocalResponseNormalizationLayer
import com.oneliang.ktx.frame.ai.cnn.layer.SoftmaxLayer
import com.oneliang.ktx.frame.ai.dnn.layer.Layer

open class SoftmaxLayerImpl : SoftmaxLayer<Array<Array<Array<Double>>>, Array<Array<Array<Double>>>>() {

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Double>>>, y: Double, training: Boolean): Array<Array<Array<Double>>> {
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