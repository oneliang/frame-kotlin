package com.oneliang.ktx.frame.ai.dnn.layer

import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap

abstract class Layer<IN : Any, OUT : Any> {

    companion object {
        private val logger = LoggerManager.getLogger(Layer::class)
    }

    open var inputNeuronMap = ConcurrentHashMap<Long, IN>()
    open var outputNeuronMap = ConcurrentHashMap<Long, OUT>()

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

    /**
     * do forward
     * invoke per input data
     */
    fun doForward(dataId: Long, inputNeuron: IN, y: Float, training: Boolean) {
        logger.verbose("doing forward, %s", this)
        this.inputNeuronMap[dataId] = inputNeuron
        val outputNeuron = forwardImpl(dataId, inputNeuron, y, training)
        this.outputNeuronMap[dataId] = outputNeuron
        val nextLayer = this.nextLayer
        nextLayer?.doForward(dataId, outputNeuron, y, training)
    }

    protected abstract fun forwardImpl(dataId: Long, inputNeuron: IN, y: Float, training: Boolean): OUT

    /**
     * do backward
     * invoke per input data
     */
    fun doBackward(dataId: Long, y: Float) {
        logger.verbose("doing backward, %s", this)
        backwardImpl(dataId, this.inputNeuronMap[dataId]!!, y)
        this.previousLayer?.doBackward(dataId, y)
    }

    protected abstract fun backwardImpl(dataId: Long, inputNeuron: IN, y: Float)

    /**
     * do forward reset
     * invoke per input data
     */
    fun doForwardRest(dataId: Long) {
        forwardResetImpl(dataId)
        this.inputNeuronMap.remove(dataId)
        this.outputNeuronMap.remove(dataId)
        val nextLayer = this.nextLayer
        nextLayer?.doForwardRest(dataId)
    }

    protected abstract fun forwardResetImpl(dataId: Long)

    /**
     * check loss before update, invoke one time for one train
     */
    fun checkLoss(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float): Pair<Boolean, Float> {
        return checkLossImpl(epoch, printPeriod, totalDataSize, learningRate)
    }

    protected open fun checkLossImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float): Pair<Boolean, Float> = true to 0f

    /**
     * invoke one time for one train
     */
    fun update(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) {
        updateImpl(epoch, printPeriod, totalDataSize, learningRate)
    }

    protected abstract fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float)

    /**
     * invoke one time for one train, after update
     */
    fun afterUpdate(epoch: Int, diffLoss: Float, printPeriod: Int, totalDataSize: Long, learningRate: Float) {
        afterUpdateImpl(epoch, diffLoss, printPeriod, totalDataSize, learningRate)
    }

    protected open fun afterUpdateImpl(epoch: Int, diffLoss: Float, printPeriod: Int, totalDataSize: Long, learningRate: Float) {}

    /**
     * on error, invoke one time for one train
     */
    fun onError(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) {
        onErrorImpl(epoch, printPeriod, totalDataSize, learningRate)
    }

    protected open fun onErrorImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) {}

    /**
     * get layer model data
     */
    fun getLayerModelData(): String {
        return saveLayerModelDataImpl()
    }

    protected abstract fun saveLayerModelDataImpl(): String

    fun initializeLayerModelData(data: String) {
        return initializeLayerModelDataImpl(data)
    }

    protected abstract fun initializeLayerModelDataImpl(data: String)

    protected fun outputNullError(): Nothing = error("out can not be null")

    /**
     * invoke one time for one test
     */
    fun testProcess(totalDataSize: Long) {
        testProcessImpl(totalDataSize)
    }

    protected open fun testProcessImpl(totalDataSize: Long) {}
}