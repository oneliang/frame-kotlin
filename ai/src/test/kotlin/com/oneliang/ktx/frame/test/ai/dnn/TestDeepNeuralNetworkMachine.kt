package com.oneliang.ktx.frame.test.ai.dnn

import com.oneliang.ktx.frame.ai.dnn.Trainer
import com.oneliang.ktx.frame.ai.dnn.LinearRegressionNeuralNetwork

fun main() {
    val learningRate = 0.0001
    val times = 100
//    val batching = TestTrendBatching(100)
    val batchSize = 50
    val trainFullFilename = "/C:/Users/Administrator/Desktop/temp/dnn.txt"
    val testFullFilename = "/C:/Users/Administrator/Desktop/temp/dnn.txt"
    val modelFullFilename = "/D:/mnist_model.txt"
    val rnnBatching = TestDNNBatching(trainFullFilename, batchSize)
    val neuralNetwork = LinearRegressionNeuralNetwork()
    Trainer.train(rnnBatching, neuralNetwork, learningRate, times, 100, modelFullFilename)
    rnnBatching.reset()
    val cnnTestBatching = TestDNNBatching(testFullFilename, batchSize)
    Trainer.test(cnnTestBatching, neuralNetwork, modelFullFilename)
}