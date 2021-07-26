package com.oneliang.ktx.frame.ai.cnn.layer

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.dnn.layer.LossLayer

open class RectifiedLinearUnitsLayer<IN : Any, OUT : Any, LOSS : IN>(
    private val forwardImpl: ((layer: RectifiedLinearUnitsLayer<IN, OUT, LOSS>, dataId: Long, inputNeuron: IN, y: Float, training: Boolean) -> OUT)? = null,
    private val backwardImpl: ((layer: RectifiedLinearUnitsLayer<IN, OUT, LOSS>, dataId: Long, inputNeuron: IN, y: Float) -> Unit)? = null,
    private val forwardResetImpl: ((layer: RectifiedLinearUnitsLayer<IN, OUT, LOSS>, dataId: Long) -> Unit)? = null,
    private val updateImpl: ((layer: RectifiedLinearUnitsLayer<IN, OUT, LOSS>, epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) -> Unit)? = null,
    private val initializeLayerModelDataImpl: ((layer: RectifiedLinearUnitsLayer<IN, OUT, LOSS>, data: String) -> Unit) = { _, _ -> },
    private val saveLayerModelDataImpl: ((layer: RectifiedLinearUnitsLayer<IN, OUT, LOSS>) -> String) = { Constants.String.BLANK }
) : LossLayer<IN, OUT, LOSS>() {

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