package com.oneliang.ktx.frame.expression

fun String.eval(): Any = Expression.eval(this)

fun String.evalBoolean(): Boolean = Expression.evalBoolean(this)

fun String.evalNumber(): Double = Expression.evalNumber(this)

fun String.evalString(): String = Expression.evalString(this)