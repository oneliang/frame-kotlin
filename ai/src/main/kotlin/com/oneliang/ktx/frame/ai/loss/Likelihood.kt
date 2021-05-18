package com.oneliang.ktx.frame.ai.loss

import kotlin.math.ln

//when value close to 1, probability is true, loss close to zero, ln(0<x<1) is negative
fun likelihood(value: Float): Float {
    return if (value > 0) {
        -ln(value)
    } else {
        0.0f
    }
}

//when value close to 1, probability is true, loss close to zero, ln(0<x<1) is negative
fun likelihood(value: Double): Double {
    return if (value > 0) {
        -ln(value)
    } else {
        0.0
    }
}