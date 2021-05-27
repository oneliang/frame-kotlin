package com.oneliang.ktx.frame.ai.cnn

import com.oneliang.ktx.frame.ai.cnn.layer.impl.*
import com.oneliang.ktx.frame.ai.dnn.NeuralNetwork
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.util.logging.LoggerManager

object ConvolutionNeuralNetwork : NeuralNetwork {
    private val logger = LoggerManager.getLogger((ConvolutionNeuralNetwork::class))

    @Suppress("UNCHECKED_CAST")
    override fun getLayerList(): List<Layer<*, *>> {
        val inputLayer = InputLayerImpl(1, 28, 28)
        val convolutionLayer1 = ConvolutionLayerImpl(inputLayer.mapDepth, 32, inputLayer.x, inputLayer.y, 5, 5)
        val rectifiedLinearUnitsLayer1 = RectifiedLinearUnitsLayerImpl()
        val averagePoolingLayer1 = AveragePoolingLayerImpl(convolutionLayer1.outX, convolutionLayer1.outY, 2)
        val convolutionLayer2 = ConvolutionLayerImpl(convolutionLayer1.mapDepth, 64, averagePoolingLayer1.outX, averagePoolingLayer1.outY, 5, 5)
        val rectifiedLinearUnitsLayer2 = RectifiedLinearUnitsLayerImpl()
        val averagePoolingLayer2 = AveragePoolingLayerImpl(convolutionLayer2.outX, convolutionLayer2.outY, 2)
//        val flattenLayer = FlattenLayerImpl(128)
        val convolutionLayer3 = ConvolutionLayerImpl(convolutionLayer2.mapDepth, 128, averagePoolingLayer2.outX, averagePoolingLayer2.outY, averagePoolingLayer2.outX, averagePoolingLayer2.outX)
//        val localResponseNormalizationLayer = LocalResponseNormalizationLayerImpl(fullyConnectedLayer1.mapDepth)
//        val dropoutLayer = DropoutLayerImpl(localResponseNormalizationLayer.mapDepth)
        val transformLayer = TransformLayerImpl()//128*1*1
        val fullyConnectedLayer2 = FullyConnectedLayerImpl(10)//10*1*1
        val softmaxLayer = SoftmaxLayerImpl(fullyConnectedLayer2.neuronCount, 10)
        val outputLayer = OutputLayerImpl(softmaxLayer.typeCount)
        return listOf(
            inputLayer,
            convolutionLayer1,
            rectifiedLinearUnitsLayer1,
            averagePoolingLayer1,
            convolutionLayer2,
            rectifiedLinearUnitsLayer2,
            averagePoolingLayer2,
            convolutionLayer3,
//            flattenLayer,
//            localResponseNormalizationLayer,
//            dropoutLayer,
            transformLayer,
            fullyConnectedLayer2,
            softmaxLayer,
            outputLayer
        )
    }
}