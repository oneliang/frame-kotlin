package com.oneliang.ktx.frame.space

fun main() {
    val a = Point(2.0, 2.0)
    val b = Point(1.0, 1.0)
    val c = a - b
    println(c.distance())
}