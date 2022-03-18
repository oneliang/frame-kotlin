package com.oneliang.ktx.frame.ai.activation

fun minMaxNormalization(doubles: Array<Double>): Array<Double> {
    var min = 0.0
    var max = 0.0
    for (x in doubles) {
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
    val resultArray = Array(doubles.size) { 0.0 }
    doubles.forEachIndexed { index, value ->
        resultArray[index] = (value - min) / gap
    }
    return resultArray
}