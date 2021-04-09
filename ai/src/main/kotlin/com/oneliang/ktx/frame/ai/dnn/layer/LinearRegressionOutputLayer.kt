package com.oneliang.ktx.frame.ai.dnn.layer

import com.oneliang.ktx.Constants
import com.oneliang.ktx.pojo.DoubleWrapper
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap

class LinearRegressionOutputLayer<IN : Any, OUT : Any>(
    private val forwardImpl: ((layer: LinearRegressionOutputLayer<IN, OUT>, dataId: Long, inputNeuron: IN, y: Double, training: Boolean) -> OUT)? = null,
    private val backwardImpl: ((layer: LinearRegressionOutputLayer<IN, OUT>, dataId: Long, inputNeuron: IN, y: Double) -> Unit)? = null,
    private val updateImpl: ((layer: LinearRegressionOutputLayer<IN, OUT>, epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) -> Unit)? = null,
    private val initializeLayerModelDataImpl: ((layer: LinearRegressionOutputLayer<IN, OUT>, data: String) -> Unit) = { _, _ -> },
    private val saveLayerModelDataImpl: ((layer: LinearRegressionOutputLayer<IN, OUT>) -> String) = { Constants.String.BLANK },
) : Layer<IN, OUT>() {

    var loss = 0.0
    var sumLoss = 0.0

    override fun forwardImpl(dataId: Long, inputNeuron: IN, y: Double, training: Boolean): OUT {
        return this.forwardImpl?.invoke(this, dataId, inputNeuron, y, training) ?: outputNullError()
    }

    override fun backwardImpl(dataId: Long, inputNeuron: IN, y: Double) {
        this.backwardImpl?.invoke(this, dataId, inputNeuron, y)
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