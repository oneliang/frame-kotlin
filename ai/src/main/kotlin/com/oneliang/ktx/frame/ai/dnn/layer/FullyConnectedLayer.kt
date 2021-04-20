package com.oneliang.ktx.frame.ai.dnn.layer

import com.oneliang.ktx.Constants

open class FullyConnectedLayer<IN : Any, OUT : Any, LOSS : Any>(
    val neuronCount: Int,
    private val forwardImpl: ((layer: FullyConnectedLayer<IN, OUT, LOSS>, dataId: Long, inputNeuron: IN, y: Double, training: Boolean) -> OUT)? = null,
    private val backwardImpl: ((layer: FullyConnectedLayer<IN, OUT, LOSS>, dataId: Long, inputNeuron: IN, y: Double) -> Unit)? = null,
    private val forwardResetImpl: ((layer: FullyConnectedLayer<IN, OUT, LOSS>, dataId: Long) -> Unit)? = null,
    private val updateImpl: ((layer: FullyConnectedLayer<IN, OUT, LOSS>, epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) -> Unit)? = null,
    private val initializeLayerModelDataImpl: ((layer: FullyConnectedLayer<IN, OUT, LOSS>, data: String) -> Unit) = { _, _ -> },
    private val saveLayerModelDataImpl: ((layer: FullyConnectedLayer<IN, OUT, LOSS>) -> String) = { Constants.String.BLANK }
) : LossLayer<IN, OUT, LOSS>() {

    override fun forwardImpl(dataId: Long, inputNeuron: IN, y: Double, training: Boolean): OUT {
        return this.forwardImpl?.invoke(this, dataId, inputNeuron, y, training) ?: outputNullError()
    }

    override fun backwardImpl(dataId: Long, inputNeuron: IN, y: Double) {
        this.backwardImpl?.invoke(this, dataId, inputNeuron, y)
    }

    override fun forwardResetImpl(dataId: Long) {
        super.forwardResetImpl(dataId)
        this.forwardResetImpl?.invoke(this, dataId)
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) {
        this.updateImpl?.invoke(this, epoch, printPeriod, totalDataSize, learningRate)
    }

    override fun initializeLayerModelDataImpl(data: String) {
        this.initializeLayerModelDataImpl.invoke(this, data)
    }

    override fun saveLayerModelDataImpl(): String {
        return this.saveLayerModelDataImpl.invoke(this)
    }
}