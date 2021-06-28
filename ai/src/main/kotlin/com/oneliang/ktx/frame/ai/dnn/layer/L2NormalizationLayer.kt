package com.oneliang.ktx.frame.ai.dnn.layer

import com.oneliang.ktx.Constants

open class L2NormalizationLayer<IN : Any, OUT : Any, LOSS : IN>(
    private val forwardImpl: ((layer: L2NormalizationLayer<IN, OUT, LOSS>, dataId: Long, inputNeuron: IN, y: Float, training: Boolean) -> OUT)? = null,
    private val backwardImpl: ((layer: L2NormalizationLayer<IN, OUT, LOSS>, dataId: Long, inputNeuron: IN, y: Float) -> Unit)? = null
) : LossLayer<IN, OUT, LOSS>() {

    override fun forwardImpl(dataId: Long, inputNeuron: IN, y: Float, training: Boolean): OUT {
        return this.forwardImpl?.invoke(this, dataId, inputNeuron, y, training) ?: outputNullError()
    }

    override fun backwardImpl(dataId: Long, inputNeuron: IN, y: Float) {
        this.backwardImpl?.invoke(this, dataId, inputNeuron, y)
    }

    override fun forwardResetImpl(dataId: Long) {}

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) {}

    override fun initializeLayerModelDataImpl(data: String) {}

    override fun saveLayerModelDataImpl(): String = Constants.String.BLANK
}