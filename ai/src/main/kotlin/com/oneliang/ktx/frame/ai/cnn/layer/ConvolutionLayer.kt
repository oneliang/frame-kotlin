package com.oneliang.ktx.frame.ai.cnn.layer

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.calculateOutSize
import com.oneliang.ktx.frame.ai.dnn.layer.Layer

open class ConvolutionLayer<IN : Any, OUT : Any>(
    val previousLayerMapDepth: Int,//1
    val mapDepth: Int,//32
    val inX: Int,
    val inY: Int,
    val filterX: Int,
    val filterY: Int,
    val padding: Int = 0,
    val stride: Int = 1,
    private val forwardImpl: ((layer: ConvolutionLayer<IN, OUT>, dataId: Long, inputNeuron: IN, y: Double, training: Boolean) -> OUT)? = null,
    private val backwardImpl: ((layer: ConvolutionLayer<IN, OUT>, dataId: Long, inputNeuron: IN, y: Double) -> Unit)? = null,
    private val forwardResetImpl: ((layer: ConvolutionLayer<IN, OUT>, dataId: Long) -> Unit)? = null,
    private val updateImpl: ((layer: ConvolutionLayer<IN, OUT>, epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double, training: Boolean) -> Unit)? = null,
    private val initializeLayerModelDataImpl: ((layer: ConvolutionLayer<IN, OUT>, data: String) -> Unit) = { _, _ -> },
    private val saveLayerModelDataImpl: ((layer: ConvolutionLayer<IN, OUT>) -> String) = { Constants.String.BLANK }
) : Layer<IN, OUT>() {

    var filters = Array(this.previousLayerMapDepth) { Array(this.mapDepth) { Array(this.filterY) { Array(this.filterX) { 0.0 } } } }
    var biases = Array(this.mapDepth) { 0.0 }
    val outX = calculateOutSize(inX, padding, filterX, stride)
    val outY = calculateOutSize(inY, padding, filterY, stride)

    override fun forwardImpl(dataId: Long, inputNeuron: IN, y: Double, training: Boolean): OUT {
        return this.forwardImpl?.invoke(this, dataId, inputNeuron, y, training) ?: outputNullError()
    }

    override fun backwardImpl(dataId: Long, inputNeuron: IN, y: Double) {
        this.backwardImpl?.invoke(this, dataId, inputNeuron, y)
    }

    override fun forwardResetImpl(dataId: Long) {
        this.forwardResetImpl?.invoke(this, dataId)
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double, training: Boolean) {
        this.updateImpl?.invoke(this, epoch, printPeriod, totalDataSize, learningRate, training)
    }

    override fun initializeLayerModelDataImpl(data: String) {
        this.initializeLayerModelDataImpl.invoke(this, data)
    }

    override fun saveLayerModelDataImpl(): String {
        return this.saveLayerModelDataImpl.invoke(this)
    }
}