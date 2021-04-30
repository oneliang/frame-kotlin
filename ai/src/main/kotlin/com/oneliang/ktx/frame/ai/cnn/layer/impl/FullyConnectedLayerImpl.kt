package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.frame.ai.cnn.layer.FullyConnectedLayer
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.json.jsonToMap
import com.oneliang.ktx.util.json.jsonToObjectList
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.math.matrix.multiply

open class FullyConnectedLayerImpl(
    neuronCount: Int//32
) : FullyConnectedLayer<Array<Double>, Array<Double>, Array<Array<Double>>>(neuronCount) {

    companion object {
        private val logger = LoggerManager.getLogger(FullyConnectedLayerImpl::class)
        private const val DERIVED_WEIGHTS_KEY = "derivedWeights"
        private const val WEIGHTS_KEY = "weights"
    }

    //coroutine concurrent, use for all data in layer
    var derivedWeights = AtomicMap<String, Array<Array<Double>>>()//Array(this.neuronCount) { 0.0 }
    var weights: Array<Array<Double>> = emptyArray()//inputMapDepth * mapDepth

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double, training: Boolean): Array<Double> {
        if (inputNeuron.isEmpty()) {
            error("input neuron error, data size:[%s]".format(inputNeuron.size))
        }

        if (this.weights.isEmpty()) {
            this.weights = Array(inputNeuron.size) { Array(this.neuronCount) { 0.1 } }
        }
        val outputNeuron = inputNeuron.multiply(this.weights)
        return outputNeuron
    }

    @Suppress("UNCHECKED_CAST")
    override fun backwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double) {
//add bias when support bias
        val newInputNeuron = inputNeuron
        //out put loss
        val nextLayerLoss = getNextLayerInputNeuronLoss<Array<Array<Double>>>(dataId)
        println(this.weights.toJson() + ", next layer loss:" + nextLayerLoss.toJson())
        //update current layer input neuron loss
        val inputNeuronLoss = this.weights.multiply(nextLayerLoss)//only one loss, after calculate, transform to inputNeuronCount*1 matrix
//        inputNeuronLoss.printToMatrix()
        this.inputNeuronLoss[dataId] = inputNeuronLoss

//        println("-----back-----" + this.inputNeuronMap[dataId]?.toJson() + "," + this.inputNeuronLoss[dataId]?.toJson())
//        println("input size:${inputNeuron.size}, out put size:${this.neuronCount}, next layer loss:${nextLayerLoss.toJson()}")

        //derived, weight gradient descent, sum all weight grad for every x, use for average weight grad
        this.derivedWeights.operate(DERIVED_WEIGHTS_KEY, create = {
            Array(newInputNeuron.size) { xIndex ->
                val x = newInputNeuron[xIndex]
                Array(this.neuronCount) { outputNeuronIndex ->
//                    println("x:$x, derived:" + ordinaryLeastSquaresDerived(x, nextLayerLoss[outputNeuronIndex][0]))
                    ordinaryLeastSquaresDerived(x, nextLayerLoss[outputNeuronIndex][0])
                }
            }
        }, update = { oldDerivedWeights ->
            Array(newInputNeuron.size) { xIndex ->
                val x = newInputNeuron[xIndex]
                Array(this.neuronCount) { outputNeuronIndex ->
                    oldDerivedWeights[xIndex][outputNeuronIndex] + ordinaryLeastSquaresDerived(x, nextLayerLoss[outputNeuronIndex][0])
                }
            }
        })
    }

    override fun forwardResetImpl(dataId: Long) {
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Double) {
        //update all weight, gradient descent
        val derivedWeights = this.derivedWeights[DERIVED_WEIGHTS_KEY] ?: emptyArray()
        this.weights.forEachIndexed { weightIndex, outputNeuronWeightArray ->
            outputNeuronWeightArray.forEachIndexed { outputNeuronIndex, weight ->
                this.weights[weightIndex][outputNeuronIndex] = weight - (learningRate * derivedWeights[weightIndex][outputNeuronIndex]) / totalDataSize
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
        val weightsData = map[WEIGHTS_KEY]?.jsonToObjectList(Array<Double>::class)
        if (weightsData != null) {
            this.weights = weightsData.toTypedArray()
        }
    }

    override fun saveLayerModelDataImpl(): String {
        val map = mutableMapOf<String, Array<Array<Double>>>()
        map[WEIGHTS_KEY] = this.weights
        return map.toJson()
    }
}