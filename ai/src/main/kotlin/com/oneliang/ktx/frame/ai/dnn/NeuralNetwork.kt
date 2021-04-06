package com.oneliang.ktx.frame.ai.dnn

import com.oneliang.ktx.frame.ai.dnn.layer.Layer

interface NeuralNetwork {

    fun getLayerList(): List<Layer<*, *>>
}