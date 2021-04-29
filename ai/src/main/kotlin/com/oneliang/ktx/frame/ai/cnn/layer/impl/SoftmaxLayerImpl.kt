package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.activation.softmax
import com.oneliang.ktx.frame.ai.cnn.layer.DropoutLayer
import com.oneliang.ktx.frame.ai.cnn.layer.LocalResponseNormalizationLayer
import com.oneliang.ktx.frame.ai.cnn.layer.SoftmaxLayer
import com.oneliang.ktx.frame.ai.dnn.SoftmaxRegressionNeuralNetwork
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.frame.ai.dnn.layer.LossLayer
import com.oneliang.ktx.frame.ai.dnn.layer.SoftmaxRegressionOutputLayer
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.math.matrix.matrixMultiply
import com.oneliang.ktx.util.math.matrix.multiply

open class SoftmaxLayerImpl(neuronCount: Int, typeCount: Int) : SoftmaxLayer<Array<Double>, Array<Double>, Array<Array<Double>>>(neuronCount, typeCount) {

    companion object {
        private const val DERIVED_WEIGHTS_KEY = "derivedWeights"
    }

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double, training: Boolean): Array<Double> {
        if (inputNeuron.isEmpty()) {
            error("input neuron error, data size:[%s]".format(inputNeuron.size))
        }
        if (this.weights.isEmpty()) {
            this.weights = Array(this.neuronCount) { Array(this.typeCount) { 0.001 } }
        }
//        println(inputNeuron.toJson())
//        println(softmax(inputNeuron, this.weights).toJson())
        return softmax(inputNeuron, this.weights)
    }

    @Suppress("UNCHECKED_CAST")
    override fun backwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double) {
        val nextLayerLoss = getNextLayerInputNeuronLoss<Array<Array<Double>>>(dataId)
//        println("-----softmax-----")
//        println(inputNeuron.toJson())
//        println(this.weights.toJson())
//        println(nextLayerLoss.toJson())
        //calculate loss
//        val inputNeuronLoss = this.weights.multiply(nextLayerLoss)//only one loss, after calculate, transform to inputNeuronCount*1 matrix
//        this.inputNeuronLoss[dataId] = inputNeuronLoss//[*][1]
        this.inputNeuronLoss[dataId] = nextLayerLoss

        //derived, weight gradient descent, sum all weight grad for every x, use for average weight grad
        this.derivedWeights.operate(DERIVED_WEIGHTS_KEY, create = {
            Array(this.neuronCount) { xIndex ->
                val x = inputNeuron[xIndex]
                Array(this.typeCount) { typeIndex ->
                    ordinaryLeastSquaresDerived(x, nextLayerLoss[typeIndex][0])
                }
            }
        }, update = {
            Array(this.neuronCount) { xIndex ->
                val x = inputNeuron[xIndex]
                Array(this.typeCount) { typeIndex ->
                    it[xIndex][typeIndex] + ordinaryLeastSquaresDerived(x, nextLayerLoss[typeIndex][0])
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

fun main() {
    val inputNeuron = arrayOf(1.0, 1.0, 1.0)
    val weights = Array(3) { Array(3) { 0.01 } }
    val probability = arrayOf(1.0, 0.0, 0.0)
    val results = softmax(inputNeuron, weights)
    println(results.toJson())
    val loss = Array(3) { 0.0 }
    singleIteration(3) { typeIndex ->
        loss[typeIndex] = results[typeIndex] - probability[typeIndex]
    }
    println(loss.toJson())
}