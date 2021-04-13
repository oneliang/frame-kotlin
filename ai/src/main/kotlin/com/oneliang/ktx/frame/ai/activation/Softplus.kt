package com.oneliang.ktx.frame.ai.activation

import kotlin.math.exp
import kotlin.math.ln

fun softplus(value: Double): Double {
    return ln(1 + exp(value))
}