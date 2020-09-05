package com.oneliang.ktx.frame.expression

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.toMap
import com.oneliang.ktx.util.logging.LoggerManager

object ExpressionExecutor {
    private val logger = LoggerManager.getLogger(ExpressionExecutor::class)

    fun execute(expressionGroupList: List<ExpressionGroup>): List<ExpressionResult> {
        return expressionGroupList.map { execute(it) }
    }

    fun execute(expressionGroup: ExpressionGroup): ExpressionResult {
        val value = execute(expressionGroup.inputMap, expressionGroup.expressionItemList)
        return ExpressionResult(value)
    }

    fun execute(inputMap: Map<String, String>, expressionItem: List<ExpressionItem>): Any {
        var startExpressionItem: ExpressionItem? = null
        val expressionItemMap = expressionItem.toMap {
            if (it.type == ExpressionItem.Type.START.value) {
                if (startExpressionItem == null) {
                    startExpressionItem = it
                } else {
                    error("duplicate start expression, ${it.expression}")
                }
            }
            it.id to it
        }
        val resultMap = mutableMapOf<String, Any>()
        var currentExpressionItem = startExpressionItem
        while (currentExpressionItem != null) {
            val resultCode = currentExpressionItem.resultCode
            val expressionItemType = currentExpressionItem.type
            currentExpressionItem = executeExpressionItemAndFindNext(inputMap, expressionItemMap, currentExpressionItem, resultMap)
            if (expressionItemType == ExpressionItem.Type.END.value) {
                return resultMap[resultCode] ?: Expression.INVALID_EVAL_RESULT
            }
        }
        return Expression.INVALID_EVAL_RESULT
    }

    private fun executeExpressionItemAndFindNext(inputMap: Map<String, String>, expressionItemMap: Map<Int, ExpressionItem>, expressionItem: ExpressionItem, resultMap: MutableMap<String, Any>): ExpressionItem? {
        val parameterList = if (expressionItem.parameters.isBlank()) {
            emptyList()
        } else {
            expressionItem.parameters.trim().split(Constants.Symbol.COMMA)
        }
        val resultCode = expressionItem.resultCode
        val calculateType = expressionItem.calculateType
        val expression = if (parameterList.isEmpty()) {
            expressionItem.expression
        } else {
            var replaceExpression = expressionItem.expression
            parameterList.forEach {
                replaceExpression = replaceExpression.replace(Constants.Symbol.BIG_BRACKET_LEFT + it + Constants.Symbol.BIG_BRACKET_RIGHT, inputMap[it].nullToBlank())
            }
            replaceExpression
        }
        val result: Any
        val nextExpressionItem: ExpressionItem?
        when (calculateType) {
            ExpressionItem.CalculateType.BOOLEAN.value -> {
                result = expression.evalBoolean()
                nextExpressionItem = if (result) {
                    expressionItemMap[expressionItem.leftId]
                } else {
                    expressionItemMap[expressionItem.rightId]
                }
            }
            ExpressionItem.CalculateType.NUMBER.value -> {
                result = expression.evalNumber()
                nextExpressionItem = expressionItemMap[expressionItem.rightId]
            }
            ExpressionItem.CalculateType.STRING.value -> {
                result = expression.evalString()
                nextExpressionItem = expressionItemMap[expressionItem.rightId]
            }
            ExpressionItem.CalculateType.CONSTANT.value -> {
                result = expression
                nextExpressionItem = expressionItemMap[expressionItem.rightId]
            }
            else -> {
                logger.error("It is not support the calculate type:%s", calculateType)
                return null
            }
        }
        if (resultCode.isNotBlank()) {
            resultMap[resultCode] = result
        }
        return nextExpressionItem
    }
}