package com.oneliang.ktx.frame.space

fun main() {
    val a = Point(1.0, 1.0)
    val b = Point(2.0, 2.0)
    val c = a - b
    println(c.distance())
    println(a.maybeSamePoint(b, 1.0))

    val shape1 = Shape(arrayOf(Point(1.0, 1.0), Point(1.0, 2.0), Point(2.0, 2.0), Point(2.0, 1.0)))
    val shape2 = shape1.copy().apply { move(y = 2.0) }
    val point = Point(1.5, 1.0)
    println(shape1)
//    println(shape2)
//    println(point.maybeInSegment(a, b))
    println(shape1.maybeInner(point))
//    println(shape2.maybeCoincide(shape1))
//    return
    val list = listOf(
        Shape(arrayOf(Point(1.0, 1.0), Point(1.0, 2.0), Point(2.0, 2.0), Point(2.0, 1.0))),
        Shape(arrayOf(Point(2.5, 1.0), Point(2.5, 2.0), Point(3.5, 2.0), Point(3.5, 1.0))),
        Shape(arrayOf(Point(4.0, 1.0), Point(4.0, 2.0), Point(5.0, 2.0), Point(5.0, 1.0))),
        Shape(arrayOf(Point(5.5, 1.0), Point(5.5, 2.0), Point(6.5, 2.0), Point(6.5, 1.0))),
        Shape(arrayOf(Point(7.0, 1.0), Point(7.5, 2.0), Point(8.0, 2.0), Point(8.0, 1.0))),
        Shape(arrayOf(Point(8.5, 1.0), Point(8.5, 2.0), Point(9.5, 2.0), Point(9.5, 1.0)))
    )
    val testShape = Shape(arrayOf(Point(0.0, 1.5), Point(0.0, 2.5), Point(1.0, 2.5), Point(1.0, 1.5)))
    for (i in list) {
//        println("test shape:" + testShape.maybeCoincide(i))
    }

//    return
    val baseShape = Shape(arrayOf(Point(1.0, 1.0), Point(1.0, 2.0), Point(2.0, 2.0), Point(2.0, 1.0)))
    val step = 0.5
    val width = 10
    val height = 4
    val shapeList = mutableListOf<Shape>()
    shapeList += baseShape

    var nextShape: Shape? = null
    while (true) {
        if (nextShape == null) {
            nextShape = baseShape.copy()
        }
        var allShapeSuccess = true
        for (shape in shapeList) {
            val result = nextShape.maybeCoincide(shape)
            if (result) {
                allShapeSuccess = false
                break
            }
        }

        if (allShapeSuccess) {
            println("success shape:$nextShape")
            shapeList += nextShape
            nextShape = null
            continue//will generate next shape
        } else {//move next shape
            if (nextShape.maxX() > width) {
                nextShape.moveXToZero()
                nextShape.move(y = step)
                if (nextShape.maxY() > height) {
                    break//break it
                }
            } else if (nextShape.maxX() <= width) {
                nextShape.move(step)
                continue
            }
        }
    }
    println("shape list size:%s".format(shapeList.size))
    for ((index, shape) in shapeList.withIndex()) {
        println("index:%s, shape:%s".format(index, shape))
    }
}