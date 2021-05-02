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
) : InputLayer<Array<Double>, Array<Array<Array<Double>>>>(mapDepth, x, y) {

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double, training: Boolean): Array<Array<Array<Double>>> {
        return inputNeuron.toTripleDimensionArray(mapDepth, this.y, x)
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double) {
    }

    override fun forwardResetImpl(dataId: Long) {
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double, training: Boolean) {
    }

    override fun initializeLayerModelDataImpl(data: String) {
    }

    override fun saveLayerModelDataImpl(): String {
        return Constants.String.BLANK
    }
}