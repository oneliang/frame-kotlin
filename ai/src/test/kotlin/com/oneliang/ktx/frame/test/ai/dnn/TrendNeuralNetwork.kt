package com.oneliang.ktx.frame.test.ai.dnn

import com.oneliang.ktx.frame.ai.dnn.NeuralNetwork
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.frame.ai.dnn.layer.impl.FullyConnectedLayerImpl
import com.oneliang.ktx.frame.ai.dnn.layer.impl.OutputLayerImpl
import com.oneliang.ktx.util.logging.LoggerManager

object TrendNeuralNetwork : NeuralNetwork {
    private val logger = LoggerManager.getLogger((TrendNeuralNetwork::class))

    override fun getLayerList(): List<Layer<*, *>> {
        //input 2 hidden 3 and 2 output 1, input in batch
        val inputLayer = FullyConnectedLayerImpl(30, true)
        val hiddenLayer1 = FullyConnectedLayerImpl(20, true)
        val hiddenLayer2 = FullyConnectedLayerImpl(10, true)
        val hiddenLayer3 = FullyConnectedLayerImpl(1, true)
        val outputLayer = OutputLayerImpl()
        return listOf(
            inputLayer,
            hiddenLayer1,
            hiddenLayer2,
            hiddenLayer3,
            outputLayer
        )
    }
}