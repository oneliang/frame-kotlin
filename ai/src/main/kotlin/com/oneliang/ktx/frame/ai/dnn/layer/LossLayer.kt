package com.oneliang.ktx.frame.ai.dnn.layer

import java.util.concurrent.ConcurrentHashMap

abstract class LossLayer<IN : Any, OUT : Any, LOSS : Any> : Layer<IN, OUT>() {

    //coroutine concurrent, use for input data, need to reset it by data id to release memory
    var inputNeuronLoss = ConcurrentHashMap<Long, LOSS>()

    override fun forwardResetImpl(dataId: Long) {
        this.inputNeuronLoss.remove(dataId)
    }
}