package com.oneliang.ktx.frame.ai.dnn

import com.oneliang.ktx.frame.ai.activation.softmax
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.frame.ai.dnn.layer.SoftmaxRegressionLayer
import com.oneliang.ktx.frame.ai.dnn.layer.SoftmaxRegressionOutputLayer
import com.oneliang.ktx.frame.ai.loss.likelihood
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.util.common.reset
import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.json.jsonToMap
import com.oneliang.ktx.util.json.jsonToObjectList
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager

object SoftmaxRegressionNeuralNetwork : NeuralNetwork {
    private val logger = LoggerManager.getLogger((SoftmaxRegressionNeuralNetwork::class))
    override fun getLayerList(): List<Layer<*, *>> {
        val typeCount = 4
        val correctProbability = Array(typeCount) { Array(typeCount) { 0.0 } }
        for (type in 0 until typeCount) {
            correctProbability[type][type] = 1.0
        }
        @Suppress("UNCHECKED_CAST") val inputLayer = SoftmaxRegressionLayer<Array<Double>, Array<Double>>(3, typeCount,
            forwardImpl = { layer, inputNeuron: Array<Double>, y: Double, _: Boolean ->
                softmax(inputNeuron, layer.weights)
            },
            backwardImpl = { layer: SoftmaxRegressionLayer<Array<Double>, Array<Double>>, inputNeuron: Array<Double>, y: Double ->
                val loss = (layer.nextLayer!! as SoftmaxRegressionOutputLayer<Array<Double>, Double>).loss
                //derived, weight gradient descent, sum all weight grad for every x, use for average weight grad
                inputNeuron.forEachIndexed { xIndex, x ->
                    for (typeIndex in layer.loss[xIndex].indices) {
                        layer.loss[xIndex][typeIndex] += ordinaryLeastSquaresDerived(x, loss[typeIndex])
                    }
                }
            },
            updateImpl = { layer, epoch, printPeriod, totalDataSize: Long, learningRate: Double ->
                //update all weight, gradient descent
                layer.weights.forEachIndexed { index, weight ->
                    for (position in weight.indices) {
                        layer.weights[index][position] = weight[position] - (learningRate * layer.loss[index][position]) / totalDataSize
                    }
                }
                if (epoch % printPeriod == 0) {
                    logger.debug("epoch:%s, weight array:%s", epoch, layer.weights.toJson())
                }
                //reset after update
                layer.loss.reset(0.0)
            }, initializeLayerModelDataImpl = { layer, data ->
                val map = data.jsonToMap()
                val weightsData = map["weights"]?.jsonToObjectList(Array<Double>::class)?.toTypedArray()
                if (weightsData != null) {
                    layer.weights = weightsData
                }
            }, saveLayerModelDataImpl = { layer ->
                val map = mutableMapOf<String, Array<Array<Double>>>()
                map["weights"] = layer.weights
                map.toJson()
            })
        val outputLayer = SoftmaxRegressionOutputLayer<Array<Double>, Array<Double>>(typeCount,
            forwardImpl = { _, inputNeuron: Array<Double>, y: Double, training: Boolean ->
                if (!training) {//test
                    logger.info("calculate y:%s, real y:%s", inputNeuron.toJson(), y)
                }
                inputNeuron
            },
            backwardImpl = { layer, inputNeuron: Array<Double>, y: Double ->
                val correctYType = y.toInt()
                singleIteration(layer.typeCount) { typeIndex ->
                    layer.loss[typeIndex] = inputNeuron[typeIndex] - correctProbability[correctYType][typeIndex]
                }
                val calculateYProbability = inputNeuron[correctYType]
                layer.sumLoss += likelihood(calculateYProbability)
            },
            updateImpl = { layer, epoch, printPeriod, totalDataSize: Long, learningRate: Double ->
                if (epoch % printPeriod == 0) {
                    val totalLoss = layer.sumLoss
                    logger.debug("epoch:%s, total loss:%s, average loss:%s", epoch, totalLoss, totalLoss / totalDataSize)
                }
                //reset after update
                layer.loss.reset(0.0)
                layer.sumLoss = 0.0
            })
        return listOf(
            inputLayer,
            outputLayer
        )
    }
}