package com.oneliang.ktx.frame.script

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.script.engine.FunctionEngineManager
import com.oneliang.ktx.util.common.matches
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.toDoubleSafely
import com.oneliang.ktx.util.common.toFloatSafely
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.abs

class FunctionExecutor(
    private val code: String,
    private val engineName: FunctionEngineManager.EngineName = FunctionEngineManager.EngineName.JS
) {
    companion object {
        private val logger = LoggerManager.getLogger(FunctionExecutor::class)
        private const val CODE_TYPE_STABLE = "STABLE"
        private const val CODE_TYPE_FUNCTION = "FUNCTION"
        private const val RETURN_SUFFIX = "_RESULT"
    }

    private val functionEngineManager = FunctionEngineManager()
    private val functionEngine = functionEngineManager.getEngineByName(this.engineName)
    private val allFunctionItemMap = ConcurrentHashMap<String, List<FunctionItem>>()
    private val updateLock = ReentrantLock()

    fun updateFunctionItemListOfEngine(functionItemList: List<FunctionItem>) {
        this.updateLock.lock()
        try {
            val sortedFunctionItemList = functionItemList.sortedBy { it.order }
            this.allFunctionItemMap[this.code] = sortedFunctionItemList
            sortedFunctionItemList.forEach { functionItem ->
                this.functionEngine.eval(functionItem.javaScriptFunction)
            }
        } finally {
            this.updateLock.unlock()
        }
    }

    fun execute(
        inputMap: Map<String, String>,
        stableValueInputMap: Map<String, String> = emptyMap(),
        totalResultCode: String = Constants.String.BLANK,//no total result
        defaultFunctionItemCodeMapping: Map<String, String>,
        otherConditionFunctionItemCodeMappingMap: Map<Map<String, String>, Map<String, String>> = emptyMap(),
        stableValueCodeType: String = CODE_TYPE_STABLE,
        functionResultCode: String = CODE_TYPE_FUNCTION,
        checkFunctionResultItem: Boolean = false,
        originalFunctionResultMap: Map<String, String> = emptyMap(),
        optimizeResultProcessor: (result: Double) -> String = { it.toString() }
    ): FunctionResult {
        if (!allFunctionItemMap.containsKey(code)) {
            logger.error("Please update function item list first, map not contains cache data, product type code:%s", code)
            return FunctionResult(false, emptyMap(), emptyMap())
        }
        val allFunctionItemList = allFunctionItemMap[code] ?: emptyList()
        if (allFunctionItemList.isEmpty()) {
            logger.info("Please update function item list first, function item is empty, product type code:%s", code)
        }
//        if (this.functionEngine !is Invocable) {
//            logger.error("engine is not invocable")
//            return FunctionResult(false, emptyMap(), emptyMap())
//        }
        val functionResultItemMap = mutableMapOf<String, FunctionResultItem>()
        //mapping function result without initialize function type
        val functionResultItemMappingMap = mutableMapOf<String, FunctionResultItem>()
        val resultList = mutableListOf<String>()
        var stableValue = 0F//固定值求和
        //stable value
        stableValueInputMap.forEach {
            val returnCode = it.key + RETURN_SUFFIX
            val result = it.value
            if (checkFunctionResultItem) {
                if (!originalFunctionResultMap.containsKey(returnCode)) {
                    logger.error("check stable value input data error, miss returnCode:$returnCode")
                    return FunctionResult(false, emptyMap(), functionResultItemMap)
                }
                val originalFunctionResult = originalFunctionResultMap[returnCode]
                if (result != originalFunctionResult) {
                    logger.error("check stable value input data error, returnCode:$returnCode, result:$result, original function result:$originalFunctionResult")
                    return FunctionResult(false, emptyMap(), functionResultItemMap)
                }
            }
            logger.info("Stable:%s, result:%s", returnCode, result)
            stableValue += result.toFloatSafely()
            val functionResultItem = FunctionResultItem(returnCode, returnCode, value = result, codeType = stableValueCodeType)
            functionResultItemMap[returnCode] = functionResultItem
            functionResultItemMappingMap[returnCode] = functionResultItem
        }
        //function input data initialize
        val optimizeInputMap = inputMap.toMutableMap()

        if (defaultFunctionItemCodeMapping.isEmpty() && otherConditionFunctionItemCodeMappingMap.isEmpty()) {
            logger.warning("default function item code mapping and other condition function item code mapping map are empty, code:%s", code)
        } else {
            var functionItemCodeMapping = defaultFunctionItemCodeMapping
            //if matches other condition, replace the function item code mapping
            for ((conditionMap, codeMapping) in otherConditionFunctionItemCodeMappingMap) {
                if (inputMap.matches(conditionMap)) {
                    functionItemCodeMapping = codeMapping
                    break
                }
            }
            if (functionItemCodeMapping.isEmpty()) {
                logger.warning("function item code mapping is empty, code:%s", code)
            } else {
                //function process and result
                allFunctionItemList.forEach { functionItem ->
                    val functionItemCode = functionItem.code
                    if (!functionItemCodeMapping.containsKey(functionItemCode)) {
                        logger.verbose("No need to execute function, code:%s", functionItemCode)
                        return@forEach
                    }
                    val functionItemType = functionItem.functionType
                    val (inputJson, result) = when (functionItem.parameterType) {
                        FunctionItem.ParameterType.JSON_OBJECT -> {
                            val inputJson = optimizeInputMap.toJson()
                            inputJson to this.functionEngine.invokeFunction(functionItemCode, inputJson)
                        }
                        FunctionItem.ParameterType.STRING -> {
                            val functionInputList = mutableListOf<String>()
                            val parameterList = functionItem.parameter.split(Constants.Symbol.COMMA)
                            parameterList.forEach parameterList@{
                                val parameterKey = it.trim()
                                if (parameterKey.isBlank()) {
                                    logger.error("Function:%s has blank parameter, please check and confirm it", functionItemCode)
                                    return@parameterList
                                }
                                when {
                                    optimizeInputMap.containsKey(parameterKey) -> {
                                        functionInputList.add(optimizeInputMap.getValue(parameterKey))
                                    }
                                    stableValueInputMap.containsKey(parameterKey) -> {
                                        functionInputList.add(stableValueInputMap.getValue(parameterKey))
                                    }
                                    functionResultItemMap.containsKey(parameterKey) -> {
                                        val result = functionResultItemMap[parameterKey]!!.value
                                        functionInputList.add(result)
                                    }
                                    else -> logger.error("Function:%s error, %s not found", functionItemCode, parameterKey)
                                }
                            }
                            val inputTypeArray = functionInputList.toTypedArray()
                            val inputJson = inputTypeArray.toJson()
                            inputJson to this.functionEngine.invokeFunction(functionItemCode, *(inputTypeArray))
                        }
                    }
                    val (fixValue, resultJson) = when (result) {
                        null -> Constants.String.NULL to Constants.String.NULL
                        is Map<*, *> -> {
                            result.forEach { (key, value) ->
                                optimizeInputMap[key?.toString().nullToBlank()] = value?.toString().nullToBlank()
                            }
                            Constants.String.BLANK to result.toJson()
                        }
                        else -> {
                            val value = result.toString()
                            value to value
                        }
                    }
                    logger.info("Function:%s(%s),result:%s, %s", functionItem.name, functionItemCode, resultJson, inputJson)
                    val fixInputJson = if (functionItem.parameterType == FunctionItem.ParameterType.JSON_OBJECT) {
                        Constants.String.BLANK
                    } else {
                        inputJson
                    }
                    val functionReturnCode = functionItem.returnCode
                    functionResultItemMap[functionReturnCode] = FunctionResultItem(functionItem.name, functionReturnCode, inputJson = fixInputJson, value = fixValue, codeType = functionResultCode)

                    if (functionItem.functionType != FunctionItem.FunctionType.RESULT) {
                        return@forEach//continue, no need to save
                    }

                    //map quote function result
                    if (functionItemCodeMapping.containsKey(functionItemCode)) {
                        val functionReturnCodeMapping = functionItemCodeMapping[functionItemCode].nullToBlank()
                        if (functionReturnCodeMapping.isNotBlank()) {
                            val functionResultValue = fixValue.toDoubleSafely()
                            val functionResultItem = functionResultItemMappingMap.getOrPut(functionReturnCodeMapping) { FunctionResultItem(functionReturnCodeMapping, functionReturnCodeMapping, Constants.String.ZERO, codeType = functionResultCode) }
                            val originalFunctionResultItemValue = functionResultItem.value.toDoubleSafely()
                            functionResultItem.value = "%.2f".format(originalFunctionResultItemValue + functionResultValue)
                        }
                    }
                    if (result == null) {
                        logger.error("Error, result is null, did you forget the keyword 'return'")
                        return FunctionResult(false, optimizeInputMap, functionResultItemMap, functionResultItemMappingMap)
                    } else {
                        if (functionItemType == FunctionItem.FunctionType.RESULT) {
                            resultList += result.toString()
                            if (checkFunctionResultItem) {
                                if (!originalFunctionResultMap.containsKey(functionReturnCode)) {
                                    logger.error("Check original function result error, miss functionReturnCode:$functionReturnCode")
                                    return FunctionResult(false, optimizeInputMap, functionResultItemMap, functionResultItemMappingMap)
                                }
                                val originalFunctionResult = originalFunctionResultMap[functionReturnCode]
                                if (result.toString() != originalFunctionResult) {
                                    logger.error("Check original function result error, functionReturnCode:$functionReturnCode, (result)/(originalFunctionResult):($result)/($originalFunctionResult)")
//                                return FunctionResult(false, functionResultItemMap, functionResultItemMappingMap)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (totalResultCode.isBlank()) {//no total result
            return FunctionResult(true, optimizeInputMap, functionResultItemMap, functionResultItemMappingMap)
        }
        //has total result
        var totalValue = 0.0
        resultList.forEach {
            totalValue += it.toDoubleSafely()
        }
        val originalResult = originalFunctionResultMap["${totalResultCode}$RETURN_SUFFIX"]?.toDouble() ?: 0.0
        val result = stableValue + totalValue
        val fixResult = if (result.isNaN()) 0.0 else result
        val optimizeResult = optimizeResultProcessor(fixResult)
        logger.info("Stable value:%s, total value:", stableValue, totalValue)
        val match = abs(fixResult - originalResult) < 10
        logger.info("Result:%.2f, fix result:%s, optimize result:%s, original result:%s, match:%s".format(result, fixResult, optimizeResult, originalResult, match))
        val totalFunctionResultItem = FunctionResultItem(totalResultCode, totalResultCode, value = optimizeResult, codeType = functionResultCode)
        functionResultItemMap[totalResultCode] = totalFunctionResultItem
        functionResultItemMappingMap[totalResultCode] = totalFunctionResultItem
        if (checkFunctionResultItem && !match) {
            return FunctionResult(false, optimizeInputMap, functionResultItemMap, functionResultItemMappingMap, totalFunctionResultItem)
        }
        return FunctionResult(true, optimizeInputMap, functionResultItemMap, functionResultItemMappingMap, totalFunctionResultItem)
    }
}
