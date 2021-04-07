package com.oneliang.ktx.frame.ai.dnn

import com.oneliang.ktx.frame.ai.dnn.layer.LinearRegressionLayer
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.frame.ai.dnn.layer.LinearRegressionOutputLayer
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquares
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.util.common.reset
import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.json.*
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.math.matrix.innerProduct

object LinearRegressionNeuralNetwork : NeuralNetwork {
    private val logger = LoggerManager.getLogger((LinearRegressionNeuralNetwork::class))

    override fun getLayerList(): List<Layer<*, *>> {
        @Suppress("UNCHECKED_CAST") val inputLayer = LinearRegressionLayer<Array<Double>, Double>(2,
            forwardImpl = { layer, inputNeuron: Array<Double>, y: Double, _: Boolean ->
                inputNeuron.innerProduct(layer.weights)
            },
            backwardImpl = { layer: LinearRegressionLayer<Array<Double>, Double>, inputNeuron: Array<Double>, y: Double ->
                val loss = (layer.nextLayer!! as LinearRegressionOutputLayer<Array<Double>, Double>).loss
                //derived, weight gradient descent, sum all weight grad for every x, use for average weight grad
                layer.inputNeuron.forEachIndexed { xIndex, x ->
                    layer.loss[xIndex] += ordinaryLeastSquaresDerived(x, loss[0])//because next layer only one loss value
                }
                layer.sumLoss += ordinaryLeastSquares(loss[0])
            },
            updateImpl = { layer, epoch, printPeriod, totalDataSize: Long, learningRate: Double ->
                //update all weight, gradient descent
                layer.weights.forEachIndexed { index, weight ->
                    layer.weights[index] = weight - (learningRate * layer.loss[index]) / totalDataSize
                }
                if (epoch % printPeriod == 0) {
                    val totalLoss = layer.sumLoss
                    logger.debug("epoch:%s, total loss:%s, average loss:%s, weight array:%s", epoch, totalLoss, totalLoss / totalDataSize, layer.weights.toJson())
                }
                //reset after update
                layer.loss.reset(0.0)
                layer.sumLoss = 0.0
            }, initializeLayerModelDataImpl = { layer, data ->
                val map = data.jsonToMap()
                val weightsData = map["weights"]?.jsonToArrayDouble()
                if (weightsData != null) {
                    layer.weights = weightsData
                }
            }, saveLayerModelDataImpl = { layer ->
                val map = mutableMapOf<String, Array<Double>>()
                map["weights"] = layer.weights
                map.toJson()
            })
        val outputLayer = LinearRegressionOutputLayer<Double, Double>(1,
            forwardImpl = { _, inputNeuron: Double, y: Double, training: Boolean ->
                if (!training) {//test
                    logger.info("calculate y:%s, real y:%s", inputNeuron, y)
                }
                inputNeuron
            },
            backwardImpl = { layer, inputNeuron: Double, y: Double ->
                singleIteration(layer.neuronCount) { neuronIndex ->
                    layer.loss[neuronIndex] = (inputNeuron - y)
                }
            })
        return listOf(
            inputLayer,
            outputLayer
        )
    }
}