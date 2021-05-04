package com.oneliang.ktx.frame.ai.cnn

import com.oneliang.ktx.frame.ai.dnn.Trainer
import com.oneliang.ktx.util.logging.*
import java.io.File

object TestMnist {
    fun testMnistNeuralNetwork() {
        val learningRate = 0.060
        val times = 5000
//    val batching = TestTrendBatching(100)
        val batchSize = 100
        val mnistLabelFullFilename = "D:/Dandelion/data/mnist/t10k-labels-idx1-ubyte"
        val mnistImageFullFilename = "D:/Dandelion/data/mnist/t10k-images-idx3-ubyte"
        val modelFullFilename = "/D:/cnn_model.txt"
        val batching = TestMnistBatching(mnistLabelFullFilename, mnistImageFullFilename, batchSize)
        val neuralNetwork = MnistNeuralNetwork
        val trainer = Trainer()
        trainer.train(batching, neuralNetwork, learningRate, times, 10, modelFullFilename)
        batching.reset()
        trainer.test(batching, neuralNetwork, modelFullFilename)
    }
}

fun main() {
    val loggerList = mutableListOf<AbstractLogger>()
    loggerList.add(BaseLogger(Logger.Level.VERBOSE))
    loggerList.add(FileLogger(Logger.Level.VERBOSE, File("D:/"), "default.log"))
    val logger = ComplexLogger(Logger.Level.DEBUG, loggerList)
    LoggerManager.registerLogger("*", logger)
    TestMnist.testMnistNeuralNetwork()
}