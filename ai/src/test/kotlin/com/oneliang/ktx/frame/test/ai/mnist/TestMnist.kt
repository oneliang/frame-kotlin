package com.oneliang.ktx.frame.test.ai.mnist

import com.oneliang.ktx.frame.ai.cnn.MnistNeuralNetwork
import com.oneliang.ktx.frame.ai.dnn.Trainer
import com.oneliang.ktx.util.logging.*
import java.io.File

object TestMnist {
    fun testMnistNeuralNetwork() {
        val learningRate = 0.060f
        val times = 5000
//    val batching = TestTrendBatching(100)
        val batchSize = 100
        val fileRoot = "D:/Dandelion/data"
//        val fileRoot = "/Users/oneliang/data"
        val mnistLabelFullFilename = "$fileRoot/mnist/t10k-labels-idx1-ubyte"
        val mnistImageFullFilename = "$fileRoot/mnist/t10k-images-idx3-ubyte"
        val modelFullFilename = "$fileRoot/cnn_model.txt"
        val batching = TestMnistBatching(mnistLabelFullFilename, mnistImageFullFilename, batchSize)
        val neuralNetwork = MnistNeuralNetwork
        val trainer = Trainer()
        trainer.train(batching, neuralNetwork, learningRate, times, 10, modelFullFilename, false)
        batching.reset()
        val testMnistLabelFullFilename = "$fileRoot/mnist/train-labels-idx1-ubyte"
        val testMnistImageFullFilename = "$fileRoot/mnist/train-images-idx3-ubyte"
        val testBatching = TestMnistBatching(testMnistLabelFullFilename, testMnistImageFullFilename, batchSize)
        trainer.test(batching, neuralNetwork, modelFullFilename)
    }
}

fun main() {
    val fileRoot = "D:/Dandelion/data"
//        val fileRoot = "/Users/oneliang/data"
    val loggerList = mutableListOf<AbstractLogger>()
    loggerList.add(BaseLogger(Logger.Level.VERBOSE))
    loggerList.add(FileLogger(Logger.Level.VERBOSE, File(fileRoot), "default.log"))
    val logger = ComplexLogger(Logger.Level.DEBUG, loggerList)
    LoggerManager.registerLogger("*", logger)
    TestMnist.testMnistNeuralNetwork()
}