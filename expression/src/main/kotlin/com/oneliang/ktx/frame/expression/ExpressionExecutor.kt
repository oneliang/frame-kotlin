package com.oneliang.ktx.frame.expression

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.toMap
import com.oneliang.ktx.util.logging.LoggerManager

object ExpressionExecutor {
    private val logger = LoggerManager.getLogger(ExpressionExecutor::class)

    fun execute(inputMap: Map<String, String>, expressionGroupList: List<ExpressionGroup>): List<ExpressionResult> {
        val sortedExpressionGroupList = expressionGroupList.sortedBy { it.order }
        val priorityInputMap = mutableMapOf<String, String>()
        return sortedExpressionGroupList.map {
            val expressionResult = execute(inputMap, it, priorityInputMap)
            priorityInputMap[it.resultCode] = expressionResult.value.toString()
            expressionResult
        }
    }

    fun execute(inputMap: Map<String, String>, expressionGroup: ExpressionGroup, priorityInputMap: Map<String, String> = emptyMap()): ExpressionResult {
        val parameters = expressionGroup.parameters
        val parameterList = if (parameters.isNotBlank()) {
            parameters.trim().split(Constants.Symbol.COMMA)
        } else {
            emptyList()
        }
        val optimizeInputMap = mutableMapOf<String, String>()
        parameterList.forEach {
            optimizeInputMap[it] = priorityInputMap[it] ?: inputMap[it].nullToBlank()
        }
        val value = execute(optimizeInputMap, priorityInputMap, expressionGroup.expressionItemList)
        return ExpressionResult(value)
    }

    fun execute(inputMap: Map<String, String>, priorityInputMap: Map<String, String> = emptyMap(), expressionItem: List<ExpressionItem>): Any {
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
            currentExpressionItem = executeExpressionItemAndFindNext(inputMap, priorityInputMap, expressionItemMap, currentExpressionItem, resultMap)
            if (expressionItemType == ExpressionItem.Type.END.value) {
                return resultMap[resultCode] ?: Expression.INVALID_EVAL_RESULT
            }
        }
        return Expression.INVALID_EVAL_RESULT
    }

    private fun executeExpressionItemAndFindNext(inputMap: Map<String, String>, priorityInputMap: Map<String, String>, expressionItemMap: Map<Int, ExpressionItem>, expressionItem: ExpressionItem, resultMap: MutableMap<String, Any>): ExpressionItem? {
        val parameterList = if (expressionItem.parameters.isNotBlank()) {
            expressionItem.parameters.trim().split(Constants.Symbol.COMMA)
        } else {
            emptyList()
        }
        val resultCode = expressionItem.resultCode
        val calculateType = expressionItem.calculateType
        val expression = if (parameterList.isEmpty()) {
            expressionItem.expression
        } else {
            var replaceExpression = expressionItem.expression
            parameterList.forEach {
                val value = resultMap[it]?.toString() ?: priorityInputMap[it] ?: inputMap[it].nullToBlank()
                replaceExpression = replaceExpression.replace(Constants.Symbol.BIG_BRACKET_LEFT + it + Constants.Symbol.BIG_BRACKET_RIGHT, value)
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