package com.oneliang.ktx.frame.ai.dnn.layer

import com.oneliang.ktx.Constants

class OutputLayer<IN : Any, OUT : Any>(
    depth: Int,
    neuronCount: Int,
    private val forwardImpl: ((layer: OutputLayer<IN, OUT>, inputNeuron: IN, y: Double, training: Boolean) -> OUT)? = null,
    private val backwardImpl: ((layer: OutputLayer<IN, OUT>, inputNeuron: IN, y: Double) -> Unit)? = null,
    private val updateImpl: ((layer: OutputLayer<IN, OUT>, epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) -> Unit)? = null,
    private val initializeLayerModelDataImpl: ((layer: OutputLayer<IN, OUT>, data: String) -> Unit) = { _, _ -> },
    private val saveLayerModelDataImpl: ((layer: OutputLayer<IN, OUT>) -> String) = { Constants.String.BLANK },
) : Layer<IN, OUT>(depth, neuronCount) {

    override fun forwardImpl(inputNeuron: IN, y: Double, training: Boolean): OUT {
        return this.forwardImpl?.invoke(this, inputNeuron, y, training) ?: outputNullError()
    }

    override fun backwardImpl(inputNeuron: IN, y: Double) {
        this.backwardImpl?.invoke(this, inputNeuron, y)
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