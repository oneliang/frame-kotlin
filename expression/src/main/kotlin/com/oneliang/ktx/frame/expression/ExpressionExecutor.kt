package com.oneliang.ktx.frame.expression

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.mapWithFilter
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.parseRegexGroup
import com.oneliang.ktx.util.common.toMap
import com.oneliang.ktx.util.logging.LoggerManager
import java.math.RoundingMode
import java.text.DecimalFormat

object ExpressionExecutor {
    private val logger = LoggerManager.getLogger(ExpressionExecutor::class)
    private const val REGEX_KEYWORD = "\\{([\\w]+?)\\}"

    fun execute(inputMap: Map<String, String>, expressionGroupList: List<ExpressionGroup>, useIgnoreResult: Boolean = false): List<ExpressionResult> {
        val sortedExpressionGroupList = expressionGroupList.sortedBy { it.order }
        val priorityInputMap = mutableMapOf<String, String>()
        val expressionResultList = mutableListOf<ExpressionResult>()
        sortedExpressionGroupList.forEach {
            val expressionResult = execute(inputMap, it, priorityInputMap)
            priorityInputMap[it.resultCode] = expressionResult.value.toString()
            if (useIgnoreResult && it.ignoreResult) {
                return@forEach//continue
            }
            expressionResultList += expressionResult
        }
        return expressionResultList
    }

    fun execute(inputMap: Map<String, String>, expressionGroup: ExpressionGroup, priorityInputMap: Map<String, String> = emptyMap()): ExpressionResult {
        val value = execute(inputMap, priorityInputMap, expressionGroup.expressionItemList)
        return when (expressionGroup.resultType) {
            ExpressionGroup.ResultType.NUMBER_ROUND_HALF_UP.value -> {
                val decimalFormat = DecimalFormat(expressionGroup.format).apply { this.roundingMode = RoundingMode.HALF_UP }
                ExpressionResult(decimalFormat.format(value.toString().toDouble()))
            }
            ExpressionGroup.ResultType.NUMBER_ROUND_CEILING.value -> {
                val decimalFormat = DecimalFormat(expressionGroup.format).apply { this.roundingMode = RoundingMode.CEILING }
                ExpressionResult(decimalFormat.format(value.toString().toDouble()))
            }
            ExpressionGroup.ResultType.NUMBER_ROUND_FLOOR.value -> {
                val decimalFormat = DecimalFormat(expressionGroup.format).apply { this.roundingMode = RoundingMode.FLOOR }
                ExpressionResult(decimalFormat.format(value.toString().toDouble()))
            }
            else -> {
                ExpressionResult(value)
            }
        }
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
        val resultCode = expressionItem.resultCode
        val calculateType = expressionItem.calculateType
        val parameterList = expressionItem.expression.parseRegexGroup(REGEX_KEYWORD)
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