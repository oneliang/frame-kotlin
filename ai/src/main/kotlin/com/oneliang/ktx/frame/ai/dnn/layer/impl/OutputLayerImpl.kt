package com.oneliang.ktx.frame.ai.dnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.dnn.layer.OutputLayer
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquares
import com.oneliang.ktx.pojo.DoubleWrapper
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.math.matrix.transpose

/**
 * this output layer, input neuron equal output neuron, so loss named inputNeuronLoss
 */
class OutputLayerImpl(neuronCount: Int) : OutputLayer<Array<Double>, Array<Double>, Array<Array<Double>>>(neuronCount) {
    companion object {
        private val logger = LoggerManager.getLogger(OutputLayerImpl::class)
        private const val SUM_KEY = "sum"
    }

    //coroutine concurrent, use for all data in layer
    var sumLoss = AtomicMap<String, DoubleWrapper>()

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double, training: Boolean): Array<Double> {
//        println("-----forward-----" + this.inputNeuronMap[dataId]?.toJson() + "," + inputNeuron.toJson())
        if (!training) {//test
            logger.info("calculate y:%s, real y:%s", inputNeuron.toJson(), y)
        }
        return inputNeuron
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double) {//get 0 for test, only out put one
        val loss = this.inputNeuronLoss.getOrPut(dataId) { inputNeuron.transpose { it - y } }!!//[inputNeuron.size][1]
        this.sumLoss.operate(SUM_KEY, create = {
            DoubleWrapper(loss.sumByDouble {
                ordinaryLeastSquares(it[0])
            })
        }, update = { oldDoubleWrapper ->
            DoubleWrapper(oldDoubleWrapper.value + loss.sumByDouble {
                ordinaryLeastSquares(it[0])
            })
        })
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double, training: Boolean) {
        if (epoch % printPeriod == 0) {
            val totalLoss = this.sumLoss[SUM_KEY]?.value ?: 0.0
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