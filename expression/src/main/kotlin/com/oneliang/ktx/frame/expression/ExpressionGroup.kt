package com.oneliang.ktx.frame.expression

import com.oneliang.ktx.Constants

class ExpressionGroup(val expressionItemList: List<ExpressionItem>, val order: Int, val resultCode: String, val resultType: Int = ResultType.STRING.value, val format: String = Constants.String.BLANK) {

    enum class ResultType(val value: Int) {
        STRING(0), NUMBER_ROUND_HALF_UP(1), NUMBER_ROUND_CEILING(2), NUMBER_ROUND_FLOOR(3)
    }
}