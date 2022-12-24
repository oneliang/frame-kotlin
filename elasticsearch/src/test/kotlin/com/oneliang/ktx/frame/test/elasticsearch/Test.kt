package com.oneliang.ktx.frame.test.elasticsearch

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.file.readContentEachLine
import java.io.File

private fun readJaegerTracerFile(fullFilename: String) {
    val dataList = mutableListOf<String>()
    var stringBuilder = StringBuilder()
    var index = 0
    var lineIndex = 0
    fullFilename.toFile().readContentEachLine {
        if (it.isBlank()) {
            stringBuilder = StringBuilder()
        } else {
            lineIndex++
            if (lineIndex == 2) {
                stringBuilder.append(it.trim().dropLast(7))
            } else {
                stringBuilder.append(it.trim())
            }
            stringBuilder.append(Constants.Symbol.COMMA)
            if (lineIndex == 6) {//last line
                stringBuilder.append(1)
                dataList += stringBuilder.toString()
                lineIndex = 0
            }
        }
        index++
        true
    }
    println("costs,api,spans,type_span,time,time_0,count")
    dataList.forEach {
        println(it)
    }
}

fun main() {
    val projectPath = File(Constants.String.BLANK).absolutePath
//    val estateFullFilename = "$projectPath/elasticsearch/src/test/resources/estate.txt"
//    readJaegerTracerFile(estateFullFilename)
//    val financingFullFilename = "$projectPath/elasticsearch/src/test/resources/financing.txt"
//    readJaegerTracerFile(financingFullFilename)
//    val salesKaFullFilename = "$projectPath/elasticsearch/src/test/resources/sales-ka.txt"
//    readJaegerTracerFile(salesKaFullFilename)
    val financingPcSalesFullFilename = "$projectPath/elasticsearch/src/test/resources/financing-pc-sales.txt"
    readJaegerTracerFile(financingPcSalesFullFilename)
}