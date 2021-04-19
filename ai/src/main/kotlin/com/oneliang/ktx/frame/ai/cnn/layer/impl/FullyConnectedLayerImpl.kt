package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.calculateOutSize
import com.oneliang.ktx.frame.ai.cnn.layer.FullyConnectedLayer
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.math.matrix.multiply
import com.oneliang.ktx.util.math.tensor.innerProduct

open class FullyConnectedLayerImpl(
    mapDepth: Int//32
) : FullyConnectedLayer<Array<Double>, Array<Double>>(mapDepth) {

    var weights: Array<Array<Double>> = emptyArray()//inputMapDepth * mapDepth

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double, training: Boolean): Array<Double> {
        if (inputNeuron.isEmpty()) {
            error("input neuron error, data size:[%s]".format(inputNeuron.size))
        }

        if (this.weights.isEmpty()) {
            this.weights = Array(inputNeuron.size) { Array(this.mapDepth) { 0.1 } }
        }
        val outputNeuron = inputNeuron.multiply(this.weights)
        return outputNeuron
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double) {
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