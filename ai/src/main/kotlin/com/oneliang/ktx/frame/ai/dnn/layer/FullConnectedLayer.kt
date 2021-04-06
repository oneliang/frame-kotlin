package com.oneliang.ktx.frame.ai.dnn.layer

import com.oneliang.ktx.Constants

class FullConnectedLayer<IN : Any, OUT : Any>(
    depth: Int,
    neuronCount: Int,
    private val forwardImpl: ((layer: FullConnectedLayer<IN, OUT>, inputNeuron: IN, y: Double, training: Boolean) -> OUT)? = null,
    private val backwardImpl: ((layer: FullConnectedLayer<IN, OUT>, inputNeuron: IN, y: Double) -> Unit)? = null,
    private val updateImpl: ((layer: FullConnectedLayer<IN, OUT>, epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) -> Unit)? = null,
    private val initializeLayerModelDataImpl: ((layer: FullConnectedLayer<IN, OUT>, data: String) -> Unit) = { _, _ -> },
    private val saveLayerModelDataImpl: ((layer: FullConnectedLayer<IN, OUT>) -> String) = { Constants.String.BLANK },
) : Layer<IN, OUT>(depth, neuronCount) {

    constructor() : this(0, 0)

    var weights: Array<Array<Double>> = Array(depth) { Array(neuronCount) { 0.0 } }
    var sumLoss: Array<Double> = Array(depth) { 0.0 }

//    var bias: Array<Double> = Array(depth) { 0.0 }

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