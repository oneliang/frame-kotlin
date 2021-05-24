package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.frame.ai.activation.softmax
import com.oneliang.ktx.frame.ai.cnn.layer.SoftmaxLayer
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.json.jsonToMap
import com.oneliang.ktx.util.json.jsonToObjectList
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.math.matrix.transpose

open class SoftmaxLayerImpl(
    neuronCount: Int,
    typeCount: Int,
    private val learningRate: Float = 0.0f
) : SoftmaxLayer<Array<Float>, Array<Float>, Array<Array<Float>>>(neuronCount, typeCount) {

    companion object {
        private const val DERIVED_WEIGHTS_KEY = "derivedWeights"
        private const val WEIGHTS_KEY = "weights"
        private val logger = LoggerManager.getLogger(SoftmaxLayerImpl::class)
    }

    private val correctProbability = Array(typeCount) { Array(typeCount) { 0.0f } }

    init {
        for (type in 0 until typeCount) {
            this.correctProbability[type][type] = 1.0f//one hot encode
        }
    }

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Float>, y: Float, training: Boolean): Array<Float> {
        if (inputNeuron.isEmpty()) {
            error("input neuron error, data size:[%s]".format(inputNeuron.size))
        }
        if (this.weights.isEmpty()) {
            this.weights = Array(this.neuronCount) { Array(this.typeCount) { 0.001f } }
        }
//        println("-----softmax forward-----")
        val outputNeuron = softmax(inputNeuron, this.weights)
//        if (dataId == 6130L) {
//            println("y:$y")
//            println("input:" + inputNeuron.toJson())
//            println("weights:" + this.weights.toJson())
//            println("output:" + outputNeuron.toJson())
//        }
        return outputNeuron
    }

    @Suppress("UNCHECKED_CAST")
    override fun backwardImpl(dataId: Long, inputNeuron: Array<Float>, y: Float) {
        val outputNeuron = this.outputNeuronMap[dataId]!!
        val loss = Array(this.typeCount) { 0.0f }
        val correctYType = y.toInt()
        singleIteration(this.typeCount) { typeIndex ->
            loss[typeIndex] = outputNeuron[typeIndex] - this.correctProbability[correctYType][typeIndex]
        }

        val outputLoss = loss.transpose()//[*][1]
        this.inputNeuronLoss[dataId] = outputLoss
//        println("-----softmax backward-----")
//        println("output loss:" + outputLoss.toJson())


        //derived, weight gradient descent, sum all weight grad for every x, use for average weight grad
        this.derivedWeights.operate(DERIVED_WEIGHTS_KEY, create = {
            Array(this.neuronCount) { xIndex ->
                val x = inputNeuron[xIndex]
                Array(this.typeCount) { typeIndex ->
                    ordinaryLeastSquaresDerived(x, outputLoss[typeIndex][0])
                }
            }
        }, update = {
            Array(this.neuronCount) { xIndex ->
                val x = inputNeuron[xIndex]
                Array(this.typeCount) { typeIndex ->
                    it[xIndex][typeIndex] + ordinaryLeastSquaresDerived(x, outputLoss[typeIndex][0])
                }
            }
        })
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) {
        val fixLearningRate = if (this.learningRate > 0.0f) this.learningRate else learningRate
        //update all weight, gradient descent
        val derivedWeights = this.derivedWeights[DERIVED_WEIGHTS_KEY] ?: emptyArray()
        this.weights.forEachIndexed { index, weight ->
            for (position in weight.indices) {
                this.weights[index][position] = weight[position] - (fixLearningRate * derivedWeights[index][position]) / totalDataSize
            }
        }
        if (epoch % printPeriod == 0) {
            logger.debug("epoch:%s, weight array:%s", epoch, this.weights.toJson())
        }
        //reset after update
        this.derivedWeights.clear()//reset after update per one time
    }

    override fun initializeLayerModelDataImpl(data: String) {
        val map = data.jsonToMap()
        val weightsData = map[WEIGHTS_KEY]?.jsonToObjectList(Array<Float>::class)
        if (weightsData != null) {
            this.weights = weightsData.toTypedArray()
        }
    }

    override fun saveLayerModelDataImpl(): String {
        val map = mutableMapOf<String, Array<Array<Float>>>()
        map[WEIGHTS_KEY] = this.weights
        return map.toJson()
    }
}