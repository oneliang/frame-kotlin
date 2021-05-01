package com.oneliang.ktx.frame.ai.cnn

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.base.Batching
import com.oneliang.ktx.util.common.toArray

class TestMnistBatching(private val labelFullFilename: String, private val imageFullFilename: String, override val batchSize: Int) : Batching<Pair<Double, Array<Double>>>(batchSize) {

    private var reader = MnistReader(labelFullFilename, imageFullFilename)
    private val dataSize = this.reader.size()
    private var lineCount = 0
    override fun reset() {
        this.lineCount = 0
        this.reader.reset()
    }

    override fun fetch(): Result<Pair<Double, Array<Double>>> {
        var currentLineCount = 0
        val dataList = mutableListOf<Pair<Double, Array<Double>>>()
        while (this.lineCount < this.dataSize) {
            dataList += Pair(this.reader.readNextLabel().toDouble(), this.reader.readNextImage())
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