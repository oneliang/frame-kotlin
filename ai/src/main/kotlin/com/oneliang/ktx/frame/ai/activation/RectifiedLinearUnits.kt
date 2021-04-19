package com.oneliang.ktx.frame.ai.activation

fun rectifiedLinearUnits(value: Double): Double {
    return if (value < 0.0) 0.0 else value
}

fun rectifiedLinearUnitsDerived(value: Double): Double {
    return if (value < 0.0) 0.0 else 1.0
}

