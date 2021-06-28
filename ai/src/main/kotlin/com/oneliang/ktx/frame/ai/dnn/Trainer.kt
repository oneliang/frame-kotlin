package com.oneliang.ktx.frame.ai.dnn

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.base.Batching
import com.oneliang.ktx.frame.ai.dnn.layer.Layer
import com.oneliang.ktx.frame.coroutine.Coroutine
import com.oneliang.ktx.util.common.toBriefString
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.common.toMap
import com.oneliang.ktx.util.file.fileExists
import com.oneliang.ktx.util.file.readContentIgnoreLine
import com.oneliang.ktx.util.file.write
import com.oneliang.ktx.util.json.jsonToObject
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import kotlinx.coroutines.Job

class Trainer {
    private val logger = LoggerManager.getLogger(Trainer::class)
    private val coroutine = Coroutine()

    fun train(
        batching: Batching<Pair<Float, Array<Float>>>,
        neuralNetwork: NeuralNetwork,
        learningRate: Float,
        epochs: Int,
        printPeriod: Int = 500,
        modelFullFilename: String = Constants.String.BLANK,
        parallel: Boolean = false
    ) {
        val maxRetryCount = 0
        var retryCount = 0
        var needToTrain = true
        while (needToTrain && retryCount <= maxRetryCount) {
            needToTrain = try {
                innerTrain(batching, neuralNetwork, learningRate, epochs, printPeriod, modelFullFilename, parallel)
                false
            } catch (e: Throwable) {
                retryCount++
                logger.error("has occur an exception, need to retry, retry count:%s", e, retryCount)
                true//need to retry
            }
        }
    }

    private fun innerTrain(
        batching: Batching<Pair<Float, Array<Float>>>,
        neuralNetwork: NeuralNetwork,
        learningRate: Float,
        epochs: Int,
        printPeriod: Int,
        modelFullFilename: String,
        parallel: Boolean
    ) {
        val training = true
        val layerList = neuralNetwork.getLayerList()
        val (inputLayer, outputLayer, model) = getInputAndOutputLayer(layerList, modelFullFilename)
        var begin = System.currentTimeMillis()
        for (epoch in 1..epochs) {
            var totalDataSize = 0L
            try {
                var dataId = 0L
                val jobList = mutableListOf<Job>()
                this.coroutine.runBlocking {
                    while (true) {
                        val result = batching.fetch()
                        if (result.finished) {
                            batching.reset()
                            break
                        }
                        val inputDataList = result.dataList
                        totalDataSize += inputDataList.size
                        inputDataList.forEach {
                            val currentDataId = ++dataId
                            val (y, xArray) = it
                            if (parallel) {
                                jobList += this.coroutine.launch {
                                    //forward include backward(backPropagation)
                                    forward(inputLayer, currentDataId, xArray, y, training)
                                    backward(outputLayer, currentDataId, y)
                                    forwardReset(inputLayer, currentDataId)
                                }
                            } else {
                                //forward include backward(backPropagation)
                                forward(inputLayer, currentDataId, xArray, y, training)
                                backward(outputLayer, currentDataId, y)
                                forwardReset(inputLayer, currentDataId)
                            }

                        }
                    }
                    jobList.forEach { it.join() }
                }

                //check loss
                val outputLoss = checkLoss(outputLayer, epoch, printPeriod, totalDataSize, learningRate)

                //update all weight, gradient descent
                update(layerList, epoch, printPeriod, totalDataSize, learningRate)

                //after update, can update other data for you need
                afterUpdate(layerList, epoch, outputLoss, printPeriod, totalDataSize, learningRate)

                if (epoch % printPeriod == 0) {
                    //first calculate cost
                    val cost = System.currentTimeMillis() - begin
                    //update and replace begin
                    begin = System.currentTimeMillis()
                    saveModel(layerList, modelFullFilename, (model?.times ?: 0) + epoch)
                    logger.debug("times:%s, cost:%s, total data size:%s", epoch, cost, totalDataSize)
                }
            } catch (e: Throwable) {
                //on error
                onError(layerList, epoch, printPeriod, totalDataSize, learningRate)
                throw e
            }
        }
    }


    @Suppress("UNCHECKED_CAST")
    private fun getInputAndOutputLayer(layerList: List<Layer<*, *>>, modelFullFilename: String = Constants.String.BLANK): Triple<Layer<Any, Any>, Layer<Any, Any>, Model?> {
        var model: Model? = null
        if (modelFullFilename.isNotBlank() && modelFullFilename.fileExists()) {
            val modelJson = modelFullFilename.toFile().readContentIgnoreLine()
            model = modelJson.jsonToObject(Model::class)
        }
        val layerModelMap = model?.layerModels?.toMap { it.index to it } ?: emptyMap()
        var inputLayer: Layer<Any, Any>? = null
        var outputLayer: Layer<Any, Any>? = null
        var inputLayerCount = 0
        for (layerIndex in 0 until layerList.size - 1) {//no need to iterate last layer
            val layer = layerList[layerIndex] as Layer<Any, Any>
            val nextLayer = layerList[layerIndex + 1] as Layer<Any, Any>
            layer.nextLayer = nextLayer
            if (layer.previousLayer == null) {
                inputLayerCount++
                inputLayer = layer
            }
            if (nextLayer.nextLayer == null) {//last layer, output layer
                outputLayer = nextLayer
            }
            logger.info("this:%s, previousLayer:%s, nextLayer:%s", layer, layer.previousLayer, layer.nextLayer)
            val layerLabel = layerIndex + 1
            val layerModel = layerModelMap[layerIndex]
            if (layerModel == null) {
                logger.info("no model for layer:%s initialize", layerLabel)
                continue
            }
            logger.info("initialize layer:%s, data:%s", layerLabel, layerModel.data.toBriefString(100))
            layer.initializeLayerModelData(layerModel.data)
        }
        if (inputLayer == null) {
            error("input layer is null, previous layer is null can be a input layer")
        }
        if (inputLayerCount > 1) {
            error("input layer is more than one, now size:%s, only support one input layer".format(inputLayerCount))
        }
        return Triple(inputLayer, outputLayer!!, model)
    }

    @Suppress("UNCHECKED_CAST")
    private fun forward(inputLayer: Layer<Any, Any>, dataId: Long, xArray: Array<Float>, y: Float, training: Boolean) {
        inputLayer.doForward(dataId, xArray, y, training)
    }

    @Suppress("UNCHECKED_CAST")
    private fun backward(outputLayer: Layer<Any, Any>, dataId: Long, y: Float) {
        outputLayer.doBackward(dataId, y)
    }

    @Suppress("UNCHECKED_CAST")
    private fun forwardReset(inputLayer: Layer<Any, Any>, dataId: Long) {
        inputLayer.doForwardRest(dataId)
    }

    private fun checkLoss(outputLayer: Layer<*, *>, epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float): Float {
        val (checkResult, outputLoss) = outputLayer.checkLoss(epoch, printPeriod, totalDataSize, learningRate)
        if (!checkResult) {
            error("epoch:%s, loss error, learning rate:%s".format(epoch, learningRate))
        }
        return outputLoss
    }

    private fun update(layerList: List<Layer<*, *>>, epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) {
        for (layerIndex in layerList.indices) {
            val layer = layerList[layerIndex]
            layer.update(epoch, printPeriod, totalDataSize, learningRate)
        }
    }

    private fun afterUpdate(layerList: List<Layer<*, *>>, epoch: Int, outputLoss: Float, printPeriod: Int, totalDataSize: Long, learningRate: Float) {
        for (layerIndex in layerList.indices) {
            val layer = layerList[layerIndex]
            layer.afterUpdate(epoch, outputLoss, printPeriod, totalDataSize, learningRate)
        }
    }

    private fun onError(layerList: List<Layer<*, *>>, epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) {
        for (layerIndex in layerList.indices) {
            val layer = layerList[layerIndex]
            layer.onError(epoch, printPeriod, totalDataSize, learningRate)
        }
    }

    private fun saveModel(layerList: List<Layer<*, *>>, fullFilename: String, epochs: Int) {
        if (fullFilename.isBlank()) {
            return
        }
        val list = mutableListOf<Model.LayerModel>()
        for (layerIndex in layerList.indices) {
            val layer = layerList[layerIndex]
            list += Model.LayerModel().apply {
                this.index = layerIndex
                this.data = layer.getLayerModelData()
            }
        }
        val model = Model(epochs, list.toTypedArray())
        fullFilename.toFile().write(model.toJson().toByteArray())
    }

    fun test(
        batching: Batching<Pair<Float, Array<Float>>>,
        neuralNetwork: NeuralNetwork,
        modelFullFilename: String = Constants.String.BLANK,
    ) {
        val layerList = neuralNetwork.getLayerList()
        val (inputLayer, outputLayer, _) = getInputAndOutputLayer(layerList, modelFullFilename)
        var dataId = 0L
        var totalDataSize = 0L
        while (true) {
            val result = batching.fetch()
            if (result.finished) {
                logger.warning("Data is empty. Batch may be finished")
                break
            }
            val inputDataList = result.dataList
            totalDataSize += inputDataList.size
            inputDataList.forEach { item ->
                dataId++
                val (y, xArray) = item
                forward(inputLayer, dataId, xArray, y, false)
            }
        }
        testProcess(layerList, totalDataSize)
    }

    private fun testProcess(layerList: List<Layer<*, *>>, totalDataSize: Long) {
        for (layerIndex in layerList.indices) {
            val layer = layerList[layerIndex]
            layer.testProcess(totalDataSize)
        }
    }
}