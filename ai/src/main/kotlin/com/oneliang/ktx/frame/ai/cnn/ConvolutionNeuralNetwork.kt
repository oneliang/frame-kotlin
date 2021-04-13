package com.oneliang.ktx.frame.ai.cnn

import com.oneliang.ktx.frame.ai.cnn.layer.impl.*
import com.oneliang.ktx.frame.ai.dnn.NeuralNetwork
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.util.logging.LoggerManager

object ConvolutionNeuralNetwork : NeuralNetwork {
    private val logger = LoggerManager.getLogger((ConvolutionNeuralNetwork::class))
    private const val WEIGHTS_KEY = "weights"
    private const val SUM_KEY = "sum"

    @Suppress("UNCHECKED_CAST")
    override fun getLayerList(): List<Layer<*, *>> {
        val inputLayer = InputLayerImpl(1, 28, 28)
        val convolutionLayer1 = ConvolutionLayerImpl(inputLayer.mapDepth, 32, inputLayer.x, inputLayer.y, 5, 5)
        val rectifiedLinearUnitsLayer1 = RectifiedLinearUnitsLayerImpl(convolutionLayer1.mapDepth, convolutionLayer1.outX, convolutionLayer1.outY)
        val averagePoolingLayer1 = AveragePoolingLayerImpl(rectifiedLinearUnitsLayer1.mapDepth, rectifiedLinearUnitsLayer1.inX, rectifiedLinearUnitsLayer1.inY, 2)
        val convolutionLayer2 = ConvolutionLayerImpl(inputLayer.mapDepth, 64, averagePoolingLayer1.outX, averagePoolingLayer1.outY, 5, 5)
        val rectifiedLinearUnitsLayer2 = RectifiedLinearUnitsLayerImpl(convolutionLayer2.mapDepth, convolutionLayer2.outX, convolutionLayer2.outY)
        val averagePoolingLayer2 = AveragePoolingLayerImpl(rectifiedLinearUnitsLayer2.mapDepth, rectifiedLinearUnitsLayer2.outX, rectifiedLinearUnitsLayer2.outY, 2)
        val fullyConnectedLayer1 = FullyConnectedLayerImpl(1024)
        val localResponseNormalizationLayer = LocalResponseNormalizationLayerImpl(fullyConnectedLayer1.mapDepth, fullyConnectedLayer1.outX, fullyConnectedLayer1.outY)
        val dropoutLayer = DropoutLayerImpl(localResponseNormalizationLayer.mapDepth, localResponseNormalizationLayer.outX, localResponseNormalizationLayer.outY)
        val fullyConnectedLayer2 = FullyConnectedLayerImpl(10)//10*1*1
        val softmaxLayer = SoftmaxLayerImpl()
        val outputLayer = OutputLayerImpl(10, 1, 1)
        return listOf(
            inputLayer,
            convolutionLayer1,
            rectifiedLinearUnitsLayer1,
            averagePoolingLayer1,
            convolutionLayer2,
            rectifiedLinearUnitsLayer2,
            averagePoolingLayer2,
            fullyConnectedLayer1,
            localResponseNormalizationLayer,
            dropoutLayer,
            fullyConnectedLayer2,
            softmaxLayer,
            outputLayer
        )
    }
}