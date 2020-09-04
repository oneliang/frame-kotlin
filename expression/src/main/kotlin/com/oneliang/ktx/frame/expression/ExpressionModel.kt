package com.oneliang.ktx.frame.expression

import com.oneliang.ktx.Constants

class ExpressionModel {
    var id = 0
    var leftId = 0
    var rightId = 0
    var parameters = Constants.String.BLANK
    var resultCode = Constants.String.BLANK
    var expression = Constants.String.BLANK
    var type = Type.DEFAULT.value
    var calculateType = CalculateType.CONSTANT.value

    enum class Type(val value: Int) {
        DEFAULT(0), START(1), END(2)
    }

    enum class CalculateType(val value: Int) {
        CONSTANT(0), BOOLEAN(1), NUMBER(2), STRING(3)
    }
}