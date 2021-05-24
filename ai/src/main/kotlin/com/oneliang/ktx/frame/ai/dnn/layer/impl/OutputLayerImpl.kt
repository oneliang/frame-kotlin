package com.oneliang.ktx.frame.ai.dnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.dnn.layer.OutputLayer
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquares
import com.oneliang.ktx.pojo.FloatWrapper
import com.oneliang.ktx.util.common.sumByFloat
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.math.matrix.transpose

/**
 * this output layer, input neuron equal output neuron, so loss named inputNeuronLoss
 */
class OutputLayerImpl : OutputLayer<Array<Float>, Array<Float>, Array<Array<Float>>>() {
    companion object {
        private val logger = LoggerManager.getLogger(OutputLayerImpl::class)
        private const val SUM_KEY = "sum"
    }

    //coroutine concurrent, use for all data in layer
    var sumLoss = AtomicMap<String, FloatWrapper>()
    private var lastSumLoss = Float.MAX_VALUE

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Float>, y: Float, training: Boolean): Array<Float> {
//        println("-----forward-----" + this.inputNeuronMap[dataId]?.toJson() + "," + inputNeuron.toJson())
        if (!training) {//test
            logger.info("calculate y:%s, real y:%s", inputNeuron.toJson(), y)
        }
        return inputNeuron
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Float>, y: Float) {//get 0 for test, only out put one
        val loss = this.inputNeuronLoss.getOrPut(dataId) { inputNeuron.transpose { it - y } }!!//[inputNeuron.size][1]
        this.sumLoss.operate(SUM_KEY, create = {
            FloatWrapper(loss.sumByFloat {
                ordinaryLeastSquares(it[0])
            })
        }, update = { oldFloatWrapper ->
            FloatWrapper(oldFloatWrapper.value + loss.sumByFloat {
                ordinaryLeastSquares(it[0])
            })
        })
    }

    override fun checkLossImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float): Boolean {
        val totalLoss = this.sumLoss[SUM_KEY]?.value ?: 0.0f
        val result = totalLoss <= this.lastSumLoss
        if (!result) {
            logger.error("epoch:%s, last total loss:%s, current total loss:%s, total data size:%s", epoch, this.lastSumLoss, totalLoss, totalDataSize)
        }
        this.lastSumLoss = totalLoss
        return result
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) {
        if (epoch % printPeriod == 0) {
            val totalLoss = this.sumLoss[SUM_KEY]?.value ?: 0.0f
            logger.debug("epoch:%s, total loss:%s, average loss:%s", epoch, totalLoss, totalLoss / totalDataSize)
        }
        //reset after update
        this.sumLoss.remove(SUM_KEY)//reset after update per one time
    }

    override fun initializeLayerModelDataImpl(data: String) {
    }

    override fun saveLayerModelDataImpl(): String {
        return Constants.String.BLANK
    }
}