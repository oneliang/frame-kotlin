package com.oneliang.ktx.frame.test.ai

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ai.base.Batching
import java.io.File

class TestSoftmaxRegressionBatching(override val batchSize: Int) : Batching(batchSize) {

    private var reader = File("/C:/Users/Administrator/Desktop/temp/softmax_regression.csv").bufferedReader()

    private var lineCount = 0
    override fun reset() {
        this.lineCount = 0
        this.reader = File("/C:/Users/Administrator/Desktop/temp/softmax_regression.csv").bufferedReader()
    }

    private fun parseLine(line: String): Pair<Double, Array<Double>> {
        val rowDataList = line.split(Constants.Symbol.COMMA)
        return rowDataList[0].toDouble() to arrayOf(rowDataList[1].toDouble(), rowDataList[2].toDouble(), 1.0)
    }

    override fun fetch(): Result {
        var currentLineCount = 0
        var line = reader.readLine() ?: null
        val dataList = mutableListOf<Pair<Double, Array<Double>>>()
        while (line != null) {//break when finished
            if (line.isNotBlank()) {
                dataList += parseLine(line)
                currentLineCount++
                lineCount++
                if (currentLineCount == batchSize) {
                    break
                }
            }
            line = reader.readLine() ?: null
        }
        return if (dataList.isEmpty()) {
            Result(true)
        } else {
            Result(false, dataList)
        }
    }
}