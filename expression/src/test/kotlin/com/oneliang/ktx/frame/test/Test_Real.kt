package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.test.util.Point
import org.junit.Assert
import org.junit.Test

class Test_Real {
    @Test
    fun Test_one() {
        val a = "1+2+3+4+5+6*9/3"
        println(Parser.eval(a).value)
        var f_x = " (2)-(5)"
        var result = Parser.eval(f_x).value
        Assert.assertTrue(result == -3.0)
        f_x = "((2)+(5))"
        result = Parser.eval(f_x).value
        Assert.assertTrue(result == 7.0)
        val xo = Point("x", 2.0)
        f_x = "5*(x+3)"
        result = Parser.eval(f_x, xo).value
        Assert.assertTrue(result == 25.0)
        f_x = "5*(2*(sqrt((x+2)^2)) +3)"
        result = Parser.eval(f_x, xo).value
        Assert.assertTrue(result == 55.0)
        f_x = "5*(2*(sqrt((x+2)^2)/2) +3)"
        result = Parser.eval(f_x, xo).value
        Assert.assertTrue(result == 35.0)
        f_x = "cosh(6+(2/0))"
        println("result:" + Parser.eval(f_x, xo).value)
        val f_xs = " 2*(-(((z*3)*sqrt(x^(2)))+3))"
        val zo = Point("z", 1.0)
        result = Parser.eval(f_xs, xo, zo).value
        Assert.assertTrue(result == -18.0)
        result = Parser.eval(f_xs, zo, xo).value
        Assert.assertTrue(result == -18.0)
        val x2 = Point("x", 0.0)
        f_x = "cos(x)"
        ParserManager.instance?.isDeegre = true
        result = Parser.eval(f_x, x2).value
        Assert.assertTrue(result == 1.0)
        ParserManager.instance?.isDeegre = false
        println("End test one")
    }

    @Test
    fun Test_two() {
        var f_x = " (2)-(5)"
        var result: Double = Parser.eval(f_x, emptyArray(), emptyArray())
        Assert.assertTrue(result == -3.0)
        f_x = "((2)+(5))"
        result = Parser.eval(f_x, emptyArray(), emptyArray())
        Assert.assertTrue(result == 7.0)
        val x0: Double = 2.0
        val values = arrayOf(x0)
        f_x = "5*(x +3)"
        result = Parser.eval(f_x, emptyArray(), values)
        Assert.assertTrue(result == 25.0)
        val f_xs = " 2*(-(((z*3)*sqrt(x^(2)))+3))"
        val z0: Double = 1.0
        val values2 = arrayOf(x0, z0)
        val vars = arrayOf("x", "z")
        result = Parser.eval(f_xs, vars, values2)
        Assert.assertTrue(result == -18.0)
        println("End test two")
    }

    @Test
    fun Test_three() {
        var f_x = "+3 +5*5*(+1)"
        var result = Parser.eval(f_x)
        Assert.assertTrue(result.value == 28.0)
        val xo = Point("x", 2.0)
        f_x = "2.35*e^(-3)*x"
        result = Parser.eval(f_x, xo)
        Assert.assertTrue(result.value == 0.2339992213289606)
        f_x = "sin(x)"
        result = Parser.eval(f_x, xo)
        Assert.assertTrue(result.value == 0.9092974268256817)
        val yo = Point("y", 1.0)
        val f_xs = "x+5*y+(3 -y)"
        result = Parser.eval(f_xs, xo, yo)
        Assert.assertTrue(result.value == 9.0)
        println("End test three")
    }

    @Test
    fun Test_four() {
        var f_xs = "x+5*y+(3 -y)"
        val xo = Point("x", "1+1")
        val yo = Point("y", "0+2*0+1*5-5 +1^4")
        var result = Parser.eval(f_xs, xo, yo)
        Assert.assertTrue(result.value == 9.0)
        val f_x = "2.35*e^(-3)*x"
        result = Parser.eval(f_x, xo)
        Assert.assertTrue(result.value == 0.2339992213289606)
        f_xs = " 2*(-(((z*3)*sqrt(x^(2)))+3))"
        val zo = Point("z", 1.0)
        result = Parser.eval(f_xs, zo, xo)
        Assert.assertTrue(result.value == -18.0)
        println("End test four")
    }
}