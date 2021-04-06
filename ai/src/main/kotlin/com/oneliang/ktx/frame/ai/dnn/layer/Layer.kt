package com.oneliang.ktx.frame.ai.dnn.layer

abstract class Layer<IN : Any, OUT : Any>(
    var depth: Int,
    var neuronCount: Int
) {

    constructor() : this(0, 0)

    var loss: Array<Array<Double>> = Array(depth) { Array(neuronCount) { 0.0 } }

    open lateinit var inputNeuron: IN

    @Suppress("UNCHECKED_CAST")
    var nextLayer: Layer<Any, Any>? = null
        //下一层
        set(value) {
            field = value
            if (value?.previousLayer == null) {
                value?.previousLayer = this as Layer<Any, Any>
            }
        }

    @Suppress("UNCHECKED_CAST")
    var previousLayer: Layer<Any, Any>? = null
        //上一层
        set(value) {
            field = value
            if (value?.nextLayer == null) {
                value?.nextLayer = this as Layer<Any, Any>
            }
        }

    fun doForward(inputNeuron: IN, y: Double, training: Boolean) {
        this.inputNeuron = inputNeuron
        val outputNeuron = forwardImpl(inputNeuron, y, training)
        val nextLayer = this.nextLayer
        nextLayer?.doForward(outputNeuron, y, training)
    }

    protected abstract fun forwardImpl(inputNeuron: IN, y: Double, training: Boolean): OUT

    fun doBackward(y: Double) {
        backwardImpl(this.inputNeuron, y)
        this.previousLayer?.doBackward(y)
    }

    protected abstract fun backwardImpl(inputNeuron: IN, y: Double)

    fun update(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) {
        updateImpl(epoch, printPeriod, totalDataSize, learningRate)
    }

    protected abstract fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double)

    fun getLayerModelData(): String {
        return saveLayerModelDataImpl()
    }

    protected abstract fun saveLayerModelDataImpl(): String

    fun initializeLayerModelData(data: String) {
        return initializeLayerModelDataImpl(data)
    }

    protected abstract fun initializeLayerModelDataImpl(data: String)

    protected fun outputNullError(): Nothing = error("out can not be null")
}