package com.oneliang.ktx.frame.test.space

import com.oneliang.ktx.frame.space.Point
import kotlin.math.abs
import kotlin.math.sqrt

object Test {

    //    companion object {
    const val MAXZERO = 0.001
    const val ERR_TRIL_CONCENTRIC = -1
    const val ERR_TRIL_COLINEAR_2SOLUTIONS = -2
    const val ERR_TRIL_SQRTNEGNUMB = -3
    const val ERR_TRIL_NOINTERSECTION_SPHERE4 = -4
    const val ERR_TRIL_NEEDMORESPHERE = -5
    const val TRIL_3SPHERES = 3
    const val TRIL_4SPHERES = 4
    const val CM_ERR_ADDED = 10 //was 5
//    }

    class Temp {
        var mu1: Double = 0.0
        var mu2: Double = 0.0
    }

    /* Return the difference of two vectors, (vector1 - vector2). */
    fun vdiff(vector1: Point, vector2: Point): Point {
        return vector1 - vector2
    }

    /* Return the sum of two vectors. */
    fun vsum(vector1: Point, vector2: Point): Point {
        return vector1 + vector2
    }

    /* Multiply vector by a number. */
    fun vmul(vector: Point, n: Double): Point {
        val x = vector.x * n
        val y = vector.y * n
        val z = vector.z * n
        return Point(x, y, z)
    }

    /* Divide vector by a number. */
    fun vdiv(vector: Point, n: Double): Point {
        val x = vector.x / n
        val y = vector.y / n
        val z = vector.z / n
        return Point(x, y, z)
    }

    /* Return the Euclidean norm. */
    fun vdist(v1: Point, v2: Point): Double {
        val xd = v1.x - v2.x
        val yd = v1.y - v2.y
        val zd = v1.z - v2.z
        return sqrt(xd * xd + yd * yd + zd * zd)
    }

    /* Return the Euclidean norm. */
    fun vnorm(vector: Point): Double {
        return sqrt(vector.x * vector.x + vector.y * vector.y + vector.z * vector.z)
    }

    /* Return the dot product of two vectors. */
    fun dot(vector1: Point, vector2: Point): Double {
        return vector1.x * vector2.x + vector1.y * vector2.y + vector1.z * vector2.z
    }

    /* Replace vector with its cross product with another vector. */
    fun cross(vector1: Point, vector2: Point): Point {
        val x = vector1.y * vector2.z - vector1.z * vector2.y
        val y = vector1.z * vector2.x - vector1.x * vector2.z
        val z = vector1.x * vector2.y - vector1.y * vector2.x
        return Point(x, y, z)
    }

    /* Return the GDOP (Geometric Dilution of Precision) rate between 0-1.
 * Lower GDOP rate means better precision of intersection.
 */
    fun gdoprate(tag: Point, p1: Point, p2: Point, p3: Point): Double {
        var ex: Point?
        var t1: Point?
        var t2: Point?
        var t3: Point?
        var h = 0.0
        var gdop1 = 0.0
        var gdop2 = 0.0
        var gdop3 = 0.0
        var result = 0.0

        ex = vdiff(p1, tag)
        h = vnorm(ex)
        t1 = vdiv(ex, h)

        ex = vdiff(p2, tag)
        h = vnorm(ex)
        t2 = vdiv(ex, h)

        ex = vdiff(p3, tag)
        h = vnorm(ex)
        t3 = vdiv(ex, h)

        gdop1 = abs(dot(t1, t2))
        gdop2 = abs(dot(t2, t3))
        gdop3 = abs(dot(t3, t1))

        if (gdop1 < gdop2) result = gdop2 else result = gdop1
        if (result < gdop3) result = gdop3

        return result
    }

    /* Intersecting a sphere sc with radius of r, with a line p1-p2.
 * Return zero if successful, negative error otherwise.
 * mu1 & mu2 are constant to find points of intersection.
*/
    fun sphereline(p1: Point, p2: Point, sc: Point, r: Double, temp: Temp): Int {
        var a = 0.0
        var b = 0.0
        var c = 0.0
        var bb4ac = 0.0

        val x = p2.x - p1.x
        val y = p2.y - p1.y
        val z = p2.z - p1.z
        val dp = p2 - p1//Point(x,y,z)

        a = dp.x * dp.x + dp.y * dp.y + dp.z * dp.z

        b = 2 * (dp.x * (p1.x - sc.x) + dp.y * (p1.y - sc.y) + dp.z * (p1.z - sc.z))

        c = sc.x * sc.x + sc.y * sc.y + sc.z * sc.z
        c += p1.x * p1.x + p1.y * p1.y + p1.z * p1.z
        c -= 2 * (sc.x * p1.x + sc.y * p1.y + sc.z * p1.z)
        c -= r * r

        bb4ac = b * b - 4 * a * c

        if (abs(a) == 0.0 || bb4ac < 0) {
            temp.mu1 = 0.0
            temp.mu2 = 0.0
            return -1
        }

        temp.mu1 = (-b + sqrt(bb4ac)) / (2 * a)
        temp.mu2 = (-b - sqrt(bb4ac)) / (2 * a)
        return 0
    }

    /* Return TRIL_3SPHERES if it is performed using 3 spheres and return
 * TRIL_4SPHERES if it is performed using 4 spheres
 * For TRIL_3SPHERES, there are two solutions: result1 and result2
 * For TRIL_4SPHERES, there is only one solution: best_solution
 *
 * Return negative number for other errors
 *
 * To force the function to work with only 3 spheres, provide a duplicate of
 * any sphere at any place among p1, p2, p3 or p4.
 *
 * The last parameter is the largest nonnegative number considered zero
 * it is somewhat analogous to machine epsilon (but inclusive).
*/
    fun trilateration(
        result1: Point,
        result2: Point,
        best_solution: Point,
        p1: Point, r1: Double,
        p2: Point, r2: Double,
        p3: Point, r3: Double,
        p4: Point, r4: Double,
        maxzero: Double
    ): Int {
        var ex: Point? = null
        var ey: Point? = null
        var ez: Point? = null
        var t1: Point? = null
        var t2: Point? = null
        var t3: Point? = null
        var h = 0.0
        var i = 0.0
        var j = 0.0
        var x = 0.0
        var y = 0.0
        var z = 0.0
        var t = 0.0
        val temp = Temp()
        var mu = 0.0
        var result = 0

        /*********** FINDING TWO POINTS FROM THE FIRST THREE SPHERES **********/

        // if there are at least 2 concentric spheres within the first 3 spheres
        // then the calculation may not continue, drop it with error -1

        /* h = |p3 - p1|, ex = (p3 - p1) / |p3 - p1| */
        ex = vdiff(p3, p1) // vector p13
        h = vnorm(ex) // scalar p13
        if (h <= maxzero) {
            /* p1 and p3 are concentric, not good to obtain a precise intersection point */
            //printf("concentric13 return -1\n")
            return ERR_TRIL_CONCENTRIC
        }

        /* h = |p3 - p2|, ex = (p3 - p2) / |p3 - p2| */
        ex = vdiff(p3, p2) // vector p23
        h = vnorm(ex) // scalar p23
        if (h <= maxzero) {
            /* p2 and p3 are concentric, not good to obtain a precise intersection point */
            //printf("concentric23 return -1\n")
            return ERR_TRIL_CONCENTRIC
        }

        /* h = |p2 - p1|, ex = (p2 - p1) / |p2 - p1| */
        ex = vdiff(p2, p1) // vector p12
        h = vnorm(ex) // scalar p12
        if (h <= maxzero) {
            /* p1 and p2 are concentric, not good to obtain a precise intersection point */
            //printf("concentric12 return -1\n")
            return ERR_TRIL_CONCENTRIC
        }
        ex = vdiv(ex, h) // unit vector ex with respect to p1 (new coordinate system)

        /* t1 = p3 - p1, t2 = ex (ex . (p3 - p1)) */
        t1 = vdiff(p3, p1) // vector p13
        i = dot(ex, t1) // the scalar of t1 on the ex direction
        t2 = vmul(ex, i) // colinear vector to p13 with the length of i

        /* ey = (t1 - t2), t = |t1 - t2| */
        ey = vdiff(t1, t2) // vector t21 perpendicular to t1
        t = vnorm(ey) // scalar t21
        if (t > maxzero) {
            /* ey = (t1 - t2) / |t1 - t2| */
            ey = vdiv(ey, t) // unit vector ey with respect to p1 (new coordinate system)

            /* j = ey . (p3 - p1) */
            j = dot(ey, t1) // scalar t1 on the ey direction
        } else
            j = 0.0

        /* Note: t <= maxzero implies j = 0.0. */
        if (abs(j) <= maxzero) {

            /* Is point p1 + (r1 along the axis) the intersection? */
            t2 = vsum(p1, vmul(ex, r1))
            if (abs(vnorm(vdiff(p2, t2)) - r2) <= maxzero &&
                abs(vnorm(vdiff(p3, t2)) - r3) <= maxzero
            ) {
                /* Yes, t2 is the only intersection point. */
                if (result1 != null) {
                    result1.replaceFrom(t2)
                }
                if (result2 != null) {
                    result2.replaceFrom(t2)
                }
                return TRIL_3SPHERES
            }

            /* Is point p1 - (r1 along the axis) the intersection? */
            t2 = vsum(p1, vmul(ex, -r1))
            if (abs(vnorm(vdiff(p2, t2)) - r2) <= maxzero &&
                abs(vnorm(vdiff(p3, t2)) - r3) <= maxzero
            ) {
                /* Yes, t2 is the only intersection point. */
                if (result1 != null) {
                    result1.replaceFrom(t2)
                }
                if (result2 != null) {
                    result2.replaceFrom(t2)
                }
                return TRIL_3SPHERES
            }
            /* p1, p2 and p3 are colinear with more than one solution */
            return ERR_TRIL_COLINEAR_2SOLUTIONS
        }

        /* ez = ex x ey */
        ez = cross(ex, ey) // unit vector ez with respect to p1 (new coordinate system)

        x = (r1 * r1 - r2 * r2) / (2 * h) + h / 2
        y = (r1 * r1 - r3 * r3 + i * i) / (2 * j) + j / 2 - x * i / j
        z = r1 * r1 - x * x - y * y
        if (z < -maxzero) {
            /* The solution is invalid, square root of negative number */
            return ERR_TRIL_SQRTNEGNUMB
        } else
            if (z > 0.0)
                z = sqrt(z)
            else
                z = 0.0

        /* t2 = p1 + x ex + y ey */
        t2 = vsum(p1, vmul(ex, x))
        t2 = vsum(t2, vmul(ey, y))

        /* result1 = p1 + x ex + y ey + z ez */
        if (result1 != null) {
            result1.replaceFrom(vsum(t2, vmul(ez, z)))
        }

        /* result1 = p1 + x ex + y ey - z ez */
        if (result2 != null) {
            result2.replaceFrom(vsum(t2, vmul(ez, -z)))
        }

        /*********** END OF FINDING TWO POINTS FROM THE FIRST THREE SPHERES **********/
        /********* RESULT1 AND RESULT2 ARE SOLUTIONS, OTHERWISE RETURN ERROR *********/


        /************* FINDING ONE SOLUTION BY INTRODUCING ONE MORE SPHERE ***********/

        // check for concentricness of sphere 4 to sphere 1, 2 and 3
        // if it is concentric to one of them, then sphere 4 cannot be used
        // to determine the best solution and return -1

        /* h = |p4 - p1|, ex = (p4 - p1) / |p4 - p1| */
        ex = vdiff(p4, p1) // vector p14
        h = vnorm(ex) // scalar p14
        if (h <= maxzero) {
            /* p1 and p4 are concentric, not good to obtain a precise intersection point */
            //printf("concentric14 return 0\n")
            return TRIL_3SPHERES
        }
        /* h = |p4 - p2|, ex = (p4 - p2) / |p4 - p2| */
        ex = vdiff(p4, p2) // vector p24
        h = vnorm(ex) // scalar p24
        if (h <= maxzero) {
            /* p2 and p4 are concentric, not good to obtain a precise intersection point */
            //printf("concentric24 return 0\n")
            return TRIL_3SPHERES
        }
        /* h = |p4 - p3|, ex = (p4 - p3) / |p4 - p3| */
        ex = vdiff(p4, p3) // vector p34
        h = vnorm(ex) // scalar p34
        if (h <= maxzero) {
            /* p3 and p4 are concentric, not good to obtain a precise intersection point */
            //printf("concentric34 return 0\n")
            return TRIL_3SPHERES
        }

        // if sphere 4 is not concentric to any sphere, then best solution can be obtained
        /* find i as the distance of result1 to p4 */
        t3 = vdiff(result1, p4)
        i = vnorm(t3)
        /* find h as the distance of result2 to p4 */
        t3 = vdiff(result2, p4)
        h = vnorm(t3)

        /* pick the result1 as the nearest point to the center of sphere 4 */
        if (i > h) {
            best_solution.replaceFrom(result1)
            result1.replaceFrom(result2)
            result2.replaceFrom(best_solution)
        }

        var count4 = 0
        var rr4 = r4
        result = 1
        /* intersect result1-result2 vector with sphere 4 */
        while (result > 0 && count4 < 10) {
            result = sphereline(result1, result2, p4, rr4, temp)
            rr4 += 0.1
            count4++
        }
        println("-----1-----")
        if (result > 0) {
            /* No intersection between sphere 4 and the line with the gradient of result1-result2! */
            best_solution.replaceFrom(result1) // result1 is the closer solution to sphere 4
            //return ERR_TRIL_NOINTERSECTION_SPHERE4

        } else {
            if (temp.mu1 < 0 && temp.mu2 < 0) {

                /* if both mu1 and mu2 are less than 0 */
                /* result1-result2 line segment is outside sphere 4 with no intersection */
                if (abs(temp.mu1) <= abs(temp.mu2)) mu = temp.mu1 else mu = temp.mu2
                /* h = |result2 - result1|, ex = (result2 - result1) / |result2 - result1| */
                ex = vdiff(result2, result1) // vector result1-result2
                h = vnorm(ex) // scalar result1-result2
                ex = vdiv(ex, h) // unit vector ex with respect to result1 (new coordinate system)
                /* 50-50 error correction for mu */
                mu = 0.5 * mu
                /* t2 points to the intersection */
                t2 = vmul(ex, mu * h)
                t2 = vsum(result1, t2)
                /* the best solution = t2 */
                best_solution.replaceFrom(t2)

            } else if ((temp.mu1 < 0 && temp.mu2 > 1) || (temp.mu2 < 0 && temp.mu1 > 1)) {

                /* if mu1 is less than zero and mu2 is greater than 1, or the other way around */
                /* result1-result2 line segment is inside sphere 4 with no intersection */
                if (temp.mu1 > temp.mu2) mu = temp.mu1 else mu = temp.mu2
                /* h = |result2 - result1|, ex = (result2 - result1) / |result2 - result1| */
                ex = vdiff(result2, result1) // vector result1-result2
                h = vnorm(ex) // scalar result1-result2
                ex = vdiv(ex, h) // unit vector ex with respect to result1 (new coordinate system)
                /* t2 points to the intersection */
                t2 = vmul(ex, mu * h)
                t2 = vsum(result1, t2)
                /* vector t2-result2 with 50-50 error correction on the length of t3 */
                t3 = vmul(vdiff(result2, t2), 0.5)
                /* the best solution = t2 + t3 */
                best_solution.replaceFrom(vsum(t2, t3))

            } else if (((temp.mu1 > 0 && temp.mu1 < 1) && (temp.mu2 < 0 || temp.mu2 > 1))
                || ((temp.mu2 > 0 && temp.mu2 < 1) && (temp.mu1 < 0 || temp.mu1 > 1))
            ) {

                /* if one mu is between 0 to 1 and the other is not */
                /* result1-result2 line segment intersects sphere 4 at one point */
                if (temp.mu1 >= 0 && temp.mu1 <= 1) mu = temp.mu1 else mu = temp.mu2
                /* add or subtract with 0.5*mu to distribute error equally onto every sphere */
                if (mu <= 0.5) mu -= 0.5 * mu else mu -= 0.5 * (1 - mu)
                /* h = |result2 - result1|, ex = (result2 - result1) / |result2 - result1| */
                ex = vdiff(result2, result1) // vector result1-result2
                h = vnorm(ex) // scalar result1-result2
                ex = vdiv(ex, h) // unit vector ex with respect to result1 (new coordinate system)
                /* t2 points to the intersection */
                t2 = vmul(ex, mu * h)
                t2 = vsum(result1, t2)
                /* the best solution = t2 */
                best_solution.replaceFrom(t2)

            } else if (temp.mu1 == temp.mu2) {

                /* if both mu1 and mu2 are between 0 and 1, and mu1 = mu2 */
                /* result1-result2 line segment is tangential to sphere 4 at one point */
                mu = temp.mu1
                /* add or subtract with 0.5*mu to distribute error equally onto every sphere */
                if (mu <= 0.25) mu -= 0.5 * mu
                else if (mu <= 0.5) mu -= 0.5 * (0.5 - mu)
                else if (mu <= 0.75) mu -= 0.5 * (mu - 0.5)
                else mu -= 0.5 * (1 - mu)
                /* h = |result2 - result1|, ex = (result2 - result1) / |result2 - result1| */
                ex = vdiff(result2, result1) // vector result1-result2
                h = vnorm(ex) // scalar result1-result2
                ex = vdiv(ex, h) // unit vector ex with respect to result1 (new coordinate system)
                /* t2 points to the intersection */
                t2 = vmul(ex, mu * h)
                t2 = vsum(result1, t2)
                /* the best solution = t2 */
                best_solution.replaceFrom(t2)

            } else {

                /* if both mu1 and mu2 are between 0 and 1 */
                /* result1-result2 line segment intersects sphere 4 at two points */

                //return ERR_TRIL_NEEDMORESPHERE

                mu = temp.mu1 + temp.mu2
                /* h = |result2 - result1|, ex = (result2 - result1) / |result2 - result1| */
                ex = vdiff(result2, result1) // vector result1-result2
                h = vnorm(ex) // scalar result1-result2
                ex = vdiv(ex, h) // unit vector ex with respect to result1 (new coordinate system)
                /* 50-50 error correction for mu */
                mu = 0.5 * mu
                /* t2 points to the intersection */
                t2 = vmul(ex, mu * h)
                t2 = vsum(result1, t2)
                /* the best solution = t2 */
                best_solution.replaceFrom(t2)

            }

        }

        return TRIL_4SPHERES

        /******** END OF FINDING ONE SOLUTION BY INTRODUCING ONE MORE SPHERE *********/
    }

    /* This function calls trilateration to get the best solution.
 *
 * If any three spheres does not produce valid solution,
 * then each distance is increased to ensure intersection to happens.
 *
 * Return the selected trilateration mode between TRIL_3SPHERES or TRIL_4SPHERES
 * For TRIL_3SPHERES, there are two solutions: solution1 and solution2
 * For TRIL_4SPHERES, there is only one solution: best_solution
 *
 * nosolution_count = the number of failed attempt before intersection is found
 * by increasing the sphere diameter.
*/
    fun deca_3dlocate(
        solution1: Point,
        solution2: Point,
        best_solution: Point,
        nosolution_count: Int,
        best_3derror: Double,
        best_gdoprate: Double,
        p1: Point, r1: Double,
        p2: Point, r2: Double,
        p3: Point, r3: Double,
        p4: Point, r4: Double,
        combination: Int
    ): Int {
        var nosolution_count = nosolution_count
        var best_3derror = best_3derror
        var combination = combination
        var r1 = r1
        var r2 = r2
        var r3 = r3
        var r4 = r4

        val o1: Point = Point(0.0, 0.0, 0.0)
        val o2: Point = Point(0.0, 0.0, 0.0)
        val solution: Point = Point(0.0, 0.0, 0.0)
        val ptemp: Point = Point(0.0, 0.0, 0.0)
        val solution_compare1 = Point(0.0, 0.0, 0.0)
        val solution_compare2 = Point(0.0, 0.0, 0.0)
        var    /*error_3dcompare1, error_3dcompare2,*/ rtemp = 0.0
        var gdoprate_compare1 = 0.0
        var gdoprate_compare2 = 0.0
        var ovr_r1 = 0.0
        var ovr_r2 = 0.0
        var ovr_r3 = 0.0
        var ovr_r4 = 0.0
        var overlook_count = 0
        var combination_counter = 0
        var trilateration_errcounter = 0
        var trilateration_mode34 = 0
        var success = 0
        var concentric = 0
        var result = 0

        trilateration_errcounter = 0
        trilateration_mode34 = 0
        combination_counter = 1 /* four spheres combination */
        var best_gdoprate = best_gdoprate
        best_gdoprate = 1.0 /* put the worst gdoprate init */
        gdoprate_compare1 = 1.0
        gdoprate_compare2 = 1.0
//        solution_compare1.x = 0 solution_compare1.y = 0 solution_compare1.z = 0

        do {
            success = 0
            concentric = 0
            overlook_count = 0
            ovr_r1 = r1
            ovr_r2 = r2
            ovr_r3 = r3
            ovr_r4 = r4

            do {

                result = trilateration(o1, o2, solution, p1, ovr_r1, p2, ovr_r2, p3, ovr_r3, p4, ovr_r4, MAXZERO)

                when (result) {
                    TRIL_3SPHERES -> {// 3 spheres are used to get the result
                        trilateration_mode34 = TRIL_3SPHERES
                        success = 1
                    }

                    TRIL_4SPHERES -> {// 4 spheres are used to get the result
                        trilateration_mode34 = TRIL_4SPHERES
                        success = 1
                    }

                    ERR_TRIL_CONCENTRIC -> {
                        concentric = 1
                    }
                    else -> {// any other return value goes here
                        ovr_r1 += 0.10
                        ovr_r2 += 0.10
                        ovr_r3 += 0.10
                        ovr_r4 += 0.10
                        overlook_count++
                    }
                }

                //qDebug() << "while(!success)" << overlook_count << concentric << "result" << result
                println("success:%s, overlook_count:%s, concentric:%s".format(success, overlook_count, concentric))
            } while (success <= 0 && (overlook_count <= CM_ERR_ADDED) && concentric <= 0)
            println("-----2-----")
            if (success > 0) {
                when (result) {
                    TRIL_3SPHERES -> {
                        solution1.replaceFrom(o1)
                        solution2.replaceFrom(o2)
                        nosolution_count = overlook_count
                        combination_counter = 0
                    }

                    TRIL_4SPHERES -> {
                        /* calculate the new gdop */
                        gdoprate_compare1 = gdoprate(solution, p1, p2, p3)
                        /* compare and swap with the better result */
                        if (gdoprate_compare1 <= gdoprate_compare2) {
                            solution1.replaceFrom(o1)
                            solution2.replaceFrom(o2)
                            best_solution.replaceFrom(solution)
                            nosolution_count = overlook_count
                            best_3derror = sqrt(
                                (vnorm(vdiff(solution, p1)) - r1) * (vnorm(vdiff(solution, p1)) - r1) +
                                        (vnorm(vdiff(solution, p2)) - r2) * (vnorm(vdiff(solution, p2)) - r2) +
                                        (vnorm(vdiff(solution, p3)) - r3) * (vnorm(vdiff(solution, p3)) - r3) +
                                        (vnorm(vdiff(solution, p4)) - r4) * (vnorm(vdiff(solution, p4)) - r4)
                            )
                            best_gdoprate = gdoprate_compare1

                            /* save the previous result */
                            solution_compare2.replaceFrom(solution_compare1)
                            gdoprate_compare2 = gdoprate_compare1
                            combination = 5 - combination_counter

                        }

                        ptemp.replaceFrom(p1)
                        p1.replaceFrom(p2)
                        p2.replaceFrom(p3)
                        p3.replaceFrom(p4)
                        p4.replaceFrom(ptemp)
                        rtemp = r1
                        r1 = r2
                        r2 = r3
                        r3 = r4
                        r4 = rtemp
                        combination_counter--
                        if (combination_counter < 0) {
                            combination_counter = 0
                        }
                    }
                    else -> {
                    }
                }
            } else {
                trilateration_errcounter++
                combination_counter--
                if (combination_counter < 0) {
                    combination_counter = 0
                }
            }

        } while (combination_counter > 0)
        println("-----3-----")
        // if it gives error for all 4 sphere combinations then no valid result is given
        // otherwise return the trilateration mode used
        if (trilateration_errcounter >= 4)
            return -1
        else
            return trilateration_mode34

    }

    fun GetLocation(best_solution: Point, use4thAnchor: Int, anchors: Array<Point>, distances: Array<Int>): Int {

        val o1: Point = Point(0.0, 0.0, 0.0)
        val o2: Point = Point(0.0, 0.0, 0.0)
        val p1: Point = Point(0.0, 0.0, 0.0)
        val p2: Point = Point(0.0, 0.0, 0.0)
        val p3: Point = Point(0.0, 0.0, 0.0)
        val p4: Point = Point(0.0, 0.0, 0.0)
        var r1 = 0.0
        var r2 = 0.0
        var r3 = 0.0
        var r4 = 0.0
        var best_3derror = 0.0
        var best_gdoprate = 0.0
        var result = 0
        var error = 0
        var combination = 0

        if (use4thAnchor == 0)//3基站定位
        {
            if (distances[3] == 0)//A3无效 A0 A1 A2有效，执行3基站定位
            {
                /* Anchors coordinate */
                p1.replaceFrom(anchors[0].x, anchors[0].y, anchors[0].z)
                p2.replaceFrom(anchors[1].x, anchors[1].y, anchors[1].z)
                p3.replaceFrom(anchors[2].x, anchors[2].y, anchors[2].z)
                p4.replaceFrom(p1.x, p1.y, p1.z)

                r1 = distances[0] / 1000.0
                r2 = distances[1] / 1000.0
                r3 = distances[2] / 1000.0
                r4 = r1
            } else if (distances[0] == 0)//A0无效 A1 A2 A3有效，执行3基站定位
            {
                /* Anchors coordinate */
                p1.replaceFrom(anchors[1].x, anchors[1].y, anchors[1].z)
                p2.replaceFrom(anchors[2].x, anchors[2].y, anchors[2].z)
                p3.replaceFrom(anchors[3].x, anchors[3].y, anchors[3].z)
                p4.replaceFrom(p1.x, p1.y, p1.z)

                r1 = distances[1] / 1000.0
                r2 = distances[2] / 1000.0
                r3 = distances[3] / 1000.0
                r4 = r1
            } else if (distances[1] == 0)//A1无效 A0 A2 A3有效，执行3基站定位
            {
                /* Anchors coordinate */
                p1.replaceFrom(anchors[0].x, anchors[0].y, anchors[0].z)
                p2.replaceFrom(anchors[2].x, anchors[2].y, anchors[2].z)
                p3.replaceFrom(anchors[3].x, anchors[3].y, anchors[3].z)
                p4.replaceFrom(p1.x, p1.y, p1.z)

                r1 = distances[0] / 1000.0
                r2 = distances[2] / 1000.0
                r3 = distances[3] / 1000.0
                r4 = r1
            } else if (distances[2] == 0)//A2无效 A0 A1 A3有效，执行3基站定位
            {
                /* Anchors coordinate */
                p1.replaceFrom(anchors[0].x, anchors[0].y, anchors[0].z)
                p2.replaceFrom(anchors[1].x, anchors[1].y, anchors[1].z)
                p3.replaceFrom(anchors[3].x, anchors[3].y, anchors[3].z)
                p4.replaceFrom(p1.x, p1.y, p1.z)

                r1 = distances[0] / 1000.0
                r2 = distances[1] / 1000.0
                r3 = distances[3] / 1000.0
                r4 = r1
            }
        } else//4基站定位
        {
            /* Anchors coordinate */
            p1.replaceFrom(anchors[0].x, anchors[0].y, anchors[0].z)
            p2.replaceFrom(anchors[1].x, anchors[1].y, anchors[1].z)
            p3.replaceFrom(anchors[2].x, anchors[2].y, anchors[2].z)
            p4.replaceFrom(anchors[3].x, anchors[3].y, anchors[3].z)

            r1 = distances[0] / 1000.0
            r2 = distances[1] / 1000.0
            r3 = distances[2] / 1000.0
            r4 = distances[3] / 1000.0

        }

        result = deca_3dlocate(o1, o2, best_solution, error, best_3derror, best_gdoprate, p1, r1, p2, r2, p3, r3, p4, r4, combination)

        if (result >= 0) {
            if (o1.z <= o2.z) best_solution.z = o1.z else best_solution.z = o2.z
            if (use4thAnchor == 0 || result == TRIL_3SPHERES) {
                if (o1.z < p1.z) best_solution.replaceFrom(o1) else best_solution.replaceFrom(o2) //assume tag is below the anchors (1, 2, and 3)
            }

            return result

        }
        return -1
    }
}

fun main() {
    var result = 0
    val anchorArray = Array<Point>(4) { Point(0.0, 0.0, 0.0) }
    val report = Point(0.0, 0.0, 0.0)
    val Range_deca = Array<Int>(4) { 0 }

    //A0
    anchorArray[0].x = 0.0; //anchor0.x uint:m
    anchorArray[0].y = 0.0; //anchor0.y uint:m
    anchorArray[0].z = 2.0; //anchor0.z uint:m

    //A1
    anchorArray[1].x = -4.28; //anchor2.x uint:m
    anchorArray[1].y = -14.74; //anchor2.y uint:m
    anchorArray[1].z = 2.0; //anchor2.z uint:m

    //A2
    anchorArray[2].x = 26.26; //anchor2.x uint:m
    anchorArray[2].y = -15.10; //anchor2.y uint:m
    anchorArray[2].z = 2.0; //anchor2.z uint:m

    //A3
    anchorArray[3].x = 25.44; //anchor2.x uint:m
    anchorArray[3].y = -0.65; //anchor2.y uint:m
    anchorArray[3].z = 2.0; //anchor2.z uint:m


    Range_deca[0] = 26393; //tag to A0 distance
    Range_deca[1] = 29368; //tag to A1 distance
    Range_deca[2] = 8655; //tag to A2 distance
    Range_deca[3] = 8278; //tag to A2 distance

    result = Test.GetLocation(report, 1, anchorArray, Range_deca)

    println("result = %d".format(result))
    println("tag.x=%.3f\r\ntag.y=%.3f\r\ntag.z=%.3f".format(report.x, report.y, report.z))
}