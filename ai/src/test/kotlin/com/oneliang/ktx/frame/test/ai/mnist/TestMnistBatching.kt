package com.oneliang.ktx.frame.test.ai.mnist

import com.oneliang.ktx.frame.ai.base.Batching
import com.oneliang.ktx.frame.ai.cnn.MnistReader

class TestMnistBatching(private val labelFullFilename: String, private val imageFullFilename: String, override val batchSize: Int) : Batching<Pair<Float, Array<Float>>>(batchSize) {

    private var reader = MnistReader(labelFullFilename, imageFullFilename)
    private val dataSize = this.reader.size()
    private var lineCount = 0
    override fun reset() {
        this.lineCount = 0
        this.reader.reset()
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