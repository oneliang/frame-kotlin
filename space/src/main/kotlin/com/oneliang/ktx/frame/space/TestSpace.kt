package com.oneliang.ktx.frame.space

fun main() {
    val a = Point(1.0, 1.0)
    val b = Point(2.0, 2.0)
    val c = a - b
    println(c.distance())
    println(a.maybeSamePoint(b, 1.0))

    val shape1 = Shape(arrayOf(Point(0.0, 0.0), Point(0.0, 1.0), Point(1.0, 1.0), Point(1.0, 0.0)))
    println(shape1.calculateSuitableRotation(Point(1.5, 0.5)))
    val shape1Copy = shape1.copy().apply { rotateClockwiseRoundTheCenterPoint(68.0f) }
    MainFrame().show(listOf(shape1, shape1Copy))
    return
//    println("before rotate:$shape1")
    shape1.rotateClockwiseRoundTheCenterPoint(360.0f)
//    println("after rotate:$shape1")

    val shape2 = Shape(arrayOf(Point(0.0, 0.0), Point(0.0, 1.0), Point(1.0, 0.0)))//shape1.copy().apply { move(y = 2.0) }
    val shape3 = Shape(arrayOf(Point(0.5, 0.5), Point(0.5, 1.5), Point(1.5, 0.5)))//shape1.copy().apply { move(y = 2.0) }
    val point = Point(0.0, 1.0)
//    println(shape1)
//    println(shape2)
//    println(point.maybeInSegment(a, b))
//    println(shape3.maybeInner(point))
//    println(shape3.maybeCoincide(shape2))
//    return
    val list = listOf(
        Shape(arrayOf(Point(0.0, 0.0), Point(0.0, 1.0), Point(1.0, 0.0)))
    )
    val testShape = Shape(arrayOf(Point(1.0, 1.0), Point(1.0, 2.0), Point(2.0, 1.0)))
    for (i in list) {
//        println("test shape:" + testShape.maybeCoincide(i))
    }

//    return
//    val baseShape = Shape(arrayOf(Point(1.0, 1.0), Point(1.0, 2.0), Point(2.0, 2.0), Point(2.0, 1.0)))
    val baseShape = Shape(arrayOf(Point(1.0, 1.0), Point(1.0, 2.0), Point(2.0, 1.0)))
    val step = 0.1
    val width = 2.5
    val height = 2.5
    val shapeList = mutableListOf<Shape>()
//    shapeList += baseShape

    var nextShape: Shape? = null
    while (true) {
        if (nextShape == null) {
            nextShape = baseShape.copy()
            nextShape.moveXToZero()
            nextShape.moveYToZero()
        }
//        if (shapeList.isEmpty()) {//reset the first shape to edge
//        nextShape.moveXToZero()
//        nextShape.moveYToZero()
//        }
        var allShapeSuccess = true
        for (shape in shapeList) {
            val nextShapePointCheck = nextShape.maybeCoincide(shape)
            val shapePointCheck = shape.maybeCoincide(nextShape)
            if (nextShapePointCheck || shapePointCheck) {
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
//            println("before move:" + nextShape)
            if ((nextShape.maxX() + step) > width) {
                nextShape.moveXToZero()
                nextShape.move(y = step)
//                println("1 after move:" + nextShape)
                if (nextShape.maxY() > height) {
                    break//break it
                }
            } else {
                nextShape.move(step)
//                println("2 after move:" + nextShape)
                continue
            }
        }
    }
    println("shape list size:%s".format(shapeList.size))
    for ((index, shape) in shapeList.withIndex()) {
        println("index:%s, shape:%s".format(index, shape))
    }
    MainFrame().show(shapeList)
}