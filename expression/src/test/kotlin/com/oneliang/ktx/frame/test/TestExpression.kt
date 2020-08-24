package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.expression.Expression

class Expression {

}

fun main() {
    val s1 = "1+2+3+4++"
    println(Expression.eval(s1))
    val s2 = "(20 - 6) < 3"
    println(Expression.eval(s2))
    val s3 = "(3 + 1) == 4 && 5 > (2 + 3)"
    println(Expression.eval(s3))
    val s4 = "\"hello\" == \"hello\" && 3 != 4"
    println(Expression.eval(s4))
    val s5 = "\"helloworld\" @ \"hello\" &&  \"helloworld\" !@ \"word\" "
    println(Expression.eval(s5))
    val s6 = "1+2*(3+4)/5"
    println(Expression.eval(s6))
}