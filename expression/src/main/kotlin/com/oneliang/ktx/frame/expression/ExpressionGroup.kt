package com.oneliang.ktx.frame.expression

class ExpressionGroup(val parameters: String, val expressionItemList: List<ExpressionItem>, val order: Int, val resultCode: String, val resultType: Int, val format: String) {

    enum class ResultType(val value: Int) {
        String(0), ROUND_HALF_UP(1), ROUND_CEILING(2), ROUND_FLOOR(3)
    }

}