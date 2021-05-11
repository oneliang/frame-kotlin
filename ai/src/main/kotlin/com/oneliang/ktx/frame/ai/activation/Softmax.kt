package com.oneliang.ktx.frame.ai.activation

import com.oneliang.ktx.pojo.DoubleWrapper
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.math.matrix.multiply
import kotlin.math.exp

private const val KEY_TOTAL_VALUE = "key_total_value"
fun softmax(xArray: Array<Double>, weightArray: Array<Array<Double>>, negative: Boolean = false): Array<Double> {
    val totalValueAtomicMap = AtomicMap<String, DoubleWrapper>()
    val valueMatrix = xArray.multiply(weightArray, true, transform = {
        val value = exp(it)
        totalValueAtomicMap.operate(KEY_TOTAL_VALUE, create = { DoubleWrapper(value) },
            update = { old ->
                DoubleWrapper(old.value + value)
//            totalValue += value
            })
        value
    })
    //update value matrix to probability
    for (index in valueMatrix.indices) {
        valueMatrix[index] = valueMatrix[index] / totalValueAtomicMap[KEY_TOTAL_VALUE]!!.value * (if (negative) -1.0 else 1.0)
    }
    return valueMatrix
}