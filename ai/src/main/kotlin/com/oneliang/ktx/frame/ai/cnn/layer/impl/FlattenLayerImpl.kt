package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.layer.FlattenLayer
import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.math.tensor.innerProduct

open class FlattenLayerImpl(
    mapDepth: Int//32
) : FlattenLayer<Array<Array<Array<Float>>>, Array<Float>, Array<Array<Array<Float>>>>(mapDepth) {

    var weights: Array<Array<Array<Array<Float>>>> = emptyArray()//mapDepth * inputMapDepth * inY * inX

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Float>>>, y: Float, training: Boolean): Array<Float> {
        if (inputNeuron.isEmpty() || inputNeuron[0].isEmpty() || inputNeuron[0][0].isEmpty()) {
            error("input neuron error, data size:[%s][%s][%s]".format(inputNeuron.size, inputNeuron[0].size, inputNeuron[0][0].size))
        }
        if (this.weights.isEmpty()) {
            this.weights = Array(this.mapDepth) { Array(inputNeuron.size) { Array(inputNeuron[0].size) { Array(inputNeuron[0][0].size) { 0.1f } } } }
        }
        val outputNeuron = Array(this.mapDepth) { 0.0f }
        singleIteration(this.mapDepth) { mapIndex ->
            val currentWeights = this.weights[mapIndex]
            outputNeuron[mapIndex] = inputNeuron.innerProduct(currentWeights)
        }
        return outputNeuron
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Float>>>, y: Float) {
        //out put loss
        val nextLayerLoss = getNextLayerInputNeuronLoss(dataId)
        println(nextLayerLoss.toJson())
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