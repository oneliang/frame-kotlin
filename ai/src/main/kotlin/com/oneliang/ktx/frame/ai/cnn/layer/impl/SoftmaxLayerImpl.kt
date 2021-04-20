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
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap

open class SoftmaxLayerImpl(neuronCount: Int, typeCount: Int) : SoftmaxLayer<Array<Double>, Array<Double>, Array<Double>>(neuronCount, typeCount) {

    companion object {
        private const val DERIVED_WEIGHTS_KEY = "derivedWeights"
    }

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double, training: Boolean): Array<Double> {
        return softmax(inputNeuron, this.weights)
    }

    @Suppress("UNCHECKED_CAST")
    override fun backwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double) {
        val nextLayerLoss = (this.nextLayer!! as LossLayer<*, *, Array<Double>>).inputNeuronLoss
        //derived, weight gradient descent, sum all weight grad for every x, use for average weight grad
        this.derivedWeights.operate(DERIVED_WEIGHTS_KEY, create = {
            Array(this.neuronCount) { xIndex ->
                val x = inputNeuron[xIndex]
                Array(this.typeCount) { typeIndex ->
                    ordinaryLeastSquaresDerived(x, nextLayerLoss[dataId]!![typeIndex])
                }
            }
        }, update = {
            Array(this.neuronCount) { xIndex ->
                val x = inputNeuron[xIndex]
                Array(this.typeCount) { typeIndex ->
                    it[xIndex][typeIndex] + ordinaryLeastSquaresDerived(x, nextLayerLoss[dataId]!![typeIndex])
                }
            }
        })
    }

    override fun forwardResetImpl(dataId: Long) {
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) {
    }

    override fun initializeLayerModelDataImpl(data: String) {
    }

    override fun saveLayerModelDataImpl(): String {
        return Constants.String.BLANK
    }
}