package com.oneliang.ktx.frame.calculator

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.perform
import com.oneliang.ktx.util.common.toDoubleSafely
import com.oneliang.ktx.util.common.toFloatSafely
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import javax.script.Bindings
import javax.script.Invocable
import javax.script.ScriptEngineManager
import kotlin.math.abs

class Calculator(private val code: String) {
    companion object {
        private const val CODE_TYPE_ADDITION = "ADDITION"
        private const val CODE_TYPE_FORMULA = "FORMULA"
        private const val RETURN_SUFFIX = "_RESULT"
    }

    private val logger = LoggerManager.getLogger(Calculator::class)
    private val scriptEngineManager = ScriptEngineManager()
    private val engine = scriptEngineManager.getEngineByName("js")
    private val allFormulaItemMap = ConcurrentHashMap<String, List<FormulaItem>>()
    private val updateLock = ReentrantLock()

    fun updateFormulaItemListOfEngine(formulaItemList: List<FormulaItem>) {
        this.updateLock.lock()
        perform({
            val sortedFormulaItemList = formulaItemList.sortedBy { it.order }
            this.allFormulaItemMap[this.code] = sortedFormulaItemList
            sortedFormulaItemList.forEach { formulaItem ->
                this.engine.eval(formulaItem.javaScriptFunction)
            }
        }, finally = {
            this.updateLock.unlock()
        })
    }

    fun calculate(inputMap: Map<String, String>,
                  additionInputMap: Map<String, String> = emptyMap(),
                  totalResultCode: String = Constants.String.BLANK,//no total result
                  calculateResultItemMapping: Map<String, String> = emptyMap(),
                  checkCalculateResultItem: Boolean = false,
                  originalFormulaResultMap: Map<String, String> = emptyMap(),
                  optimizeResultProcessor: (result: Double) -> String = { it.toString() }): CalculateResult {
        if (!allFormulaItemMap.containsKey(code)) {
            logger.error("Please update formula item list first, map not contains cache data, product type code:%s", code)
            return CalculateResult(false, emptyMap(), emptyMap())
        }
        val allFormulaItemList = allFormulaItemMap[code] ?: emptyList()
        if (allFormulaItemList.isEmpty()) {
            logger.info("Please update formula item list first, formula item is empty, product type code:%s", code)
        }
        if (engine !is Invocable) {
            logger.error("engine is not invocable")
            return CalculateResult(false, emptyMap(), emptyMap())
        }
        val calculateResultItemMap = mutableMapOf<String, CalculateResultItem>()
        //mapping formula result without initialize formula type
        val calculateResultItemMappingMap = mutableMapOf<String, CalculateResultItem>()
        val resultList = mutableListOf<String>()
        var addition = 0F//附加加价
        //addition result
        additionInputMap.forEach {
            val returnCode = it.key + RETURN_SUFFIX
            val result = it.value
            if (checkCalculateResultItem) {
                if (!originalFormulaResultMap.containsKey(returnCode)) {
                    logger.error("check addition input data error, miss returnCode:$returnCode")
                    return CalculateResult(false, emptyMap(), calculateResultItemMap)
                }
                val originalFormulaResult = originalFormulaResultMap[returnCode]
                if (result != originalFormulaResult) {
                    logger.error("check addition input data error, returnCode:$returnCode, result:$result, original formula result:$originalFormulaResult")
                    return CalculateResult(false, emptyMap(), calculateResultItemMap)
                }
            }
            logger.info("Addition:$returnCode, result:$result")
            addition += result.toFloatSafely()
            val calculateResultItem = CalculateResultItem(returnCode, returnCode, value = result, codeType = CODE_TYPE_ADDITION)
            calculateResultItemMap[returnCode] = calculateResultItem
            calculateResultItemMappingMap[returnCode] = calculateResultItem
        }
        //formula input data initialize
        val optimizeInputMap = inputMap.toMutableMap()

        if (calculateResultItemMapping.isEmpty()) {
            logger.info("calculate result item mapping is empty, code:%s", code)
        } else {
            //formula process and result
            allFormulaItemList.forEach { formulaItem ->
                val formulaItemCode = formulaItem.code
                if (!calculateResultItemMapping.containsKey(formulaItemCode)) {
                    logger.verbose("No need to execute formula, code:%s", formulaItemCode)
                    return@forEach
                }
                val formulaItemType = formulaItem.formulaType
                val (inputJson, result) = when (formulaItem.parameterType) {
                    FormulaItem.ParameterType.JSON_OBJECT -> {
                        val inputJson = optimizeInputMap.toJson()
                        inputJson to this.engine.invokeFunction(formulaItemCode, inputJson)
                    }
                    FormulaItem.ParameterType.STRING -> {
                        val formulaInputList = mutableListOf<String>()
                        val parameterList = formulaItem.parameter.split(Constants.Symbol.COMMA)
                        parameterList.forEach parameterList@{
                            val parameterKey = it.trim()
                            if (parameterKey.isBlank()) {
                                logger.error("Formula:%s has blank parameter, please check and confirm it", formulaItemCode)
                                return@parameterList
                            }
                            when {
                                optimizeInputMap.containsKey(parameterKey) -> {
                                    formulaInputList.add(optimizeInputMap.getValue(parameterKey))
                                }
                                calculateResultItemMap.containsKey(parameterKey) -> {
                                    val result = calculateResultItemMap[parameterKey]!!.value
                                    formulaInputList.add(result)
                                }
                                else -> logger.error("Formula:%s error, %s not found", formulaItemCode, parameterKey)
                            }
                        }
                        val inputTypeArray = formulaInputList.toTypedArray()
                        val inputJson = inputTypeArray.toJson()
                        inputJson to this.engine.invokeFunction(formulaItemCode, *(inputTypeArray))
                    }
                }
                val (fixValue, resultJson) = when (result) {
                    null -> Constants.String.NULL to Constants.String.NULL
                    is Bindings -> {
                        result.forEach { (key, value) ->
                            optimizeInputMap[key] = value?.toString().nullToBlank()
                        }
                        Constants.String.BLANK to result.toJson()
                    }
                    else -> {
                        val value = result.toString()
                        value to value
                    }
                }
                logger.info("Formula:%s(%s),result:%s, %s", formulaItem.name, formulaItemCode, resultJson, inputJson)
                val fixInputJson = if (formulaItem.parameterType == FormulaItem.ParameterType.JSON_OBJECT) {
                    Constants.String.BLANK
                } else {
                    inputJson
                }
                val formulaReturnCode = formulaItem.returnCode
                calculateResultItemMap[formulaReturnCode] = CalculateResultItem(formulaItem.name, formulaReturnCode, inputJson = fixInputJson, value = fixValue, codeType = CODE_TYPE_FORMULA)

                if (formulaItem.formulaType != FormulaItem.FormulaType.RESULT) {
                    return@forEach//continue, no need to save
                }

                //map quote formula result
                if (calculateResultItemMapping.containsKey(formulaItemCode)) {
                    val formulaReturnCodeMapping = calculateResultItemMapping[formulaItemCode].nullToBlank()
                    if (formulaReturnCodeMapping.isNotBlank()) {
                        val formulaResultValue = result.toString().toDoubleSafely()
                        val calculateResultItem = calculateResultItemMappingMap.getOrPut(formulaReturnCodeMapping) { CalculateResultItem(formulaReturnCodeMapping, formulaReturnCodeMapping, Constants.String.ZERO, codeType = CODE_TYPE_FORMULA) }
                        val originalCalculateResultItemValue = calculateResultItem.value.toDoubleSafely()
                        calculateResultItem.value = "%.2f".format(originalCalculateResultItemValue + formulaResultValue)
                    }
                }
                if (result == null) {
                    logger.error("Error, result is null, did you forget the keyword 'return'")
                    return CalculateResult(false, optimizeInputMap, calculateResultItemMap, calculateResultItemMappingMap)
                } else {
                    if (formulaItemType == FormulaItem.FormulaType.RESULT) {
                        resultList += result.toString()
                        if (checkCalculateResultItem) {
                            if (!originalFormulaResultMap.containsKey(formulaReturnCode)) {
                                logger.error("Check original formula result error, miss formulaReturnCode:$formulaReturnCode")
                                return CalculateResult(false, optimizeInputMap, calculateResultItemMap, calculateResultItemMappingMap)
                            }
                            val originalFormulaResult = originalFormulaResultMap[formulaReturnCode]
                            if (result.toString() != originalFormulaResult) {
                                logger.error("Check original formula result error, formulaReturnCode:$formulaReturnCode, (result)/(originalFormulaResult):($result)/($originalFormulaResult)")
//                                return CalculateResult(false, calculateResultItemMap, calculateResultItemMappingMap)
                            }
                        }
                    }
                }
            }
        }

        if (totalResultCode.isBlank()) {//no total result
            return CalculateResult(true, optimizeInputMap, calculateResultItemMap, calculateResultItemMappingMap)
        }
        //has total result
        var totalValue = 0.0
        resultList.forEach {
            totalValue += it.toDoubleSafely()
        }
        val originalResult = originalFormulaResultMap["${totalResultCode}$RETURN_SUFFIX"]?.toDouble() ?: 0.0
        val result = addition + totalValue
        val optimizeResult = optimizeResultProcessor(result)
        logger.info("Addition:$addition, total value:$totalValue")
        val match = abs(result - originalResult) < 10
        logger.info("Result:%.2f, optimize result:%s, original result:%s, match:%s".format(result, optimizeResult, originalResult, match))
        val totalCalculateResultItem = CalculateResultItem(totalResultCode, totalResultCode, value = optimizeResult, codeType = CODE_TYPE_FORMULA)
        calculateResultItemMap[totalResultCode] = totalCalculateResultItem
        calculateResultItemMappingMap[totalResultCode] = totalCalculateResultItem
        if (checkCalculateResultItem && !match) {
            return CalculateResult(false, optimizeInputMap, calculateResultItemMap, calculateResultItemMappingMap, totalCalculateResultItem)
        }
        return CalculateResult(true, optimizeInputMap, calculateResultItemMap, calculateResultItemMappingMap, totalCalculateResultItem)
    }
}
