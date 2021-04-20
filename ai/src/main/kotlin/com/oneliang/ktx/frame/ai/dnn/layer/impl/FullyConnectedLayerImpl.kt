package com.oneliang.ktx.frame.ai.dnn.layer.impl

import com.oneliang.ktx.frame.ai.dnn.layer.FullyConnectedLayer
import com.oneliang.ktx.frame.ai.dnn.layer.OutputLayer
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.json.jsonToMap
import com.oneliang.ktx.util.json.jsonToObjectList
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.math.matrix.multiply

class FullyConnectedLayerImpl(
    neuronCount: Int,
    private val supportBias: Boolean = false,
) : FullyConnectedLayer<Array<Double>, Array<Double>, Array<Array<Double>>>(neuronCount) {

    companion object {
        private val logger = LoggerManager.getLogger(FullyConnectedLayerImpl::class)
        private const val DERIVED_WEIGHTS_KEY = "derivedWeights"
        private const val WEIGHTS_KEY = "weights"
    }

    //coroutine concurrent, use for all data in layer
    var derivedWeights = AtomicMap<String, Array<Array<Double>>>()//Array(this.neuronCount) { 0.0 }

    //coroutine concurrent, use for input data, need to reset it by data id to release memory
//    var inputNeuronLoss = ConcurrentHashMap<Long, Array<Array<Double>>>()//Array(this.neuronCount) { 0.0 }

    //use for layer, public
    var weights: Array<Array<Double>> = emptyArray()
    private var bias: Double = 1.0

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double, training: Boolean): Array<Double> {
        //add bias when support bias
        val newInputNeuron = if (this.supportBias) inputNeuron + this.bias else inputNeuron
        //initialize the weights in current layer
        if (this.weights.isEmpty()) {
            this.weights = Array(newInputNeuron.size) { Array(this.neuronCount) { 0.1 } }
        }
        val out = newInputNeuron.multiply(this.weights)
//        println("-----forward-----" + this.inputNeuronMap[dataId]?.toJson() + ", weights:" + this.weights.toJson() + ", out:" + out.toJson())
//        out.printToMatrix(neuronCount)
        return out
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Double>, y: Double) {
        //add bias when support bias
        val newInputNeuron = if (this.supportBias) inputNeuron + this.bias else inputNeuron
        //out put loss
        val nextLayerLoss = when (val nextLayer = this.nextLayer ?: error("next layer is null, need FullyConnectedLayer or OutputLayer")) {
            is OutputLayer<*, *, *> -> {
//                println("-----output-----")
                val outputLayerImpl = nextLayer as OutputLayerImpl
                arrayOf(outputLayerImpl.inputNeuronLoss[dataId]!!)
            }
            is FullyConnectedLayer<*, *, *> -> {
//                println("-----fully connected-----")
                val fullyConnectedLayerImpl = nextLayer as FullyConnectedLayerImpl
                val outputNeuronLoss = fullyConnectedLayerImpl.inputNeuronLoss[dataId]!!//next layer input neuron loss = this layer output neuron loss
                if (this.supportBias) outputNeuronLoss.copyOfRange(0, outputNeuronLoss.size - 1) else outputNeuronLoss//when support bias, delete the bias loss, last input is bias
            }
            else -> {
                error("not support $nextLayer yet, only support FullyConnectedLayer and OutputLayer")
            }
        }
//        println(this.weights.toJson() + ", next layer loss:" + nextLayerLoss.toJson())
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
//        println("${inputNeuron.size},${this.neuronCount},${this.weights.toJson()},${this.derivedWeights[DERIVED_WEIGHTS_KEY]?.toJson()}")
    }

    override fun forwardResetImpl(dataId: Long) {
        this.inputNeuronLoss.remove(dataId)
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