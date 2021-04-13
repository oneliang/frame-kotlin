package com.oneliang.ktx.frame.ai.dnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.printToMatrix
import com.oneliang.ktx.frame.ai.dnn.layer.FullyConnectedLayer
import com.oneliang.ktx.frame.ai.dnn.layer.OutputLayer
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.math.matrix.multiply
import java.util.concurrent.ConcurrentHashMap

class FullyConnectedLayerImpl(
    neuronCount: Int,
) : FullyConnectedLayer<Array<Double>, Array<Double>>(neuronCount) {

    companion object {
        private val logger = LoggerManager.getLogger(FullyConnectedLayerImpl::class)
        private const val DERIVED_WEIGHTS_KEY = "derivedWeights"
        private const val WEIGHTS_KEY = "weights"
        private const val SUM_KEY = "sum"
    }

    //coroutine concurrent, use for all data in layer
    var derivedWeights = AtomicMap<String, Array<Array<Double>>>()//Array(this.neuronCount) { 0.0 }

    //coroutine concurrent, use for input data
    var inputNeuronLoss = ConcurrentHashMap<Long, Array<Array<Double>>>()//Array(this.neuronCount) { 0.0 }

    //use for layer, public
    var weights: Array<Array<Double>> = emptyArray()

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double, training: Boolean): Array<Double> {
        //initialize the weights in current layer
        if (this.weights.isEmpty()) {
            this.weights = Array(inputNeuron.size) { Array(this.neuronCount) { 0.0 } }
        }
        val out = inputNeuron.multiply(this.weights)
        println("-----forward-----" + this.inputNeuronMap[dataId]?.toJson())
        out.printToMatrix(neuronCount)
        return out
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double) {
        //out put loss
        val nextLayerLoss = when (val nextLayer = this.nextLayer ?: error("next layer is null, need FullyConnectedLayer or OutputLayer")) {
            is OutputLayer -> {
                val outputLayerImpl = nextLayer as OutputLayerImpl
                arrayOf(outputLayerImpl.loss[dataId]!!)
            }
            is FullyConnectedLayer -> {
                val fullyConnectedLayerImpl = nextLayer as FullyConnectedLayerImpl
                fullyConnectedLayerImpl.inputNeuronLoss[dataId]!!//next layer input neuron loss = this layer output neuron loss
            }
            else -> {
                error("not support $nextLayer yet, only support FullyConnectedLayer and OutputLayer")
            }
        }
        //update current layer input neuron loss
        val inputNeuronLoss = this.weights.multiply(nextLayerLoss)//only one loss, after calculate, transform to inputNeuronCount*1 matrix
        inputNeuronLoss.printToMatrix()
        this.inputNeuronLoss.getOrPut(dataId) { inputNeuronLoss }

        println("-----back-----" + this.inputNeuronMap[dataId]?.toJson() + "," + this.inputNeuronLoss[dataId]?.toJson())
        println("input size:${inputNeuron.size}, out put size:${this.neuronCount}, next layer loss:${nextLayerLoss.toJson()}")

        //derived, weight gradient descent, sum all weight grad for every x, use for average weight grad
        this.derivedWeights.operate(DERIVED_WEIGHTS_KEY, create = {
            Array(inputNeuron.size) { xIndex ->
                val x = inputNeuron[xIndex]
                Array(this.neuronCount) { outputNeuronIndex ->
                    println("x:$x, derived:" + ordinaryLeastSquaresDerived(x, nextLayerLoss[outputNeuronIndex][0]))
                    ordinaryLeastSquaresDerived(x, nextLayerLoss[outputNeuronIndex][0])
                }
            }
        }, update = { oldDerivedWeights ->
            Array(inputNeuron.size) { xIndex ->
                val x = inputNeuron[xIndex]
                Array(this.neuronCount) { outputNeuronIndex ->
                    oldDerivedWeights[xIndex][outputNeuronIndex] + ordinaryLeastSquaresDerived(x, nextLayerLoss[outputNeuronIndex][0])
                }
            }
        })
        println("${inputNeuron.size},${this.neuronCount},${this.weights.toJson()},${this.derivedWeights[DERIVED_WEIGHTS_KEY]?.toJson()}")
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
        this.derivedWeights = AtomicMap()//reset after update per one time
    }

    override fun initializeLayerModelDataImpl(data: String) {
    }

    override fun saveLayerModelDataImpl(): String {
        return Constants.String.BLANK
    }
}