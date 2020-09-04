package com.oneliang.ktx.frame.expression

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.toMap
import com.oneliang.ktx.util.logging.LoggerManager

object ExpressionExecutor {
    private val logger = LoggerManager.getLogger(ExpressionExecutor::class)

    fun execute(inputMap: Map<String, String>, expressionModel: List<ExpressionModel>): Any {
        var startExpressionModel: ExpressionModel? = null
        val expressionModelMap = expressionModel.toMap {
            if (it.type == ExpressionModel.Type.START.value) {
                if (startExpressionModel == null) {
                    startExpressionModel = it
                } else {
                    error("duplicate start expression, ${it.expression}")
                }
            }
            it.id to it
        }
        val resultMap = mutableMapOf<String, Any>()
        var currentExpressionModel = startExpressionModel
        while (currentExpressionModel != null) {
            val resultCode = currentExpressionModel.resultCode
            val expressionModelType = currentExpressionModel.type
            currentExpressionModel = executeExpressionModelAndFindNext(inputMap, expressionModelMap, currentExpressionModel, resultMap)
            if (expressionModelType == ExpressionModel.Type.END.value) {
                return resultMap[resultCode] ?: Expression.INVALID_CALCULATE_RESULT
            }
        }
        return Expression.INVALID_CALCULATE_RESULT
    }

    private fun executeExpressionModelAndFindNext(inputMap: Map<String, String>, expressionModelMap: Map<Int, ExpressionModel>, expressionModel: ExpressionModel, resultMap: MutableMap<String, Any>): ExpressionModel? {
        val parameterList = if (expressionModel.parameters.isBlank()) {
            emptyList()
        } else {
            expressionModel.parameters.trim().split(Constants.Symbol.COMMA)
        }
        val resultCode = expressionModel.resultCode
        val calculateType = expressionModel.calculateType
        val expression = if (parameterList.isEmpty()) {
            expressionModel.expression
        } else {
            var replaceExpression = expressionModel.expression
            parameterList.forEach {
                replaceExpression = replaceExpression.replace(Constants.Symbol.BIG_BRACKET_LEFT + it + Constants.Symbol.BIG_BRACKET_RIGHT, inputMap[it].nullToBlank())
            }
            replaceExpression
        }
        val result: Any
        val nextExpressionModel: ExpressionModel?
        when (calculateType) {
            ExpressionModel.CalculateType.BOOLEAN.value -> {
                result = expression.evalBoolean()
                nextExpressionModel = if (result) {
                    expressionModelMap[expressionModel.leftId]
                } else {
                    expressionModelMap[expressionModel.rightId]
                }
            }
            ExpressionModel.CalculateType.NUMBER.value -> {
                result = expression.evalNumber()
                nextExpressionModel = expressionModelMap[expressionModel.rightId]
            }
            ExpressionModel.CalculateType.STRING.value -> {
                result = expression.evalString()
                nextExpressionModel = expressionModelMap[expressionModel.rightId]
            }
            ExpressionModel.CalculateType.CONSTANT.value -> {
                result = expression
                nextExpressionModel = expressionModelMap[expressionModel.rightId]
            }
            else -> {
                logger.error("It is not support the calculate type:%s", calculateType)
                return null
            }
        }
        if (resultCode.isNotBlank()) {
            resultMap[resultCode] = result
        }
        return nextExpressionModel
    }
}