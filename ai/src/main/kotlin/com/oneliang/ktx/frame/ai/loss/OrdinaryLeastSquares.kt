package com.oneliang.ktx.frame.ai.loss

import kotlin.math.pow

fun ordinaryLeastSquares(calculateY: Double, realY: Double): Double {
    return (calculateY - realY).pow(2)
}

fun ordinaryLeastSquaresDerived(x: Double, calculateY: Double, y: Double): Double {
    return (calculateY - y) * x
}