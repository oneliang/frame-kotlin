package com.oneliang.ktx.frame.expression

import com.oneliang.ktx.Constants

class ExpressionNode(var nodeValue: String) {
    companion object {
        val INVALID_EXPRESSION_NODE = ExpressionNode(Constants.String.BLANK)
        /**
         * is datetime
         * @param string
         * @return Boolean
         */
        fun isDatetime(string: String): Boolean {
            var value = string
            value = value.replace("\"", "").trim { it <= ' ' }
            return value.matches("\\d{4}\\-\\d{2}\\-\\d{2}(\\s\\d{2}\\:\\d{2}\\:\\d{2})?".toRegex())
        }

        /**
         * 判断是否是一元操作符节点
         * @param type
         * @return Boolean
         */
        fun isUnitaryNode(type: Type): Boolean {
            return type == Type.PLUS || type == Type.MINUS
        }

        fun isNumeric(string: String): Boolean {
            return string.matches("^[\\+\\-]?(0|[1-9]\\d*|[1-9]\\d*\\.\\d+|0\\.\\d+)".toRegex())
        }

        /**
         * parse type
         * @param value
         * @return Type
         */
        private fun parseType(value: String): Type {
            if (value.isEmpty()) {
                return Type.UNKNOWN
            }
            when (value) {
                "+" -> return Type.PLUS
                "-" -> return Type.MINUS
                "*" -> return Type.MULTIPLY
                "/" -> return Type.DIVIDE
                "%" -> return Type.MODULUS
                "^" -> return Type.POWER
                "(" -> return Type.BRACKET_LEFT
                ")" -> return Type.BRACKET_RIGHT
                "&" -> return Type.BITWISE_AND
                "|" -> return Type.BITWISE_OR
                "&&", "<并且>", "并且" -> return Type.AND
                "||", "<或者>", "或者" -> return Type.OR
                "!" -> return Type.NOT
                "==", "=" -> return Type.EQUAL
                "!=", "<>", "≠" -> return Type.UNEQUAL
                ">" -> return Type.GREATER_THAN
                "<" -> return Type.LESS_THAN
                ">=", "≥" -> return Type.GREATER_THAN_OR_EQUAL
                "<=", "≤" -> return Type.LESS_THAN_OR_EQUAL
                "<<" -> return Type.LEFT_SHIFT
                ">>" -> return Type.RIGHT_SHIFT
                "@", "<包含>", "包含" -> return Type.LIKE
                "!@", "<不包含>", "不包含" -> return Type.NOT_LIKE
                "!!$" -> return Type.START_WITH
                "!!@" -> return Type.END_WITH
            }
            if (isNumeric(value)) {
                return Type.NUMERIC
            }
            if (isDatetime(value)) {
                return Type.DATE
            }
            return if (value.contains("\"")) {
                Type.STRING
            } else Type.UNKNOWN
        }

        /**
         * 获取各节点类型的优先级
         * @param type
         * @return Int
         */
        private fun getTypePriority(type: Type): Int {
            return when (type) {
                Type.BRACKET_LEFT, Type.BRACKET_RIGHT -> 9
                Type.NOT -> 8
                Type.MODULUS -> 7
                Type.MULTIPLY, Type.DIVIDE, Type.POWER -> 6
                Type.PLUS, Type.MINUS -> 5
                Type.LEFT_SHIFT, Type.RIGHT_SHIFT -> 4
                Type.BITWISE_AND, Type.BITWISE_OR -> 3
                Type.EQUAL, Type.UNEQUAL, Type.GREATER_THAN, Type.LESS_THAN, Type.GREATER_THAN_OR_EQUAL, Type.LESS_THAN_OR_EQUAL, Type.LIKE, Type.NOT_LIKE, Type.START_WITH, Type.END_WITH -> 2
                Type.AND, Type.OR -> 1
                else -> 0
            }
        }
    }

    var type: Type = parseType(this.nodeValue)
    var priority: Int = getTypePriority(type)
    /**
     * 设置或返回与当前节点相关联的一元操作符节点
     */
    var unitaryNode: ExpressionNode? = null
    var value: Any? = null
        set(value) {
            field = value
            nodeValue = field?.toString() ?: Constants.String.BLANK
        }
        get() {
            if (field == null) {
                if (type == Type.STRING || type == Type.DATE) {
                    return nodeValue
                }
                if (type != Type.NUMERIC) {
                    return Constants.String.ZERO
                }
                var number: Double = nodeValue.toDouble()
                val unitaryNode = this.unitaryNode
                if (unitaryNode != null && unitaryNode.type == Type.MINUS) {
                    number = 0 - number
                }
                field = number.toString()
            }
            return field
        }

    enum class Type {
        UNKNOWN,
        PLUS,  // +
        MINUS,  /// -
        MULTIPLY,  // *
        DIVIDE,  // /
        BRACKET_LEFT,  // (
        BRACKET_RIGHT,  /// )
        MODULUS,  // % (求模,取余)
        POWER,  // ^ (次幂)
        BITWISE_AND,  /// & (按位与)
        BITWISE_OR,  /// | (按位或)
        AND,  // && (逻辑与)
        OR,  /// || (逻辑或)
        NOT,  /// ! (逻辑非)
        EQUAL,  /// == (相等)
        UNEQUAL,  /// != 或 <> (不等于)
        GREATER_THAN,  /// > (大于)
        LESS_THAN,  /// < (小于)
        GREATER_THAN_OR_EQUAL,  /// >= (大于等于)
        LESS_THAN_OR_EQUAL,  /// <= (小于等于)
        LEFT_SHIFT,  /// << (左移位)
        RIGHT_SHIFT,  /// >> (右移位)
        NUMERIC,  /// 数值,
        STRING, DATE,
        LIKE,  // 包含
        NOT_LIKE,  // 不包含
        START_WITH,  // 已什么开始
        END_WITH // 已什么结尾
    }
}