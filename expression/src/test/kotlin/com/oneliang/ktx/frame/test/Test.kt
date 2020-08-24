package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.test.util.Point

fun main() {
    val function = "A*B"
    val pointA = Point("A", 2.0)
    val pointB = Point("B", 3.0)
    val value = Parser.eval(function, pointA, pointB)
    println(value.value)
}