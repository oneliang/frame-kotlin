package com.oneliang.ktx.frame.ai.loss

import kotlin.math.pow

fun ordinaryLeastSquares(calculateY: Float, realY: Float): Float {
    return ordinaryLeastSquares(calculateY - realY)
}

fun ordinaryLeastSquares(calculateY: Double, realY: Double): Double {
    return ordinaryLeastSquares(calculateY - realY)
}

fun ordinaryLeastSquares(diffY: Float): Float {
    return (diffY).pow(2)
}

fun ordinaryLeastSquares(diffY: Double): Double {
    return (diffY).pow(2)
}

fun ordinaryLeastSquaresDerived(x: Float, calculateY: Float, realY: Float): Float = ordinaryLeastSquaresDerived(x, calculateY - realY)

fun ordinaryLeastSquaresDerived(x: Double, calculateY: Double, realY: Double): Double = ordinaryLeastSquaresDerived(x, calculateY - realY)

fun ordinaryLeastSquaresDerived(x: Float, diffY: Float): Float {
    return diffY * x
}

fun ordinaryLeastSquaresDerived(x: Double, diffY: Double): Double {
    return diffY * x
}
