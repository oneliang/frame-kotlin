package com.oneliang.ktx.frame.ai.cnn

import com.oneliang.ktx.frame.ai.dnn.Trainer
import com.oneliang.ktx.util.logging.*
import java.io.File

object TestMnist {
    fun testMnistNeuralNetwork() {
        val learningRate = 0.060
        val times = 1000
//    val batching = TestTrendBatching(100)
        val batchSize = 100
        val mnistLabelFullFilename = "/Users/oneliang/data/mnist/t10k-labels-idx1-ubyte"
        val mnistImageFullFilename = "/Users/oneliang/data/mnist/t10k-images-idx3-ubyte"
        val modelFullFilename = "/Users/oneliang/data/cnn_model.txt"
        val batching = TestMnistBatching(mnistLabelFullFilename, mnistImageFullFilename, batchSize)
        val neuralNetwork = MnistNeuralNetwork
        val trainer = Trainer()
//        trainer.train(batching, neuralNetwork, learningRate, times, 10, modelFullFilename, true)
//        batching.reset()
        val testMnistLabelFullFilename = "/Users/oneliang/data/mnist/train-labels-idx1-ubyte"
        val testMnistImageFullFilename = "/Users/oneliang/data/mnist/train-images-idx3-ubyte"
        val testBatching = TestMnistBatching(testMnistLabelFullFilename, testMnistImageFullFilename, batchSize)
        trainer.test(testBatching, neuralNetwork, modelFullFilename)
    }
}

fun main() {
    val loggerList = mutableListOf<AbstractLogger>()
    loggerList.add(BaseLogger(Logger.Level.VERBOSE))
    loggerList.add(FileLogger(Logger.Level.VERBOSE, File("/Users/oneliang/data"), "default.log"))
    val logger = ComplexLogger(Logger.Level.DEBUG, loggerList)
    LoggerManager.registerLogger("*", logger)
    TestMnist.testMnistNeuralNetwork()
}