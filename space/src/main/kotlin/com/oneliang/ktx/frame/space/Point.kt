package com.oneliang.ktx.frame.space

import kotlin.math.pow
import kotlin.math.sqrt

data class Point(val x: Double, val y: Double, val z: Double = 0.0) {

    operator fun plus(point: Point): Point {
        return Point(this.x + point.x, this.y + point.y, this.z + point.z)
    }

    operator fun minus(point: Point): Point {
        return Point(this.x - point.x, this.y - point.y, this.z - point.z)
    }

    fun distance(): Double {
        return sqrt(x.pow(2) + y.pow(2) + z.pow(2))
    }

    fun maybeSamePoint(point: Point, threshold: Double): Boolean {
        return (this - point).distance() <= threshold
    }
}