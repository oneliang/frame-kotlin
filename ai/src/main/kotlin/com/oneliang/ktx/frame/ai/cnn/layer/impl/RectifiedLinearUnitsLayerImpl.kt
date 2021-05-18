package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.activation.rectifiedLinearUnits
import com.oneliang.ktx.frame.ai.cnn.layer.RectifiedLinearUnitsLayer
import com.oneliang.ktx.util.common.doubleIteration
import com.oneliang.ktx.util.common.singleIteration

class RectifiedLinearUnitsLayerImpl : RectifiedLinearUnitsLayer<Array<Array<Array<Float>>>, Array<Array<Array<Float>>>>() {

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Float>>>, y: Float, training: Boolean): Array<Array<Array<Float>>> {
        if (inputNeuron.isEmpty() || inputNeuron[0].isEmpty() || inputNeuron[0][0].isEmpty()) {
            error("input neuron error, data size:[%s][%s][%s]".format(inputNeuron.size, inputNeuron[0].size, inputNeuron[0][0].size))
        }
        val outputNeuron = Array(inputNeuron.size) { Array(inputNeuron[0].size) { Array(inputNeuron[0][0].size) { 0.0f } } }
        singleIteration(inputNeuron.size) { mapDepth ->
            doubleIteration(inputNeuron[0].size, inputNeuron[0][0].size) { row, column ->
                outputNeuron[mapDepth][row][column] = rectifiedLinearUnits(inputNeuron[mapDepth][row][column])
            }
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