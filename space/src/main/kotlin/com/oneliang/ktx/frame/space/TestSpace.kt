package com.oneliang.ktx.frame.space

fun main() {
    val a = Point(1.0, 1.0)
    val b = Point(2.0, 2.0)
    val c = a - b
    println(c.distance())
    println(a.maybeSamePoint(b, 1.0))

    val shape1 = Shape(arrayOf(Point(1.0, 1.0), Point(1.0, 2.0), Point(2.0, 2.0), Point(2.0, 1.0)))
    val shape2 = shape1.copy().apply { move(2.0) }
    val point = Point(1.5, 1.5)
    println(shape1)
    println(shape2)
    println(point.maybeInSegment(a, b))
    println(shape1.maybeInner(point))

    val baseShape = Shape(arrayOf(Point(1.0, 1.0), Point(1.0, 2.0), Point(2.0, 2.0), Point(2.0, 1.0)))
    var shapeCount = 0
    val step = 0.5
    val width = 100
    val height = 100
    val shapeList = mutableListOf<Shape>()
    shapeList += baseShape
    shapeCount++

    val testShape = baseShape.copy()
    for (shape in shapeList) {
        while (true) {
            val result = shape.maybeCoincide(testShape)
            if (result) {
                //first move x
                testShape.move(step)
                if (testShape.maxX() >= width) {

                }
                if (testShape.maxY() >= height) {

                }
            } else {
                shapeCount++
                break
            }
        }
    }
    println(testShape)
}