package com.oneliang.ktx.frame.ai.cnn

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.base.Batching
import com.oneliang.ktx.util.common.toArray

class TestConvolutionNeuralNetworkBatching(private val labelFullFilename: String, private val imageFullFilename: String, override val batchSize: Int) : Batching<Pair<Float, Array<Float>>>(batchSize) {

    private var reader = MnistReader(labelFullFilename, imageFullFilename)
    private val dataSize = 1//this.reader.size()
    private var lineCount = 0
    override fun reset() {
        this.lineCount = 0
        this.reader.reset()
    }

    private fun parseLine(line: String): Pair<Float, Array<Float>> {
        val rowDataList = line.split(Constants.Symbol.COMMA)
        val lastIndex = rowDataList.size - 1
        return rowDataList[lastIndex].toFloat() to rowDataList.subList(0, lastIndex).toArray { it.toFloat() }
    }

    override fun fetch(): Result<Pair<Float, Array<Float>>> {
        var currentLineCount = 0
        val dataList = mutableListOf<Pair<Float, Array<Float>>>()
        while (this.lineCount < this.dataSize) {
            dataList += Pair(this.reader.readNextLabel().toFloat(), this.reader.readNextImage())
            currentLineCount++
            this.lineCount++
            if (currentLineCount == this.batchSize) {
                break
            }
        }
        return if (dataList.isEmpty()) {
            Result(true)
        } else {
            Result(false, dataList)
        }
    }
}