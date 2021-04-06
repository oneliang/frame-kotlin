package com.oneliang.ktx.frame.ai.dnn

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.dnn.layer.FullConnectedLayer
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.frame.ai.dnn.layer.OutputLayer
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquares
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.util.common.reset
import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.json.*
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.math.matrix.multiplyAndOperate

class LinearRegressionNeuralNetwork : NeuralNetwork {
    companion object {
        private val logger = LoggerManager.getLogger((LinearRegressionNeuralNetwork::class))
    }

    override fun getLayerList(): List<Layer<*, *>> {
        val inputLayer = FullConnectedLayer<Array<Double>, Array<Double>>(1, 2,
            forwardImpl = { layer, inputNeuron: Array<Double>, y: Double, _: Boolean ->
                val calculateY = inputNeuron.multiplyAndOperate(layer.weights, resultOperate = { result: Double, matrixValue: Double ->
                    result + matrixValue
                })
                Array(1) { calculateY }
            },
            backwardImpl = { layer: FullConnectedLayer<Array<Double>, Array<Double>>, inputNeuron: Array<Double>, y: Double ->
                val loss = layer.nextLayer!!.loss
                //derived, weight gradient descent, sum all weight grad for every x, use for average weight grad
                layer.inputNeuron.forEachIndexed { xIndex, x ->
                    layer.loss[0][xIndex] += ordinaryLeastSquaresDerived(x, loss[0][0])//because next layer only one loss value
                }
                singleIteration(layer.depth) { depthIndex ->
                    layer.sumLoss[depthIndex] += ordinaryLeastSquares(loss[0][0])
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
                    val totalLoss = layer.sumLoss[0]
                    logger.debug("epoch:%s, total loss:%s, average loss:%s, weight array:%s", epoch, totalLoss, totalLoss / totalDataSize, layer.weights.toJson())
                }
                //reset after update
                layer.loss.reset(0.0)
                layer.sumLoss.reset(0.0)
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
        val outputLayer = OutputLayer<Array<Double>, Double>(1, 1,
            forwardImpl = { _, inputNeuron: Array<Double>, y: Double, training: Boolean ->
                if (!training) {//test
                    logger.info("calculate y:%s, real y:%s", inputNeuron.toJson(), y)
                }
                y
            },
            backwardImpl = { layer, inputNeuron: Array<Double>, y: Double ->
                singleIteration(layer.depth) { depthIndex ->
                    val calculateY = inputNeuron[0]
                    singleIteration(layer.neuronCount) { neuronIndex ->
                        layer.loss[depthIndex][neuronIndex] = (calculateY - y)
                    }
                }
            })
        return listOf(
            inputLayer,
            outputLayer
        )
    }
}