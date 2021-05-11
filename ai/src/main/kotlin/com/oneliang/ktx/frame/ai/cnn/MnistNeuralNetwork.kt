package com.oneliang.ktx.frame.ai.cnn

import com.oneliang.ktx.frame.ai.cnn.layer.impl.*
import com.oneliang.ktx.frame.ai.dnn.NeuralNetwork
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.frame.ai.dnn.layer.impl.FullyConnectedLayerImpl
import com.oneliang.ktx.util.logging.LoggerManager

object MnistNeuralNetwork : NeuralNetwork {
    private val logger = LoggerManager.getLogger((MnistNeuralNetwork::class))

    @Suppress("UNCHECKED_CAST")
    override fun getLayerList(): List<Layer<*, *>> {
        //input 2 hidden 3 and 2 output 1, input in batch
        val inputLayer = FullyConnectedLayerImpl(10, learningRate = 0.80)
//        val hiddenLayer1 = FullyConnectedLayerImpl(10)
//        val hiddenLayer2 = FullyConnectedLayerImpl(10)
        val softmaxLayer = SoftmaxLayerImpl(inputLayer.neuronCount, 10, learningRate = 0.06)
        val outputLayer = OutputLayerImpl(softmaxLayer.typeCount)
        return listOf(
            inputLayer,
//            hiddenLayer1,
//            hiddenLayer2,
            softmaxLayer,
            outputLayer
        )
    }
}