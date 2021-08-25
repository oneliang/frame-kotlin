package com.oneliang.ktx.frame.space

class Shape(private val points: Array<Point>) {

    fun move(x: Double, y: Double = 0.0, z: Double = 0.0) {
        for (point in this.points) {
            point.replaceFrom(point.x + x, point.y + y, point.z + z)
        }
    }

    fun copy(): Shape {
        val newPoints = Array(this.points.size) { index ->
            this.points[index].copy()
        }
        return Shape(newPoints)
    }

    override fun toString(): String {
        return this.points.joinToString { "(${it.x}, ${it.y}, ${it.z})" }
    }

    fun maxX(): Double {
        return this.points.maxOf { it.x }
    }

    fun maxY(): Double {
        return this.points.maxOf { it.y }
    }

    fun maybeCoincide(shape: Shape): Boolean {
        for (point in shape.points) {
            if (maybeInner(point)) {
                return true
            }
        }
        return false
    }

    fun maybeInner(point: Point): Boolean {
        if (this.points.size == 1) {
            return point.maybeSamePoint(this.points[0])
        } else if (this.points.size == 2) {
            return point.maybeInSegment(this.points[0], this.points[1])
        } else {
            var leftCrossCount = 0
            var rightCrossCount = 0
            for (index in this.points.indices) {
                val start = index
                var end = index + 1
                if (start == this.points.size - 1) {
                    end = 0
                }
                val p1 = this.points[start]//current point
                if (p1.x == point.x && p1.y == point.y) {//same point
                    println("the point is vertex")
                    break//will return true at last
                }

                val p2 = this.points[end]//next point
                // y = kx+b => k=(y2-y1)/(x2-x1) => b = y1-kx1
                var x = point.x
                var y = point.y
                if ((p2.y - p1.y) == 0.0) {//same y, horizontal line
                    if (point.y == p1.y) {//same y line, check x range
                        if (p2.x >= p1.x) {//p2>p1, left is p1.x, right is p2.x
                            if (p2.x >= x && x >= p1.x) {//x in segment=> point in segment
                                break//will return true at last
                            } else if (x <= p1.x) {//x is on left
                                //no need to count
//                                continue
                            } else {//x is on right
                                //no need to count
//                                continue
                            }
                        } else {//p1>p2, left is p2.x, right is p1.x
                            if (p1.x >= x && x >= p2.x) {//x in segment=> point in segment
                                break//will return true at last
                            } else if (x <= p2.x) {//x is on left
                                //no need to count
//                                continue
                            } else {//x is on right
                                //no need to count
//                                continue
                            }
                        }
                    } else {//no same line, uncross
                        //no need to count
                        continue
                    }
                } else if ((p2.x - p1.x) == 0.0) {//same x, vertical line
                    if (p2.y >= p1.y) {//p2>p1
                        if (p2.y >= point.y && point.y >= p1.y) {//y is in line, check x position
                            if (point.x < p1.x) {//point is on left of line
                                rightCrossCount++
                            } else {//x is on right of line
                                leftCrossCount++
                            }
                            x = p1.x
                        } else {//y is not in line
                            //no need to count
//                            continue
                        }
                    } else {//p1>p2
                        if (p1.y >= point.y && point.y >= p2.y) {//y is in line, check x position
                            if (point.x < p1.x) {//point is on left of line
                                rightCrossCount++
                            } else {//x is on right of line
                                leftCrossCount++
                            }
                            x = p1.x
                        } else {//y not in line
                            //no need to count
//                            continue
                        }
                    }
                } else {
                    val k = (p2.y - p1.y) / (p2.x - p1.x)
                    val b = p1.y - k * p1.x
                    //cross point, set y = point.y
                    y = point.y
                    x = (y - b) / k
                    println("index:$index, line:(k:$k,b:$b), cross point:($x,$y)")
                    if (x == point.x) {//self is cross point
                        break//will return true at last
                    } else if (x < point.x) {
                        leftCrossCount++
                    } else {
                        rightCrossCount++
                    }
                }
                println("index:$index, cross point:($x,$y)")
            }
            println("left cross count:$leftCrossCount, right cross count:$rightCrossCount")
            //odd number is inner, even number is outer not include zero, zero is inner
            if (leftCrossCount == 0 && rightCrossCount == 0) {//in segment, or the point is vertex
                return true
            }
            return !(leftCrossCount % 2 == 0 && rightCrossCount % 2 == 0)
        }
    }
}