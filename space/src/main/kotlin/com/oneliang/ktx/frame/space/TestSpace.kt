package com.oneliang.ktx.frame.space

fun main() {
    val a = Point(2.0, 2.0)
    val b = Point(2.0, 2.1)
    val c = a - b
    println(c.distance())
    println(a.maybeSamePoint(b, 1.0))
}