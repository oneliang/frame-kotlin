package com.oneliang.ktx.frame.ai.base

import kotlin.math.pow

object OlsLossFunction {
    fun loss(calculateY: Double, realY: Double): Double {
        return (calculateY - realY).pow(2)
    }
}