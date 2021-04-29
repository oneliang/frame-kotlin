package com.oneliang.ktx.frame.ai.activation

fun minMaxNormalization(xArray: Array<Double>): Array<Double> {
    var min = 0.0
    var max = 0.0
    for (x in xArray) {
        if (min == 0.0) {
            min = x
        }
        if (max == 0.0) {
            max = x
        }
        if (min > x) {
            min = x
        }
        if (max < x) {
            max = x
        }
    }
    val gap = max - min
    if (gap == 0.0) {
        error("min equal max, all value are same. max:$max, min:$min")
    }
    val resultArray = Array(xArray.size) { 0.0 }
    xArray.forEachIndexed { index, value ->
        resultArray[index] = (value - min) / gap
    }
    return resultArray
}