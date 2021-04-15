package com.oneliang.ktx.frame.test.ai.dnn

import com.oneliang.ktx.frame.ai.dnn.DeepNeuralNetwork
import com.oneliang.ktx.frame.ai.dnn.LinearRegressionNeuralNetwork
import com.oneliang.ktx.frame.ai.dnn.SoftmaxRegressionNeuralNetwork
import com.oneliang.ktx.frame.ai.dnn.Trainer
import com.oneliang.ktx.frame.ai.loss.ordinaryLeastSquaresDerived
import com.oneliang.ktx.frame.coroutine.Coroutine
import com.oneliang.ktx.frame.test.ai.TestSoftmaxRegressionBatching
import com.oneliang.ktx.pojo.DoubleWrapper
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import kotlinx.coroutines.Job
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

object TestDeepNeuralNetworkMachine {
    private val logger = LoggerManager.getLogger(TestDeepNeuralNetworkMachine::class)
    fun testLinearRegressionNeuralNetwork() {
        val learningRate = 0.0001
        val times = 1000
//    val batching = TestTrendBatching(100)
        val batchSize = 5
        val trainFullFilename = "/C:/Users/Administrator/Desktop/temp/dnn.txt"
        val testFullFilename = "/C:/Users/Administrator/Desktop/temp/dnn.txt"
        val modelFullFilename = "/D:/dnn_model.txt"
        val rnnBatching = TestDNNBatching(trainFullFilename, batchSize)
        val neuralNetwork = LinearRegressionNeuralNetwork
        val trainer = Trainer()
        trainer.train(rnnBatching, neuralNetwork, learningRate, times, 100, modelFullFilename)
        rnnBatching.reset()
        val cnnTestBatching = TestDNNBatching(testFullFilename, batchSize)
        trainer.test(cnnTestBatching, neuralNetwork, modelFullFilename)
    }

    fun testSoftmaxRegressionNeuralNetwork() {
        val learningRate = 0.04
        val times = 1
        val batchSize = 50
        val batching = TestSoftmaxRegressionBatching(batchSize)
        val modelFullFilename = "/D:/softmax_model.txt"
        val neuralNetwork = SoftmaxRegressionNeuralNetwork
        val trainer = Trainer()
        val begin = System.currentTimeMillis()
        trainer.train(batching, neuralNetwork, learningRate, times, 1, modelFullFilename, false)
        logger.info("train cost:%s", System.currentTimeMillis() - begin)
        batching.reset()
        trainer.test(batching, neuralNetwork, modelFullFilename)
    }

    fun testDeepNeuralNetwork() {
        val learningRate = 0.0001
        val times = 3000
        val batchSize = 5
        val trainFullFilename = File("ai/src/test/resources/dnn.txt").absolutePath
        val testFullFilename = File("ai/src/test/resources/dnn.txt").absolutePath
        val modelFullFilename = "/D:/dnn_model.txt"
        val rnnBatching = TestDNNBatching(trainFullFilename, batchSize)
        val neuralNetwork = DeepNeuralNetwork
        val trainer = Trainer()
        trainer.train(rnnBatching, neuralNetwork, learningRate, times, 100, modelFullFilename)
        rnnBatching.reset()
        val cnnTestBatching = TestDNNBatching(testFullFilename, batchSize)
        trainer.test(cnnTestBatching, neuralNetwork, modelFullFilename)
    }
}

fun main() {
//    TestDeepNeuralNetworkMachine.testLinearRegressionNeuralNetwork()
//    TestDeepNeuralNetworkMachine.testSoftmaxRegressionNeuralNetwork()
    TestDeepNeuralNetworkMachine.testDeepNeuralNetwork()
}