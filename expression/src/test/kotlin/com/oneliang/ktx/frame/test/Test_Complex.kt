package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.test.function.Complex
import com.oneliang.ktx.frame.test.util.Point
import org.junit.Test

class Test_Complex {
    @Test
    fun Test_one() { // TODO Auto-generated method stub
        var f_x = "1+j"
        var result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "1+j*3"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "(1+j)*3"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "(1+2j)*3"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "(1+2j)^3"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "(1-2j)^3"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "ln((1-2j)^3)"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "log((1-2j)^3)"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "sqrt((1-2j)^3)"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "sin((2*3-2j)^0.5)"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "cos((3/2-2j)^0.5)"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "tan((3/2-2j)^(1+j))"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "tan((3/2-2j)^(1+j))"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "2*sinh((3/2-2j)^(1+j))"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "(2+j)*cosh((3/2-2j)^(j+2j))"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = " ((2+j)^2)/tanh((3/2-2j)^(j+2j))"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = " ((2+j)^2) -asin((3/2-2j)^(j+2j))"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = " ((2+j)^2) + acos((3/2-2j)^(5+2j))"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = " ((2+j)/2) + atan((3/2-2j)^(5/2j))"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = " e^(acos((3/2-2j)^(5+2j)))"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = " e^(acos((3/2-2j)^(pi)))"
        result = Parser.eval(f_x)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
    }

    @Test
    fun Test_two() {
        val xo = Point("x", Complex(1.0, 2.0))
        var f_x = " e^(1*x*acos((3/2-2j)^(pi)))"
        var result = Parser.eval(f_x, xo)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        val yo = Point("y", Complex(2.0, 1.0))
        f_x = " e^(1*x*y*acos((3/2)^(pi)))"
        result = Parser.eval(f_x, xo, yo)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = " e^(1*x*y*sin((3/2)^(pi)))"
        result = Parser.eval(f_x, xo, yo)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "x+j"
        result = Parser.eval(f_x, xo, yo)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
        f_x = "x-y"
        result = Parser.eval(f_x, xo, yo)
        println("real:" + result.complexValue.real + " imaginary:" + result.complexValue.imaginary)
    }

    @Test
    fun Test_three() {
        var f_x = "1+j +x"
        val xo = Point("x", "2 +j")
        var result = Parser.eval(f_x, xo)
        println("String Expressions-->real:" + result.complexValue.real + " imaginary:"
                + result.complexValue.imaginary)
        f_x = "1+j +x+y"
        val yo = Point("y", "2*1+1")
        result = Parser.eval(f_x, xo, yo)
        println("String Expressions-->real:" + result.complexValue.real + " imaginary:"
                + result.complexValue.imaginary)
        f_x = "1 +x + y"
        result = Parser.eval(f_x, xo, yo)
        println("String Expressions-->real:" + result.complexValue.real + " imaginary:"
                + result.complexValue.imaginary)
    }
}