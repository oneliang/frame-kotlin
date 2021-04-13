package com.oneliang.ktx.frame.ai.dnn

import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.frame.ai.dnn.layer.impl.FullyConnectedLayerImpl
import com.oneliang.ktx.frame.ai.dnn.layer.impl.OutputLayerImpl
import com.oneliang.ktx.util.logging.LoggerManager

object DeepNeuralNetwork : NeuralNetwork {
    private val logger = LoggerManager.getLogger((DeepNeuralNetwork::class))

    override fun getLayerList(): List<Layer<*, *>> {
        //input 2 hidden 3 and 2 output 1, input in batch
        val inputLayer = FullyConnectedLayerImpl(3)
        val hiddenLayer1 = FullyConnectedLayerImpl(2)
        val hiddenLayer2 = FullyConnectedLayerImpl(1)
        val outputLayer = OutputLayerImpl(1)
        return listOf(
            inputLayer,
            hiddenLayer1,
            hiddenLayer2,
            outputLayer
        )
    }
}