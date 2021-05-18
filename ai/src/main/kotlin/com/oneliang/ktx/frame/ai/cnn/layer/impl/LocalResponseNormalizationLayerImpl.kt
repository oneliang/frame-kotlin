package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.layer.LocalResponseNormalizationLayer

open class LocalResponseNormalizationLayerImpl(
    mapDepth: Int
) : LocalResponseNormalizationLayer<Array<Array<Array<Float>>>, Array<Array<Array<Float>>>>(mapDepth) {

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Float>>>, y: Float, training: Boolean): Array<Array<Array<Float>>> {
        return emptyArray()
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Float>>>, y: Float) {
    }

    override fun forwardResetImpl(dataId: Long) {
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) {
    }

    override fun initializeLayerModelDataImpl(data: String) {
    }

    override fun saveLayerModelDataImpl(): String {
        return Constants.String.BLANK
    }
}