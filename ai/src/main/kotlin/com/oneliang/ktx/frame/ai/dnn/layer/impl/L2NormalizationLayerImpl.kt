package com.oneliang.ktx.frame.ai.dnn.layer.impl

import com.oneliang.ktx.frame.ai.activation.l2Normalization
import com.oneliang.ktx.frame.ai.dnn.layer.L2NormalizationLayer

class L2NormalizationLayerImpl : L2NormalizationLayer<Array<Float>, Array<Float>, Array<Float>>() {

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Float>, y: Float, training: Boolean): Array<Float> {
        if (inputNeuron.isEmpty()) {
            error("input neuron error, data size:[%s]".format(inputNeuron.size))
        }
        return l2Normalization(inputNeuron)
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Float>, y: Float) {
        this.inputNeuronLoss[dataId] = getNextLayerInputNeuronLoss(dataId)
    }
}