package com.oneliang.ktx.frame.ai.cnn

import com.oneliang.ktx.frame.ai.dnn.Trainer
import com.oneliang.ktx.util.logging.LoggerManager

object TestCNN {
    private val logger = LoggerManager.getLogger(TestCNN::class)
    fun testConvolutionNeuralNetwork() {
        val learningRate = 0.0001
        val times = 1
//    val batching = TestTrendBatching(100)
        val batchSize = 1
        val mnistLabelFullFilename = "D:/Dandelion/data/mnist/t10k-labels-idx1-ubyte"
        val mnistImageFullFilename = "D:/Dandelion/data/mnist/t10k-images-idx3-ubyte"
//        val modelFullFilename = "/D:/cnn_model.txt"
        val batching = TestConvolutionNeuralNetworkBatching(mnistLabelFullFilename, mnistImageFullFilename, batchSize)
        val neuralNetwork = ConvolutionNeuralNetwork
        val trainer = Trainer()
        trainer.train(batching, neuralNetwork, learningRate, times, 1)//, modelFullFilename)
        batching.reset()
    }
}

fun main() {
    TestCNN.testConvolutionNeuralNetwork()
}