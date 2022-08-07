package com.oneliang.ktx.frame.adjuster

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.file.readContentEachLine

private fun test() {
    var totalWeight = 0.0
    "C:/Users/Administrator/Desktop/2021_06_21_default_problem.log".toFile().readContentEachLine { line ->
        if (line.indexOf("transform processor,") > 0
            && line.indexOf("COMPANY_ID=15737025182471024006") > 0
            && (line.indexOf("TYPE=现货") > 0
                    || line.indexOf("TYPE=期货") > 0
                    || line.indexOf("TYPE=加工") > 0)
            && (line.indexOf("STATUS=已退款") > 0
                    || line.indexOf("STATUS=已支付") > 0)
        ) {
            val weightString = "STEEL_P_ORDER_WEIGHT="
            val weightStartIndex = line.indexOf(weightString) + weightString.length
            var weightEndIndex = line.indexOf(Constants.Symbol.COMMA, weightStartIndex)
            if (weightEndIndex < 0) {
                weightEndIndex = line.indexOf(Constants.Symbol.BIG_BRACKET_RIGHT, weightStartIndex)
            }
            try {
                val weight = line.substring(weightStartIndex, weightEndIndex).toDouble()
                println("$weight,$line")
                totalWeight += weight
            } catch (e: Throwable) {
                println("$weightStartIndex,$weightEndIndex, $line")
            }
        }
        true
    }
    println(totalWeight)
}

fun main() {
    test()
    return
    val map = mutableMapOf<Int, String>()
    "C:/Users/Administrator/Desktop/a.csv".toFile().readContentEachLine { line ->
        val headerList = line.trim().split(Constants.Symbol.COMMA)
        for (i in headerList.indices) {
            map[i] = map[i].nullToBlank() + headerList[i] + Constants.Symbol.COMMA
        }
        true
    }
    map.forEach { (key, value) ->
        println(value)
    }
}