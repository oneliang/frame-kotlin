package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.layer.TransformLayer
import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.json.toJson

open class TransformLayerImpl : TransformLayer<Array<Array<Array<Double>>>, Array<Double>, Array<Array<Double>>>() {

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Double>>>, y: Double, training: Boolean): Array<Double> {
        if (inputNeuron.isEmpty() || inputNeuron[0].isEmpty() || inputNeuron[0].size != 1 || inputNeuron[0][0].isEmpty() || inputNeuron[0][0].size != 1) {
            error("input neuron error, data size:[%s][%s][%s]".format(inputNeuron.size, inputNeuron[0].size, inputNeuron[0][0].size))
        }
        val outputNeuron = Array(inputNeuron.size) { 0.0 }
        singleIteration(inputNeuron.size) { mapIndex ->
            outputNeuron[mapIndex] = inputNeuron[mapIndex][0][0]
        }
        return outputNeuron
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Double>>>, y: Double) {
        val nextLayerLoss = getNextLayerInputNeuronLoss<Array<Array<Double>>>(dataId)
        println("next layer loss:" + nextLayerLoss.toJson())
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