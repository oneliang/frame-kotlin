package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.activation.softmax
import com.oneliang.ktx.frame.ai.cnn.layer.SoftmaxLayer
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.math.matrix.transpose

open class SoftmaxLayerImpl(neuronCount: Int, typeCount: Int) : SoftmaxLayer<Array<Double>, Array<Double>, Array<Array<Double>>>(neuronCount, typeCount) {

    companion object {
        private const val DERIVED_WEIGHTS_KEY = "derivedWeights"
    }

    private val correctProbability = Array(typeCount) { Array(typeCount) { 0.0 } }

    init {
        for (type in 0 until typeCount) {
            this.correctProbability[type][type] = 1.0//one hot encode
        }
    }

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double, training: Boolean): Array<Double> {
        if (inputNeuron.isEmpty()) {
            error("input neuron error, data size:[%s]".format(inputNeuron.size))
        }
        if (this.weights.isEmpty()) {
            this.weights = Array(this.neuronCount) { Array(this.typeCount) { 0.001 } }
        }
//        println("-----softmax-----")
//        println("input:" + inputNeuron.toJson())
        val outputNeuron = softmax(inputNeuron, this.weights)
//        println("output:" + outputNeuron.toJson())
        return outputNeuron
    }

    @Suppress("UNCHECKED_CAST")
    override fun backwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double) {
        val outputNeuron = this.outputNeuronMap[dataId]!!
        val loss = Array(this.typeCount) { 0.0 }
        val correctYType = y.toInt()
        singleIteration(this.typeCount) { typeIndex ->
            loss[typeIndex] = outputNeuron[typeIndex] - this.correctProbability[correctYType][typeIndex]
        }

        val outputLoss = loss.transpose()//[*][1]
        this.inputNeuronLoss[dataId] = outputLoss
        println("-----output loss-----")
        println(outputLoss.toJson())


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

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) {
    }

    override fun initializeLayerModelDataImpl(data: String) {
    }

    override fun saveLayerModelDataImpl(): String {
        return Constants.String.BLANK
    }
}