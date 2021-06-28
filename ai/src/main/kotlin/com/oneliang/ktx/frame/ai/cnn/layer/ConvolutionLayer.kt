package com.oneliang.ktx.frame.ai.cnn.layer

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.calculateOutSize
import com.oneliang.ktx.frame.ai.dnn.layer.LossLayer

open class ConvolutionLayer<IN : Any, OUT : Any, LOSS : IN>(
    val previousLayerMapDepth: Int,//1
    val mapDepth: Int,//32
    val inX: Int,
    val inY: Int,
    val filterX: Int,
    val filterY: Int,
    val padding: Int = 0,
    val stride: Int = 1,
    private val forwardImpl: ((layer: ConvolutionLayer<IN, OUT, LOSS>, dataId: Long, inputNeuron: IN, y: Float, training: Boolean) -> OUT)? = null,
    private val backwardImpl: ((layer: ConvolutionLayer<IN, OUT, LOSS>, dataId: Long, inputNeuron: IN, y: Float) -> Unit)? = null,
    private val forwardResetImpl: ((layer: ConvolutionLayer<IN, OUT, LOSS>, dataId: Long) -> Unit)? = null,
    private val updateImpl: ((layer: ConvolutionLayer<IN, OUT, LOSS>, epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) -> Unit)? = null,
    private val initializeLayerModelDataImpl: ((layer: ConvolutionLayer<IN, OUT, LOSS>, data: String) -> Unit) = { _, _ -> },
    private val saveLayerModelDataImpl: ((layer: ConvolutionLayer<IN, OUT, LOSS>) -> String) = { Constants.String.BLANK }
) : LossLayer<IN, OUT, LOSS>() {

    var filters = Array(this.previousLayerMapDepth) { Array(this.mapDepth) { Array(this.filterY) { Array(this.filterX) { 0.0f } } } }
    var biases = Array(this.mapDepth) { 0.0f }
    val outX = calculateOutSize(inX, padding, filterX, stride)
    val outY = calculateOutSize(inY, padding, filterY, stride)

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