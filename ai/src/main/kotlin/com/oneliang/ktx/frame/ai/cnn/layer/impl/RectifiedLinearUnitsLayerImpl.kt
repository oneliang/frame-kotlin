package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.layer.RectifiedLinearUnitsLayer
import com.oneliang.ktx.frame.ai.dnn.layer.Layer

class RectifiedLinearUnitsLayerImpl(
    mapDepth: Int,//32
    inX: Int,
    inY: Int
) : RectifiedLinearUnitsLayer<Array<Array<Array<Double>>>, Array<Array<Array<Double>>>>(mapDepth, inX, inY) {

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Double>>>, y: Double, training: Boolean): Array<Array<Array<Double>>> {
        return inputNeuron
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