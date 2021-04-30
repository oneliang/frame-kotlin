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
        val rnnBatching = TestConvolutionNeuralNetworkBatching(mnistLabelFullFilename, mnistImageFullFilename, batchSize)
        val neuralNetwork = ConvolutionNeuralNetwork
        val trainer = Trainer()
        trainer.train(rnnBatching, neuralNetwork, learningRate, times, 1)//, modelFullFilename)
        rnnBatching.reset()
    }
}

fun main() {
    TestCNN.testConvolutionNeuralNetwork()
}