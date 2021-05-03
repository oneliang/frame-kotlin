package com.oneliang.ktx.frame.ai.cnn.layer

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.calculateOutSize
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.frame.ai.dnn.layer.LossLayer

open class FlattenLayer<IN : Any, OUT : Any, LOSS : Any>(
    val mapDepth: Int,//32
    private val forwardImpl: ((layer: FlattenLayer<IN, OUT, LOSS>, dataId: Long, inputNeuron: IN, y: Double, training: Boolean) -> OUT)? = null,
    private val backwardImpl: ((layer: FlattenLayer<IN, OUT, LOSS>, dataId: Long, inputNeuron: IN, y: Double) -> Unit)? = null,
    private val forwardResetImpl: ((layer: FlattenLayer<IN, OUT, LOSS>, dataId: Long) -> Unit)? = null,
    private val updateImpl: ((layer: FlattenLayer<IN, OUT, LOSS>, epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) -> Unit)? = null,
    private val initializeLayerModelDataImpl: ((layer: FlattenLayer<IN, OUT, LOSS>, data: String) -> Unit) = { _, _ -> },
    private val saveLayerModelDataImpl: ((layer: FlattenLayer<IN, OUT, LOSS>) -> String) = { Constants.String.BLANK }
) : LossLayer<IN, OUT, LOSS>() {

    override fun forwardImpl(dataId: Long, inputNeuron: IN, y: Double, training: Boolean): OUT {
        return this.forwardImpl?.invoke(this, dataId, inputNeuron, y, training) ?: outputNullError()
    }

    override fun backwardImpl(dataId: Long, inputNeuron: IN, y: Double) {
        this.backwardImpl?.invoke(this, dataId, inputNeuron, y)
    }

    override fun forwardResetImpl(dataId: Long) {
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