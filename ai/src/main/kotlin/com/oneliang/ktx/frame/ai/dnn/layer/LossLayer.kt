package com.oneliang.ktx.frame.ai.dnn.layer

import java.util.concurrent.ConcurrentHashMap

abstract class LossLayer<IN : Any, OUT : Any, LOSS : Any> : Layer<IN, OUT>() {

    //coroutine concurrent, use for input data, need to reset it by data id to release memory
    var inputNeuronLoss = ConcurrentHashMap<Long, LOSS>()

    override fun forwardResetImpl(dataId: Long) {
        this.inputNeuronLoss.remove(dataId)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getNextLayerInputNeuronLoss(dataId: Long): T {
        //out put loss
        return when (val nextLayer = this.nextLayer ?: error("next layer is null, need LossLayer")) {
            is LossLayer<*, *, *> -> {
                val nextLayerImpl = nextLayer as LossLayer<*, *, T>
                val outputNeuronLoss = nextLayerImpl.inputNeuronLoss[dataId]!!//next layer input neuron loss = this layer output neuron loss
                outputNeuronLoss//when support bias, delete the bias loss, last input is bias
            }
            else -> {
                error("not support $nextLayer yet, only support LossLayer")
            }
        }
    }
}