package com.oneliang.ktx.frame.ai.dnn.layer.impl

import com.oneliang.ktx.frame.ai.base.matrix.multiply
import com.oneliang.ktx.frame.ai.dnn.layer.FullyConnectedLayer
import com.oneliang.ktx.frame.ai.dnn.layer.LossLayer
import com.oneliang.ktx.frame.ai.dnn.layer.OutputLayer
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.util.common.toBriefString
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.json.jsonToMap
import com.oneliang.ktx.util.json.jsonToObjectList
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager

class FullyConnectedLayerImpl(
    neuronCount: Int,
    private val supportBias: Boolean = false,
    private val learningRate: Float = 0.0f,
) : FullyConnectedLayer<Array<Float>, Array<Float>, Array<Array<Float>>>(neuronCount) {

    companion object {
        private val logger = LoggerManager.getLogger(FullyConnectedLayerImpl::class)
        private const val DERIVED_WEIGHTS_KEY = "derivedWeights"
        private const val WEIGHTS_KEY = "weights"
    }

    //coroutine concurrent, use for all data in layer
    var derivedWeights = AtomicMap<String, Array<Array<Float>>>()//Array(this.neuronCount) { 0.0f }

    //coroutine concurrent, use for input data, need to reset it by data id to release memory
//    var inputNeuronLoss = ConcurrentHashMap<Long, Array<Array<Float>>>()//Array(this.neuronCount) { 0.0 }

    //use for layer, public
    var weights: Array<Array<Float>> = emptyArray()
    private var bias: Float = 1.0f

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Float>, y: Float, training: Boolean): Array<Float> {
        //add bias when support bias
        val newInputNeuron = if (this.supportBias) inputNeuron + this.bias else inputNeuron
        //initialize the weights in current layer
        if (this.weights.isEmpty()) {
            this.weights = Array(newInputNeuron.size) { Array(this.neuronCount) { 0.1f } }
        }
        val outputNeuron = newInputNeuron.multiply(this.weights, parallel = false, gpu = false)
//        println("-----fully connected forward-----")
//        println("input:" + inputNeuron.toJson())
//        println("weights:" + this.weights.toJson())
//        println("output:" + outputNeuron.toJson())
        return outputNeuron
    }

    @Suppress("UNCHECKED_CAST")
    override fun backwardImpl(dataId: Long, inputNeuron: Array<Float>, y: Float) {
        //add bias when support bias
        val newInputNeuron = if (this.supportBias) inputNeuron + this.bias else inputNeuron
        //out put loss
        val nextLayerLoss = when (val nextLayer = this.nextLayer ?: error("next layer is null, need FullyConnectedLayer or OutputLayer")) {
            is OutputLayer<*, *, *> -> {
//                println("-----output-----")
                val outputLayerImpl = nextLayer as OutputLayerImpl
                outputLayerImpl.inputNeuronLoss[dataId]!!
            }
            is FullyConnectedLayer<*, *, *> -> {
//                println("-----fully connected-----")
                val fullyConnectedLayerImpl = nextLayer as FullyConnectedLayerImpl
                val outputNeuronLoss = fullyConnectedLayerImpl.inputNeuronLoss[dataId]!!//next layer input neuron loss = this layer output neuron loss
                if (this.supportBias) outputNeuronLoss.copyOfRange(0, outputNeuronLoss.size - 1) else outputNeuronLoss//when support bias, delete the bias loss, last input is bias
            }
            is LossLayer<*, *, *> -> {
                val outputNeuronLoss = getNextLayerInputNeuronLoss<Array<Array<Float>>>(dataId)//next layer input neuron loss = this layer output neuron loss
                if (this.supportBias) outputNeuronLoss.copyOfRange(0, outputNeuronLoss.size - 1) else outputNeuronLoss//when support bias, delete the bias loss, last input is bias
            }
            else -> {
                error("not support $nextLayer yet, only support FullyConnectedLayer and OutputLayer")
            }
        }
//        println("-----fully connected backward-----")
//        println("next layer loss:" + nextLayerLoss.toJson())
        //update current layer input neuron loss
        val inputNeuronLoss = this.weights.multiply(nextLayerLoss, parallel = false, gpu = false)//only one loss, after calculate, transform to inputNeuronCount*1 matrix
//        inputNeuronLoss.printToMatrix()
//        println("input loss:" + inputNeuronLoss.toJson())
        this.inputNeuronLoss[dataId] = inputNeuronLoss

//        println("-----back-----" + this.inputNeuronMap[dataId]?.toJson() + "," + this.inputNeuronLoss[dataId]?.toJson())
//        println("input size:${inputNeuron.size}, out put size:${this.neuronCount}, next layer loss:${nextLayerLoss.toJson()}")

        //derived, weight gradient descent, sum all weight grad for every x, use for average weight grad
        this.derivedWeights.operate(DERIVED_WEIGHTS_KEY, create = {
            Array(newInputNeuron.size) { xIndex ->
                val x = newInputNeuron[xIndex]
                Array(this.neuronCount) { outputNeuronIndex ->
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
//        println("derived weight:${this.derivedWeights[DERIVED_WEIGHTS_KEY]?.toJson()}")
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) {
        val fixLearningRate = if (this.learningRate > 0.0) this.learningRate else learningRate
//update all weight, gradient descent
        val derivedWeights = this.derivedWeights[DERIVED_WEIGHTS_KEY] ?: emptyArray()
        this.weights.forEachIndexed { weightIndex, outputNeuronWeightArray ->
            outputNeuronWeightArray.forEachIndexed { outputNeuronIndex, weight ->
                this.weights[weightIndex][outputNeuronIndex] = weight - (fixLearningRate * derivedWeights[weightIndex][outputNeuronIndex]) / totalDataSize
            }
        }
        if (epoch % printPeriod == 0) {
            logger.debug("epoch:%s, weight array:%s", epoch, this.weights.toJson().toBriefString(100))
        }
//        println("-----fully connected-----")
//        println("update weights:" + this.weights.toJson())
        //reset after update
        this.derivedWeights.clear()//reset after update per one time
    }

    override fun initializeLayerModelDataImpl(data: String) {
        val map = data.jsonToMap()
        val weightsData = map[WEIGHTS_KEY]?.jsonToObjectList(Array<Float>::class)
        if (weightsData != null) {
            this.weights = weightsData.toTypedArray()
        }
    }

    override fun saveLayerModelDataImpl(): String {
        val map = mutableMapOf<String, Array<Array<Float>>>()
        map[WEIGHTS_KEY] = this.weights
        return map.toJson()
    }
}