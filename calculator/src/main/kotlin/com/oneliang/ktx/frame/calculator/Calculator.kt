package com.oneliang.ktx.frame.calculator

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.toDoubleSafely
import com.oneliang.ktx.util.common.toFloatSafely
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap
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

    fun updateFormulaItemListOfEngine(formulaItemList: List<FormulaItem>) {
        val sortedFormulaItemList = formulaItemList.sortedBy { it.order }
        allFormulaItemMap[code] = sortedFormulaItemList
        sortedFormulaItemList.forEach { formulaItem ->
            engine.eval(formulaItem.javaScriptFunction)
        }
    }

    fun calculate(inputMap: Map<String, String>,
                  additionInputMap: Map<String, String>,
                  initializeFormulaTypeCode: String,
                  resultFormulaTypeCode: String,
                  resultCode: String,
                  calculateResultItemMapping: Map<String, String> = emptyMap(),
                  checkCalculateResultItem: Boolean = false,
                  originalFormulaResultMap: Map<String, String> = emptyMap(),
                  optimizeResultProcessor: (result: Double) -> String = { it.toString() }): CalculateResult {
        if (!allFormulaItemMap.containsKey(code)) {
            logger.error("Please update formula item list first, map not contains cache data, product type code:%s", code)
            return CalculateResult(false, emptyMap())
        }
        val allFormulaItemList = allFormulaItemMap[code] ?: emptyList()
        if (allFormulaItemList.isEmpty()) {
            logger.info("Please update formula item list first, formula item is empty, product type code:%s", code)
        }
        if (engine !is Invocable) {
            logger.error("engine is not invocable")
            return CalculateResult(false, emptyMap())
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
                    return CalculateResult(false, calculateResultItemMap)
                }
                val originalFormulaResult = originalFormulaResultMap[returnCode]
                if (result != originalFormulaResult) {
                    logger.error("check addition input data error, returnCode:$returnCode, result:$result, original formula result:$originalFormulaResult")
                    return CalculateResult(false, calculateResultItemMap)
                }
            }
            logger.info("Addition:$returnCode, result:$result")
            addition += result.toFloatSafely()
            val calculateResultItem = CalculateResultItem(returnCode, returnCode, value = result, codeType = CODE_TYPE_ADDITION)
            calculateResultItemMap[returnCode] = calculateResultItem
            calculateResultItemMappingMap[returnCode] = calculateResultItem
        }
        //formula input data initialize
        val optimizeInputDataMap = mutableMapOf<String, String>()
        inputMap.forEach { (key, value) ->
            optimizeInputDataMap[key] = value
        }

        if (calculateResultItemMapping.isEmpty()) {
            logger.info("calculate result item mapping is empty, code:%s", code)
        } else {
            //execute initialize formula
            allFormulaItemList.forEach { formulaItem ->
                if (formulaItem.formulaTypeCode != initializeFormulaTypeCode) {
                    return@forEach //continue
                }
                val formulaItemCode = formulaItem.code
                val inputJson = inputMap.toJson()
                val result = engine.invokeFunction(formulaItemCode, inputJson)
//            val formulaReturnCode = quoteFormulaItem.returnCode
                val resultJson = when (result) {
                    null -> Constants.String.NULL
                    is Bindings -> {
                        result.forEach { (key, value) ->
                            optimizeInputDataMap[key] = value?.toString().nullToBlank()
                        }
                        result.toJson()
                    }
                    else -> {
                        logger.error("result is not javax.script.Bindings")
                        Constants.String.NULL
                    }
                }
                logger.info("Formula:%s(%s),result:%s, %s", formulaItem.name, formulaItemCode, resultJson, inputJson)
//            resultMap[formulaReturnCode] = CalculateResult(productTypeFormula.name, formulaReturnCode, inputJson = inputJson, value = resultJson)
                //no need to save initialize data
                if (result == null || result !is Bindings) {
                    logger.error("Error, result is null or result is not javax.script.Bindings, initialize formula execute error")
                    return CalculateResult(false, calculateResultItemMap)
                }
            }

            //formula process and result
            allFormulaItemList.forEach { formulaItem ->
                val formulaItemTypeCode = formulaItem.formulaTypeCode
                if (formulaItemTypeCode == initializeFormulaTypeCode) {
                    return@forEach
                }
                val formulaItemCode = formulaItem.code
                if (formulaItemTypeCode == resultFormulaTypeCode && !calculateResultItemMapping.containsKey(formulaItemCode)) {
                    logger.verbose("No need to execute formula, code:%s", formulaItemCode)
                    return@forEach
                }
                val formulaInputList = mutableListOf<String>()
                formulaItem.parameterList.forEach parameterList@{
                    val parameterKey = it.trim()
                    if (parameterKey.isBlank()) {
                        logger.error("Formula:%s has blank parameter, please check and confirm it", formulaItemCode)
                        return@parameterList
                    }
                    when {
                        optimizeInputDataMap.containsKey(parameterKey) -> {
                            formulaInputList.add(optimizeInputDataMap.getValue(parameterKey))
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
                val result = engine.invokeFunction(formulaItemCode, *(inputTypeArray))
                logger.info("Formula:%s(%s),result:%s, %s", formulaItem.name, formulaItemCode, result, inputJson)

                val formulaReturnCode = formulaItem.returnCode
                calculateResultItemMap[formulaReturnCode] = CalculateResultItem(formulaItem.name, formulaReturnCode, inputJson = inputJson, value = result?.toString()
                        ?: Constants.String.NULL, codeType = CODE_TYPE_FORMULA)
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
                    return CalculateResult(false, calculateResultItemMap, calculateResultItemMappingMap)
                } else {
                    if (formulaItemTypeCode == resultFormulaTypeCode) {
                        resultList += result.toString()
                        if (checkCalculateResultItem) {
                            if (!originalFormulaResultMap.containsKey(formulaReturnCode)) {
                                logger.error("Check original formula result error, miss formulaReturnCode:$formulaReturnCode")
                                return CalculateResult(false, calculateResultItemMap, calculateResultItemMappingMap)
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


        var totalValue = 0.0
        resultList.forEach {
            totalValue += it.toDoubleSafely()
        }
        val originalResult = originalFormulaResultMap["${resultCode}$RETURN_SUFFIX"]?.toDouble() ?: 0.0
        val result = addition + totalValue
        val optimizeResult = optimizeResultProcessor(result)
        logger.info("Addition:$addition, total value:$totalValue")
        val match = abs(result - originalResult) < 10
        logger.info("Result:%.2f, optimize result:%s, original result:%s, match:%s".format(result, optimizeResult, originalResult, match))
        val totalCalculateResultItem = CalculateResultItem(resultCode, resultCode, value = optimizeResult, codeType = CODE_TYPE_FORMULA)
        calculateResultItemMap[resultCode] = totalCalculateResultItem
        calculateResultItemMappingMap[resultCode] = totalCalculateResultItem
        if (checkCalculateResultItem && !match) {
            return CalculateResult(false, calculateResultItemMap, calculateResultItemMappingMap, totalCalculateResultItem)
        }
        return CalculateResult(true, calculateResultItemMap, calculateResultItemMappingMap, totalCalculateResultItem)
    }
}
