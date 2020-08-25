package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.expression.Expression
import com.oneliang.ktx.frame.expression.evalBoolean
import com.oneliang.ktx.frame.expression.evalNumber
import com.oneliang.ktx.frame.expression.evalString

fun main() {
    val s1 = "1+2+3+4"
    println(s1.evalNumber())
    val s2 = "(20 - 6) >= 14"
    println(s2.evalBoolean())
    val s3 = "(3 + 1) == 4 && 5 > (2 + 3)"
    println(Expression.eval(s3))
    val s4 = "\"hello\" == \"hello\" && 3 != 4"
    println(Expression.eval(s4))
    val s5 = "\"helloworld\" @ \"hello\" &&  \"helloworld\" !@ \"word\" "
    println(Expression.eval(s5))
    val s6 = "1+2*(3+4)/5"
    println(Expression.eval(s6))
    val s7 = "\"a\"+\"b\""
    println(s7.evalString())
}