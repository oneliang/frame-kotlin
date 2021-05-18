package com.oneliang.ktx.frame.ai.dnn.layer

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap

class SoftmaxRegressionLayer<IN : Any, OUT : Any>(
    val neuronCount: Int,
    val typeCount: Int,
    private val forwardImpl: ((layer: SoftmaxRegressionLayer<IN, OUT>, dataId: Long, inputNeuron: IN, y: Float, training: Boolean) -> OUT)? = null,
    private val backwardImpl: ((layer: SoftmaxRegressionLayer<IN, OUT>, dataId: Long, inputNeuron: IN, y: Float) -> Unit)? = null,
    private val forwardResetImpl: ((layer: SoftmaxRegressionLayer<IN, OUT>, dataId: Long) -> Unit)? = null,
    private val updateImpl: ((layer: SoftmaxRegressionLayer<IN, OUT>, epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) -> Unit)? = null,
    private val initializeLayerModelDataImpl: ((layer: SoftmaxRegressionLayer<IN, OUT>, data: String) -> Unit) = { _, _ -> },
    private val saveLayerModelDataImpl: ((layer: SoftmaxRegressionLayer<IN, OUT>) -> String) = { Constants.String.BLANK },
) : Layer<IN, OUT>() {

    constructor() : this(0, 0)

    var derivedWeights = AtomicMap<String, Array<Array<Float>>>()//: Array<Array<Float>> = Array(neuronCount) { Array(this.typeCount) { 0.0 } }
    var weights: Array<Array<Float>> = Array(neuronCount) { Array(this.typeCount) { 0.0f } }

    override fun forwardImpl(dataId: Long, inputNeuron: IN, y: Float, training: Boolean): OUT {
        return this.forwardImpl?.invoke(this, dataId, inputNeuron, y, training) ?: outputNullError()
    }

    override fun backwardImpl(dataId: Long, inputNeuron: IN, y: Float) {
        this.backwardImpl?.invoke(this, dataId, inputNeuron, y)
    }

    override fun forwardResetImpl(dataId: Long) {
        this.forwardResetImpl?.invoke(this, dataId)
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) {
        this.updateImpl?.invoke(this, epoch, printPeriod, totalDataSize, learningRate)
    }

    override fun initializeLayerModelDataImpl(data: String) {
        this.initializeLayerModelDataImpl.invoke(this, data)
    }

    override fun saveLayerModelDataImpl(): String {
        return this.saveLayerModelDataImpl.invoke(this)
    }
}