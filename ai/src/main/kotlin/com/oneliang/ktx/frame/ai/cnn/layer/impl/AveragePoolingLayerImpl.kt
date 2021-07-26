package com.oneliang.ktx.frame.ai.cnn.layer.impl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.cnn.layer.AveragePoolingLayer
import com.oneliang.ktx.util.common.doubleIteration
import com.oneliang.ktx.util.common.singleIteration
import com.oneliang.ktx.util.math.matrix.dotMultiply
import com.oneliang.ktx.util.math.matrix.scaleToBig
import com.oneliang.ktx.util.math.matrix.scaleToSmall

open class AveragePoolingLayerImpl(
    inX: Int,
    inY: Int,
    scale: Int
) : AveragePoolingLayer<Array<Array<Array<Float>>>, Array<Array<Array<Float>>>, Array<Array<Array<Float>>>>(inX, inY, scale) {

    override fun forwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Float>>>, y: Float, training: Boolean): Array<Array<Array<Float>>> {
        val outputNeuron = Array(inputNeuron.size) { Array(this.outY) { Array(this.outX) { 0.0f } } }
        singleIteration(inputNeuron.size) { mapIndex ->
            outputNeuron[mapIndex] = inputNeuron[mapIndex].scaleToSmall(this.scale)
        }
        return outputNeuron
    }

    override fun backwardImpl(dataId: Long, inputNeuron: Array<Array<Array<Float>>>, y: Float) {
        //sampleLayer.mapNumber=convolutionLayer.mapNumber
        val outputLoss = getNextLayerInputNeuronLoss(dataId)

        val inputLoss = Array(inputNeuron.size) { Array(this.inX) { Array(this.inY) { 0.0f } } }
        singleIteration(inputNeuron.size) { previousMapIndex ->
            val mapOutputLoss = outputLoss[previousMapIndex]
            val outputProbability = calculateProbability(inputNeuron[previousMapIndex])
            inputLoss[previousMapIndex] = outputProbability.dotMultiply(kronecker(mapOutputLoss, this.scale))
        }
        this.inputNeuronLoss[dataId] = inputLoss
    }

    private fun calculateProbability(values: Array<Array<Float>>): Array<Array<Float>> {
        val results = Array(values.size) { Array(values[0].size) { 0.0f } }
        doubleIteration(results.size, results[0].size) { row, column ->
            results[row][column] = values[row][column] * (1 - values[row][column])
        }
        return results
    }

    private fun kronecker(maps: Array<Array<Float>>, scale: Int): Array<Array<Float>> {
        return maps.scaleToBig(scale)
    }

    override fun forwardResetImpl(dataId: Long) {
    }

    override fun updateImpl(epoch: Int, printPeriod: Int, totalDataSize: Long, learningRate: Float) {
    }

    override fun initializeLayerModelDataImpl(data: String) {
    }

    override fun saveLayerModelDataImpl(): String {
        return Constants.String.BLANK
    }
}