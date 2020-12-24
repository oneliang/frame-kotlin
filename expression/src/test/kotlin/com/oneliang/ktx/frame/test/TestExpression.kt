package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.expression.*

fun main() {
    val expressionItemList = listOf(
            ExpressionItem().apply {
                this.id = 1
                this.leftId = 2
                this.rightId = 3
                this.expression = " \"{STEEL_P_IF_CUSTOM_SPECIFICATION}\" != \"是\""
                this.resultCode = "1_RESULT"
                this.type = ExpressionItem.Type.START.value
                this.calculateType = ExpressionItem.CalculateType.BOOLEAN.value
            },
            ExpressionItem().apply {
                this.id = 2
                this.leftId = 0
                this.rightId = 0
                this.expression = "{STEEL_P_ORDER_WIDTH}"
                this.resultCode = "2_RESULT"
                this.type = ExpressionItem.Type.END.value
                this.calculateType = ExpressionItem.CalculateType.CONSTANT.value
            },
            ExpressionItem().apply {
                this.id = 3
                this.leftId = 0
                this.rightId = 0
                this.expression = "1219"
                this.resultCode = "3_RESULT"
                this.type = ExpressionItem.Type.END.value
                this.calculateType = ExpressionItem.CalculateType.CONSTANT.value
            }
    )
    val begin = System.currentTimeMillis()
    val expressionGroupList = listOf(ExpressionGroup(expressionItemList, 1, "A"))
    val result = ExpressionExecutor.execute(mapOf(
            "STEEL_P_IF_CUSTOM_SPECIFICATION" to "否",
            "STEEL_P_ORDER_WIDTH" to "1500"), expressionGroupList)
    result.forEach {
        println(it.value)
    }
    println("cost:" + (System.currentTimeMillis() - begin) + "," + result)
    return
    println("true".eval())
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