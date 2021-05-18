package com.oneliang.ktx.frame.ai.dnn

import com.oneliang.ktx.frame.ai.activation.softmax
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.frame.ai.dnn.layer.SoftmaxRegressionLayer
import com.oneliang.ktx.frame.ai.dnn.layer.SoftmaxRegressionOutputLayer
import com.oneliang.ktx.frame.ai.loss.likelihood
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.pojo.FloatWrapper
import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.json.jsonToMap
import com.oneliang.ktx.util.json.jsonToObjectList
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager

object SoftmaxRegressionNeuralNetwork : NeuralNetwork {
    private val logger = LoggerManager.getLogger((SoftmaxRegressionNeuralNetwork::class))
    private const val DERIVED_WEIGHTS_KEY = "derivedWeights"
    private const val WEIGHTS_KEY = "weights"
    private const val SUM_KEY = "sum"

    override fun getLayerList(): List<Layer<*, *>> {
        val typeCount = 4
        val correctProbability = Array(typeCount) { Array(typeCount) { 0.0f } }
        for (type in 0 until typeCount) {
            correctProbability[type][type] = 1.0f
        }
        @Suppress("UNCHECKED_CAST") val inputLayer = SoftmaxRegressionLayer<Array<Float>, Array<Float>>(3, typeCount,
            forwardImpl = { layer, dataId, inputNeuron: Array<Float>, y: Float, _: Boolean ->
                softmax(inputNeuron, layer.weights)
            },
            backwardImpl = { layer: SoftmaxRegressionLayer<Array<Float>, Array<Float>>, dataId, inputNeuron: Array<Float>, y: Float ->
                val nextLayerLoss = (layer.nextLayer!! as SoftmaxRegressionOutputLayer<Array<Float>, Float>).loss
                //derived, weight gradient descent, sum all weight grad for every x, use for average weight grad
                layer.derivedWeights.operate(DERIVED_WEIGHTS_KEY, create = {
                    Array(layer.neuronCount) { xIndex ->
                        val x = inputNeuron[xIndex]
                        Array(layer.typeCount) { typeIndex ->
                            ordinaryLeastSquaresDerived(x, nextLayerLoss[dataId]!![typeIndex])
                        }
                    }
                }, update = {
                    Array(layer.neuronCount) { xIndex ->
                        val x = inputNeuron[xIndex]
                        Array(layer.typeCount) { typeIndex ->
                            it[xIndex][typeIndex] + ordinaryLeastSquaresDerived(x, nextLayerLoss[dataId]!![typeIndex])
                        }
                    }
                })

//                inputNeuron.forEachIndexed { xIndex, x ->
//                    for (typeIndex in layer.loss[xIndex].indices) {
//                        layer.loss[xIndex][typeIndex] += ordinaryLeastSquaresDerived(x, nextLayerLoss[typeIndex])
//                    }
//                }
            },
            updateImpl = { layer, epoch, printPeriod, totalDataSize: Long, learningRate: Float ->
                //update all weight, gradient descent
                val derivedWeights = layer.derivedWeights[DERIVED_WEIGHTS_KEY] ?: emptyArray()
                layer.weights.forEachIndexed { index, weight ->
                    for (position in weight.indices) {
                        layer.weights[index][position] = weight[position] - (learningRate * derivedWeights[index][position]) / totalDataSize
                    }
                }
                if (epoch % printPeriod == 0) {
                    logger.debug("epoch:%s, weight array:%s", epoch, layer.weights.toJson())
                }
                //reset after update
//                layer.loss.reset(0.0)
                layer.derivedWeights.clear()//reset after update per one time
            }, initializeLayerModelDataImpl = { layer, data ->
                val map = data.jsonToMap()
                val weightsData = map[WEIGHTS_KEY]?.jsonToObjectList(Array<Float>::class)?.toTypedArray()
                if (weightsData != null) {
                    layer.weights = weightsData
                }
            }, saveLayerModelDataImpl = { layer ->
                val map = mutableMapOf<String, Array<Array<Float>>>()
                map[WEIGHTS_KEY] = layer.weights
                map.toJson()
            })
        val outputLayer = SoftmaxRegressionOutputLayer<Array<Float>, Array<Float>>(typeCount,
            forwardImpl = { _, dataId, inputNeuron: Array<Float>, y: Float, training: Boolean ->
                if (!training) {//test
                    val correctYType = y.toInt()
                    logger.info("calculate y:%s, real y:%s, calculate probability:%s", inputNeuron[correctYType], y, inputNeuron.toJson())
                }
                inputNeuron
            },
            backwardImpl = { layer, dataId, inputNeuron: Array<Float>, y: Float ->
                val loss = layer.loss.getOrPut(dataId) { Array(layer.typeCount) { 0.0f } }
                val correctYType = y.toInt()
                singleIteration(layer.typeCount) { typeIndex ->
                    loss[typeIndex] = inputNeuron[typeIndex] - correctProbability[correctYType][typeIndex]
                }
                val calculateYProbability = inputNeuron[correctYType]
                layer.sumLoss.operate(SUM_KEY, create = {
                    FloatWrapper(likelihood(calculateYProbability))
                }, update = {
                    FloatWrapper(it.value + likelihood(calculateYProbability))
                })
            },
            forwardResetImpl = { layer, dataId ->
                layer.loss.remove(dataId)//remove per one data
            },
            updateImpl = { layer, epoch, printPeriod, totalDataSize: Long, learningRate: Float ->
                if (epoch % printPeriod == 0) {
                    val totalLoss = layer.sumLoss[SUM_KEY]?.value ?: 0.0f
                    logger.debug("epoch:%s, total loss:%s, average loss:%s", epoch, totalLoss, totalLoss / totalDataSize)
                }
                //reset after update
//                layer.loss.reset(0.0)
//                layer.sumLoss = 0.0
                layer.sumLoss.remove(SUM_KEY)//reset after update per one time
            })
        return listOf(
            inputLayer,
            outputLayer
        )
    }
}