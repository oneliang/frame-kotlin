package com.oneliang.ktx.frame.ai.activation

fun rectifiedLinearUnits(value: Float): Float {
    return if (value < 0.0f) 0.0f else value
}

fun rectifiedLinearUnits(value: Double): Double {
    return if (value < 0.0) 0.0 else value
}

fun rectifiedLinearUnitsDerived(value: Float): Float {
    return if (value < 0.0f) 0.0f else 1.0f
}

fun rectifiedLinearUnitsDerived(value: Double): Double {
    return if (value < 0.0) 0.0 else 1.0
}

