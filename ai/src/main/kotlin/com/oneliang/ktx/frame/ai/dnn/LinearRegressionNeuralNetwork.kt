package com.oneliang.ktx.frame.ai.dnn

import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.frame.ai.dnn.layer.LinearRegressionLayer
import com.oneliang.ktx.frame.ai.dnn.layer.LinearRegressionOutputLayer
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquares
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.pojo.DoubleWrapper
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.json.jsonToArrayDouble
import com.oneliang.ktx.util.json.jsonToMap
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.math.matrix.innerProduct

object LinearRegressionNeuralNetwork : NeuralNetwork {
    private val logger = LoggerManager.getLogger((LinearRegressionNeuralNetwork::class))
    private const val DERIVED_WEIGHTS_KEY = "derivedWeights"
    private const val WEIGHTS_KEY = "weights"
    private const val SUM_KEY = "sum"

    override fun getLayerList(): List<Layer<*, *>> {
        @Suppress("UNCHECKED_CAST") val inputLayer = LinearRegressionLayer<Array<Double>, Double>(2,
            forwardImpl = { layer, dataId, inputNeuron: Array<Double>, y: Double, _: Boolean ->
                inputNeuron.innerProduct(layer.weights)
            },
            backwardImpl = { layer: LinearRegressionLayer<Array<Double>, Double>, dataId, inputNeuron: Array<Double>, y: Double ->
                val nextLayerLoss = (layer.nextLayer!! as LinearRegressionOutputLayer<Array<Double>, Double>).loss
                //derived, weight gradient descent, sum all weight grad for every x, use for average weight grad
                layer.derivedWeights.operate(DERIVED_WEIGHTS_KEY, create = {
                    Array(layer.neuronCount) { xIndex ->
                        val x = inputNeuron[xIndex]
                        ordinaryLeastSquaresDerived(x, nextLayerLoss[dataId]!!)
                    }
                }, update = {
                    Array(layer.neuronCount) { xIndex ->
                        val x = inputNeuron[xIndex]
                        it[xIndex] + ordinaryLeastSquaresDerived(x, nextLayerLoss[dataId]!!)
                    }
                })
//                layer.inputNeuron.forEachIndexed { xIndex, x ->
//                    val loss = layer.loss.getOrPut(dataId) { Array(layer.neuronCount) { 0.0 } }
//                    loss[xIndex] += ordinaryLeastSquaresDerived(x, nextLayerLoss[dataId]!!)//because next layer only one loss value
//                }
            },
            updateImpl = { layer, epoch, printPeriod, totalDataSize: Long, learningRate: Double, training: Boolean ->
                //update all weight, gradient descent
                val derivedWeights = layer.derivedWeights[DERIVED_WEIGHTS_KEY] ?: emptyArray()
                layer.weights.forEachIndexed { index, weight ->
                    layer.weights[index] = weight - (learningRate * derivedWeights[index]) / totalDataSize
                }
                if (epoch % printPeriod == 0) {
                    logger.debug("epoch:%s, weight array:%s", epoch, layer.weights.toJson())
                }
                //reset after update
//                layer.loss.reset(0.0)
                layer.derivedWeights.clear()//reset after update per one time
            }, initializeLayerModelDataImpl = { layer, data ->
                val map = data.jsonToMap()
                val weightsData = map[WEIGHTS_KEY]?.jsonToArrayDouble()
                if (weightsData != null) {
                    layer.weights = weightsData
                }
            }, saveLayerModelDataImpl = { layer ->
                val map = mutableMapOf<String, Array<Double>>()
                map[WEIGHTS_KEY] = layer.weights
                map.toJson()
            })
        val outputLayer = LinearRegressionOutputLayer<Double, Double>(
            forwardImpl = { _, dataId, inputNeuron: Double, y: Double, training: Boolean ->
                if (!training) {//test
                    logger.info("calculate y:%s, real y:%s", inputNeuron, y)
                }
                inputNeuron
            },
            backwardImpl = { layer, dataId, inputNeuron: Double, y: Double ->
                val loss = layer.loss.getOrPut(dataId) { inputNeuron - y }
                layer.sumLoss.operate(SUM_KEY, create = {
                    DoubleWrapper(ordinaryLeastSquares(loss))
                }, update = {
                    DoubleWrapper(it.value + ordinaryLeastSquares(loss))
                })
            },
            forwardResetImpl = { layer, dataId ->
                layer.loss.remove(dataId)//remove per one data
            },
            updateImpl = { layer, epoch, printPeriod, totalDataSize: Long, learningRate: Double, training: Boolean ->
                if (epoch % printPeriod == 0) {
                    val totalLoss = layer.sumLoss[SUM_KEY]?.value ?: 0.0
                    logger.debug("epoch:%s, total loss:%s, average loss:%s", epoch, totalLoss, totalLoss / totalDataSize)
                }
                //reset after update
//                layer.loss = 0.0
//                layer.sumLoss = 0.0
                layer.sumLoss.remove(SUM_KEY)//reset after update per one time
            })
        return listOf(
            inputLayer,
            outputLayer
        )
    }
}