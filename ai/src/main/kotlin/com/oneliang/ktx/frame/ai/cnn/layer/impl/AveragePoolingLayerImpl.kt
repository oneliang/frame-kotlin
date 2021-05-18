package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.layer.AveragePoolingLayer
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.math.matrix.scaleToSmall

open class AveragePoolingLayerImpl(
    inX: Int,
    inY: Int,
    scale: Int
) : AveragePoolingLayer<Array<Array<Array<Float>>>, Array<Array<Array<Float>>>>(inX, inY, scale) {

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Float>>>, y: Float, training: Boolean): Array<Array<Array<Float>>> {
        val outputNeuron = Array(inputNeuron.size) { Array(this.outY) { Array(this.outX) { 0.0f } } }
        singleIteration(inputNeuron.size) { mapIndex ->
            outputNeuron[mapIndex] = inputNeuron[mapIndex].scaleToSmall(this.scale)
        }
        return inputNeuron
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