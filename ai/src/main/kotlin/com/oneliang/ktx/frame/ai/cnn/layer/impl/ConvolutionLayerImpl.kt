package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.layer.ConvolutionLayer

class ConvolutionLayerImpl(
    previousLayerMapDepth: Int,//1
    mapDepth: Int,//32
    inX: Int,
    inY: Int,
    x: Int,
    y: Int,
    padding: Int = 0,
    stride: Int = 1
) : ConvolutionLayer<Array<Array<Array<Double>>>, Array<Array<Array<Double>>>>(previousLayerMapDepth, mapDepth, inX, inY, x, y, padding, stride) {

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