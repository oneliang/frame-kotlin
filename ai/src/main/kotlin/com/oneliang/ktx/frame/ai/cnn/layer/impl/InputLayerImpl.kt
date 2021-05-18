package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.layer.InputLayer
import com.oneliang.ktx.frame.ai.cnn.toTripleDimensionArray
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.util.common.doubleIteration
import com.oneliang.ktx.util.common.singleIteration

class InputLayerImpl(
    mapDepth: Int = 1,
    x: Int,
    y: Int
) : InputLayer<Array<Float>, Array<Array<Array<Float>>>>(mapDepth, x, y) {

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Float>, y: Float, training: Boolean): Array<Array<Array<Float>>> {
        return inputNeuron.toTripleDimensionArray(mapDepth, this.y, x)
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Float>, y: Float) {
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