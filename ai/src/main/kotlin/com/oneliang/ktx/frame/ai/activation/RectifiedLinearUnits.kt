package com.oneliang.ktx.frame.ai.activation

/**
 * relu
 */
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

/**
 * leaky relu
 */
fun leakyRectifiedLinearUnits(value: Float, negativeValueSlope: Float = 0.1f): Float {
    return if (value < 0.0f) negativeValueSlope * value else value
}

fun leakyRectifiedLinearUnits(value: Double, negativeValueSlope: Double = 0.1): Double {
    return if (value < 0.0) negativeValueSlope * value else value
}

fun leakyRectifiedLinearUnitsDerived(value: Float, negativeValueSlope: Float = 0.1f): Float {
    return if (value < 0.0f) negativeValueSlope else 1.0f
}

fun leakyRectifiedLinearUnitsDerived(value: Double, negativeValueSlope: Double = 0.1): Double {
    return if (value < 0.0) negativeValueSlope else 1.0
}
