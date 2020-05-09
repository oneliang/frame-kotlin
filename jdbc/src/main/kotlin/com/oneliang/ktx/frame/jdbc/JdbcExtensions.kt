package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants

private fun <T> sqlConditionTransform(instance: T): String {
    return Constants.Symbol.SINGLE_QUOTE + instance.toString() + Constants.Symbol.SINGLE_QUOTE
}

fun <T> Iterable<T>.toSqlCondition(transform: (T) -> String = { it.toString() }): String {
    return this.joinToString {
        sqlConditionTransform(transform(it))
    }
}

fun <T> Array<T>.toSqlCondition(transform: (T) -> String = { it.toString() }): String {
    return this.joinToString {
        sqlConditionTransform(transform(it))
    }
}