package com.oneliang.ktx.frame.expression

class ExpressionParser(var expression: String) {

    companion object {
        /**
         * 判断两个字符是否是同一类
         * @param charOne
         * @param charTwo
         * @return Boolean
         */
        fun isCongener(charOne: Char, charTwo: Char): Boolean {
            if (charOne == '(' || charTwo == '(') {
                return false
            }
            if (charOne == ')' || charTwo == ')') {
                return false
            }
            if (charOne == '"' || charTwo == '"') {
                return false
            }
            return if (Character.isDigit(charOne) || charOne == '.') { // c1为数字,则c2也为数字
                Character.isDigit(charTwo) || charTwo == '.'
            } else !Character.isDigit(charTwo) && charTwo != '.'
        }

        fun needMoreOperator(char: Char): Boolean {
            when (char) {
                '&', '|', '=', '!', '>', '<', '.' -> return true
            }
            return Character.isDigit(char)
        }
    }

    var position = 0

    /**
     * read expression node
     * @return Pair<Boolean, ExpressionNode>
     */
    fun readNode(): Pair<Boolean, ExpressionNode> {
        var whileSpacePosition = -1
        var flag = false
        val stringBuilder = StringBuilder()
        while (this.position < this.expression.length) {
            val char = this.expression[this.position]
            if (char == '"') {
                flag = !flag
                if (!flag) {
                    this.position++
                    stringBuilder.append(char)
                    break
                }
                if (stringBuilder.isNotEmpty()) {
                    break
                }
            }
            if (flag) {
                this.position++
                stringBuilder.append(char)
            } else {
                if (char.isWhitespace()) {
                    if (whileSpacePosition >= 0 && this.position - whileSpacePosition > 1) {
                        throw ExpressionException(String.format("expression \"%s\" in position (%s) is illegal", expression, position))
                    }
                    whileSpacePosition = if (stringBuilder.isEmpty()) {
                        -1
                    } else {
                        this.position
                    }
                    this.position++
                    continue
                }
                if (stringBuilder.isEmpty() || isCongener(char, stringBuilder[stringBuilder.length - 1])) {
                    this.position++
                    stringBuilder.append(char)
                } else {
                    break
                }
                if (!needMoreOperator(char)) {
                    break
                }
            }
        }
        if (stringBuilder.isEmpty()) {
            return false to ExpressionNode.INVALID_EXPRESSION_NODE
        }
        val node = ExpressionNode(stringBuilder.toString())
        if (node.type == ExpressionNode.Type.UNKNOWN) {
            throw ExpressionException(String.format("expression \"%s\" in position(%s) with char \"%s\" is illegal", expression, position - node.nodeValue.length, node.nodeValue))
        }
        return true to node
    }
}