package com.oneliang.ktx.frame.expression

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.toUtilDate
import java.util.*
import kotlin.math.pow

object Expression {
    private val INVALID_DATA = Any()
    val INVALID_EVAL_RESULT = INVALID_DATA

    /**
     * 将算术表达式转换为逆波兰表达式
     * @param expression 要计算的表达式,如"1+2+3+4"
     * @return MutableList<ExpressionNode>
     */
    private fun parseExpression(expression: String): MutableList<ExpressionNode> {
        if (expression.isEmpty()) {
            return mutableListOf()
        }
        val listOperator = mutableListOf<ExpressionNode>()
        val stackOperator = Stack<ExpressionNode>()
        val expressionParser = ExpressionParser(expression)
        var beforeExpNode: ExpressionNode? // 前一个节点
        var unitaryNode: ExpressionNode? = null // 一元操作符
        var (success, currentExpressionNode) = expressionParser.readNode()
        // 是否需要操作数
        var requireOperand = false
        while (success) {
            if (currentExpressionNode.type == ExpressionNode.Type.NUMBER
                || currentExpressionNode.type == ExpressionNode.Type.STRING
                || currentExpressionNode.type == ExpressionNode.Type.DATE
            ) { // 操作数， 直接加入后缀表达式中
                if (unitaryNode != null) { // 设置一元操作符节点
                    currentExpressionNode.unitaryNode = unitaryNode
                    unitaryNode = null
                }
                listOperator.add(currentExpressionNode)
                requireOperand = false
            } else if (currentExpressionNode.type == ExpressionNode.Type.BRACKET_LEFT) { // 左括号， 直接加入操作符栈
                stackOperator.push(currentExpressionNode)
            } else if (currentExpressionNode.type == ExpressionNode.Type.BRACKET_RIGHT) { // 右括号则在操作符栈中反向搜索，直到遇到匹配的左括号为止，将中间的操作符依次加到后缀表达式中。
                var lpNode: ExpressionNode? = null
                while (stackOperator.size > 0) {
                    lpNode = stackOperator.pop()
                    if (lpNode.type == ExpressionNode.Type.BRACKET_LEFT) break
                    listOperator.add(lpNode)
                }
                if (lpNode == null || lpNode.type != ExpressionNode.Type.BRACKET_LEFT) {
                    throw ExpressionException(String.format("expression \"%s\" in position(%s) lose \")\"", expressionParser.expression, expressionParser.position))
                }
            } else {
                if (stackOperator.size == 0) { // 第一个节点则判断此节点是否是一元操作符"+,-,!,("中的一个,否则其它都非法
                    if (listOperator.size == 0 && !(currentExpressionNode.type == ExpressionNode.Type.BRACKET_LEFT || currentExpressionNode.type == ExpressionNode.Type.NOT)) { // 后缀表达式没有任何数据则判断是否是一元操作数
                        unitaryNode = if (ExpressionNode.isUnitaryNode(currentExpressionNode.type)) {
                            currentExpressionNode
                        } else { // 丢失操作数
                            throw ExpressionException(String.format("expression \"%s\" in position(%s) lose operator", expressionParser.expression, expressionParser.position))
                        }
                    } else { // 直接压入操作符栈
                        stackOperator.push(currentExpressionNode)
                    }
                    requireOperand = true // 下一个节点需要操作数
                } else {
                    if (requireOperand) { // 如果需要操作数则判断当前的是否是"+","-"号(一元操作符),如果是则继续
                        unitaryNode = if (ExpressionNode.isUnitaryNode(currentExpressionNode.type) && unitaryNode == null) {
                            currentExpressionNode
                        } else { // 丢失操作数
                            throw ExpressionException(String.format("expression \"%s\" in position (%s) lose operator", expressionParser.expression, expressionParser.position))
                        }
                    } else { // 对前面的所有操作符进行优先级比较
                        do { // 取得上一次的操作符
                            beforeExpNode = stackOperator.peek()
                            // 如果前一个操作符优先级较高，则将前一个操作符加入后缀表达式中
                            if (beforeExpNode.type != ExpressionNode.Type.BRACKET_LEFT
                                && beforeExpNode.priority - currentExpressionNode.priority >= 0
                            ) {
                                listOperator.add(stackOperator.pop())
                            } else {
                                break
                            }
                        } while (stackOperator.size > 0)
                        // 将操作符压入操作符栈
                        stackOperator.push(currentExpressionNode)
                        requireOperand = true
                    }
                }
            }
            val readNodeResult = expressionParser.readNode()
            success = readNodeResult.first
            currentExpressionNode = readNodeResult.second
        }
        if (requireOperand) { // 丢失操作数
            throw ExpressionException(String.format("expression \"%s\" in position(%s) lose operate data", expressionParser.expression, expressionParser.position))
        }
        // 清空堆栈
        while (stackOperator.size > 0) { // 取得操作符
            beforeExpNode = stackOperator.pop()
            if (beforeExpNode.type == ExpressionNode.Type.BRACKET_LEFT) {
                throw ExpressionException(String.format("expression \"%s\" in position(%s) lose \")\"", expressionParser.expression, expressionParser.position))
            }
            listOperator.add(beforeExpNode)
        }
        return listOperator
    }

    /**
     * eval expression
     * @param nodeList
     * @return Any, in fact is boolean or double or string or INVALID_EVAL_RESULT
     */
    private fun evalExpression(nodeList: MutableList<ExpressionNode>): Any {
        if (nodeList.size == 0) return INVALID_EVAL_RESULT
        if (nodeList.size > 1) {
            var index = 0
            // 储存数据
            val valueList = mutableListOf<Any>()//not include operator
            while (index < nodeList.size) {
                val node = nodeList[index]
                if (node.isDataNode()) {
                    valueList.add(node.calculateValue)
                    index++
                } else {
                    // 二元表达式，需要二个参数， 如果是Not的话，则只要一个参数
                    var paramCount = 2
                    if (node.type == ExpressionNode.Type.NOT) paramCount = 1
                    // 计算操作数的值
                    if (valueList.size < paramCount) {
                        throw ExpressionException("lose operator")
                    }
                    // 传入参数
                    val data = Array(paramCount) {
                        valueList[index - paramCount + it]
                    }
                    // 将计算结果再存入当前节点
                    node.calculateValue = calculate(node.type, data)
                    node.type = if (node.calculateValue is String) {
                        ExpressionNode.Type.STRING
                    } else {
                        ExpressionNode.Type.NUMBER
                    }
                    // 将操作数节点删除
                    var i = 0
                    while (i < paramCount) {
                        nodeList.removeAt(index - i - 1)
                        valueList.removeAt(index - i - 1)
                        i++
                    }
                    index -= paramCount
                }
            }
        }
        if (nodeList.size != 1) {
            throw ExpressionException("lose operator")
        }
        val node = nodeList[0]
        return when (node.type) {
            ExpressionNode.Type.NUMBER -> node.calculateValue
            ExpressionNode.Type.STRING, ExpressionNode.Type.DATE -> node.calculateValue.toString().replace("\"", Constants.String.BLANK)
            else -> {
                throw ExpressionException("lose operator")
            }
        }
    }

    /**
     * calculate node
     * @param type type
     * @param datas size may be one or two
     * @return Any, in fact is boolean or double or string or INVALID_EVAL_RESULT
     */
    private fun calculate(type: ExpressionNode.Type, datas: Array<Any>): Any {
        if (!(datas.size == 1 || datas.size == 2)) {
            return INVALID_EVAL_RESULT
        }
        val dataOne = datas[0]
        val dataTwo = datas.getOrElse(1) { INVALID_DATA }
        var stringOne = dataOne.toString()
        var stringTwo = dataTwo.toString()
        val dateFlag = ExpressionNode.isDatetime(stringOne) || ExpressionNode.isDatetime(stringTwo)
        val stringFlag = stringOne.contains(Constants.Symbol.DOUBLE_QUOTE) || stringTwo.contains(Constants.Symbol.DOUBLE_QUOTE)
        stringOne = stringOne.replace(Constants.Symbol.DOUBLE_QUOTE, Constants.String.BLANK)
        stringTwo = stringTwo.replace(Constants.Symbol.DOUBLE_QUOTE, Constants.String.BLANK)
        when (type) {
            ExpressionNode.Type.PLUS -> {
                if (!stringFlag) {
                    return convertToDouble(dataOne) + convertToDouble(dataTwo)
                }
                return stringOne + stringTwo
            }
            ExpressionNode.Type.MINUS -> {
                return convertToDouble(dataOne) - convertToDouble(dataTwo)
            }
            ExpressionNode.Type.MULTIPLY -> {
                return convertToDouble(dataOne) * convertToDouble(dataTwo)
            }
            ExpressionNode.Type.DIVIDE -> {
                val one = convertToDouble(dataOne)
                val two = convertToDouble(dataTwo)
                if (two == 0.0) error("can not divide zero")
                return one / two
            }
            ExpressionNode.Type.POWER -> {
                return convertToDouble(dataOne).pow(convertToDouble(dataTwo))
            }
            ExpressionNode.Type.MODULUS -> {
                val one = convertToDouble(dataOne)
                val two = convertToDouble(dataTwo)
                if (two == 0.0) error("can not modulus zero")
                return one % two
            }
            ExpressionNode.Type.BITWISE_AND -> {
                val one = convertToDouble(dataOne)
                val two = convertToDouble(dataTwo)
                return one.toInt() and two.toInt()
            }
            ExpressionNode.Type.BITWISE_OR -> {
                val one = convertToDouble(dataOne)
                val two = convertToDouble(dataTwo)
                return one.toInt() or two.toInt()
            }
            ExpressionNode.Type.AND -> {
                return convertToBoolean(dataOne) && convertToBoolean(dataTwo)
            }
            ExpressionNode.Type.OR -> {
                return convertToBoolean(dataOne) || convertToBoolean(dataTwo)
            }
            ExpressionNode.Type.NOT -> {
                return !convertToBoolean(dataOne)
            }
            ExpressionNode.Type.EQUAL -> {
                if (!dateFlag) {
                    if (stringFlag) {
                        return stringOne == stringTwo
                    }
                    val one = convertToDouble(dataOne)
                    val two = convertToDouble(dataTwo)
                    return one == two
                }
                val timeOne = stringOne.toUtilDate()
                val timeTwo = stringTwo.toUtilDate()
                return timeOne.time == timeTwo.time
            }
            ExpressionNode.Type.UNEQUAL -> {
                if (!dateFlag) {
                    if (stringFlag) {
                        return stringOne != stringTwo
                    }
                    return convertToDouble(dataOne) != convertToDouble(dataTwo)
                }
                val time1 = stringOne.toUtilDate()
                val time2 = stringTwo.toUtilDate()
                return time1.time != time2.time
            }
            ExpressionNode.Type.GREATER_THAN -> {
                if (!dateFlag) {
                    return convertToDouble(dataOne) > convertToDouble(dataTwo)
                }
                val timeOne = stringOne.toUtilDate()
                val timeTwo = stringTwo.toUtilDate()
                return timeOne.time > timeTwo.time
            }
            ExpressionNode.Type.LESS_THAN -> {
                if (!dateFlag) {
                    return convertToDouble(dataOne) < convertToDouble(dataTwo)
                }
                val timeOne = stringOne.toUtilDate()
                val timeTwo = stringTwo.toUtilDate()
                return timeOne.time < timeTwo.time
            }
            ExpressionNode.Type.GREATER_THAN_OR_EQUAL -> {
                if (!dateFlag) {
                    return convertToDouble(dataOne) >= convertToDouble(dataTwo)
                }
                val timeOne = stringOne.toUtilDate()
                val timeTwo = stringTwo.toUtilDate()
                return timeOne.time >= timeTwo.time
            }
            ExpressionNode.Type.LESS_THAN_OR_EQUAL -> {
                if (!dateFlag) {
                    return convertToDouble(dataOne) <= convertToDouble(dataTwo)
                }
                val timeOne = stringOne.toUtilDate()
                val timeTwo = stringTwo.toUtilDate()
                return timeOne.time <= timeTwo.time
            }
            ExpressionNode.Type.LEFT_SHIFT -> {
                return convertToDouble(dataOne).toLong() shl convertToDouble(dataTwo).toInt()
            }
            ExpressionNode.Type.RIGHT_SHIFT -> {
                return convertToDouble(dataOne).toLong() shr convertToDouble(dataTwo).toInt()
            }
            ExpressionNode.Type.LIKE -> {
                return if (!stringFlag) {
                    false
                } else stringOne.contains(stringTwo)
            }
            ExpressionNode.Type.NOT_LIKE -> {
                return if (!stringFlag) {
                    false
                } else !stringOne.contains(stringTwo)
            }
            ExpressionNode.Type.STARTS_WITH -> {
                return if (!stringFlag) {
                    false
                } else stringOne.startsWith(stringTwo)
            }
            ExpressionNode.Type.ENDS_WITH -> {
                return if (!stringFlag) {
                    false
                } else stringOne.endsWith(stringTwo)
            }
            else -> return 0
        }
    }

    /**
     * convert to boolean
     * @param value
     * @return
     */
    private fun convertToBoolean(value: Any): Boolean {
        return if (value is Boolean) {
            value
        } else {
            false
        }
    }

    /**
     * convert to double
     * @param value
     * @return Double
     */
    private fun convertToDouble(value: Any): Double {
        return if (value is Boolean) {
            if (value) 1.0 else 0.0
        } else {
            value.toString().toDouble()
        }
    }

    fun eval(expression: String): Any {
        val value = try {
            evalExpression(parseExpression(expression))
        } catch (e: Throwable) {
            INVALID_EVAL_RESULT
        }
        return if (value != INVALID_EVAL_RESULT) {
            value
        } else {
            expression
        }
    }

    fun evalBoolean(expression: String): Boolean = try {
        val result = eval(expression)
        if (result is Boolean) {
            result
        } else {
            false
        }
    } catch (e: Throwable) {
        false
    }

    fun evalNumber(expression: String): Double = try {
        val result = eval(expression)
        if (result is Double) {
            result
        } else {
            0.0
        }
    } catch (e: Throwable) {
        0.0
    }

    fun evalString(expression: String) = try {
        val result = eval(expression)
        if (result is String) {
            result
        } else {
            Constants.String.BLANK
        }
    } catch (e: Throwable) {
        Constants.String.BLANK
    }

    fun evalThreeOperand(expression: String): Any? {
        var index = expression.indexOf("?")
        if (index > -1) {
            val stringOne = expression.substring(0, index)
            val stringTwo = expression.substring(index + 1)
            index = stringTwo.indexOf(":")
            return if (java.lang.Boolean.parseBoolean(evalExpression(parseExpression(stringOne)).toString())) {
                eval(stringTwo.substring(0, index))
            } else eval(stringTwo.substring(index + 1))
        }
        return evalExpression(parseExpression(expression))
    }
}