package com.oneliang.ktx.frame.script

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.script.engine.FunctionEngineManager
import com.oneliang.ktx.util.common.matches
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.toDoubleSafely
import com.oneliang.ktx.util.common.toKeyListAndMap
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.abs

class FunctionExecutor(
    private val engineName: FunctionEngineManager.EngineName = FunctionEngineManager.EngineName.JS,
    private val classLoader: ClassLoader? = null
) {
    companion object {
        private val logger = LoggerManager.getLogger(FunctionExecutor::class)
        private const val CODE_TYPE_STABLE = "STABLE"
        private const val CODE_TYPE_FUNCTION = "FUNCTION"
        private const val RETURN_SUFFIX = "_RESULT"
    }

    private val functionEngineManager = FunctionEngineManager()
    private val functionEngine = functionEngineManager.getEngineByName(this.engineName, this.classLoader)
    private val allFunctionItemMap = ConcurrentHashMap<String, FunctionItem>()

    //    private val allFunctionItemList = CopyOnWriteArrayList<FunctionItem>()
    private val updateLock = ReentrantLock()

    fun updateFunctionItemListOfEngine(functionItemList: List<FunctionItem>) {
        this.updateLock.lock()
        try {
//            val sortedFunctionItemList = functionItemList.sortedBy { it.order }
            this.allFunctionItemMap.clear()
//            this.allFunctionItemList.addAll(sortedFunctionItemList)
            functionItemList.forEach { functionItem ->
                this.allFunctionItemMap[functionItem.code] = functionItem
                this.functionEngine.eval(functionItem.script)
            }
        } finally {
            this.updateLock.unlock()
        }
    }

    private fun fixCode(code: String, executeTimes: Int): String {
        return if (executeTimes == 0) {
            code
        } else {
            code + Constants.Symbol.UNDERLINE + executeTimes
        }
    }

    fun execute(
        inputMap: Map<String, String>,
        stableValueInputMap: Map<String, String> = emptyMap(),
        totalResultCode: String = Constants.String.BLANK,//no total result
        defaultExecuteFunctionItemCodeList: List<ExecuteFunctionItemCode>,//execute list
        otherConditionExecuteFunctionItemCodeListMap: Map<Map<String, String>, List<ExecuteFunctionItemCode>> = emptyMap(),
        stableValueCodeType: String = CODE_TYPE_STABLE,
        functionResultCode: String = CODE_TYPE_FUNCTION,
        checkFunctionResultItem: Boolean = false,
        originalFunctionResultMap: Map<String, String> = emptyMap(),
        optimizeResultProcessor: (resultValue: Double) -> String = { it.toString() }
    ): FunctionResult {
        val allFunctionItemMap = this.allFunctionItemMap
        if (allFunctionItemMap.isEmpty()) {
            logger.info("Please update function item list first, function item is empty")
        }
        val functionResultItemMap = mutableMapOf<String, FunctionResultItem>()
        //mapping function result without initialize function type
        val functionResultItemMappingMap = mutableMapOf<String, FunctionResultItem>()
        val functionResultList = mutableListOf<String>()
        var stableValue = 0.0//固定值求和
        //stable value
        stableValueInputMap.forEach {
            val returnCode = it.key + RETURN_SUFFIX
            val result = it.value
            if (checkFunctionResultItem) {
                if (!originalFunctionResultMap.containsKey(returnCode)) {
                    logger.error("check stable value input data error, miss returnCode:%s", returnCode)
                    return FunctionResult(false, emptyMap(), functionResultItemMap)
                }
                val originalFunctionResult = originalFunctionResultMap[returnCode]
                if (result != originalFunctionResult) {
                    logger.error("check stable value input data error, returnCode:%s, result:%s, original function result:%s", returnCode, result, originalFunctionResult)
                    return FunctionResult(false, emptyMap(), functionResultItemMap)
                }
            }
            logger.info("Stable:%s, result:%s", returnCode, result)
            stableValue += result.toDoubleSafely()
            val functionResultItem = FunctionResultItem(name = returnCode, code = returnCode, originalCode = returnCode, value = result, codeType = stableValueCodeType)
            functionResultItemMap[returnCode] = functionResultItem
            functionResultItemMappingMap[returnCode] = functionResultItem
        }
        //function input data initialize
        val optimizeInputMap = inputMap.toMutableMap()
        val supportTotalResult = totalResultCode.isNotBlank()
        var functionResultTotalValue = 0.0//函数值求和
        var totalFunctionResultItem = FunctionResultItem()
        var totalResultValue = 0.0
        if (supportTotalResult) {
            //initialize total function result item
            totalFunctionResultItem = FunctionResultItem(name = totalResultCode, code = totalResultCode, originalCode = totalResultCode, value = Constants.String.ZERO, codeType = functionResultCode)
            functionResultItemMap[totalResultCode] = totalFunctionResultItem
            functionResultItemMappingMap[totalResultCode] = totalFunctionResultItem
        }
        if (defaultExecuteFunctionItemCodeList.isEmpty() && otherConditionExecuteFunctionItemCodeListMap.isEmpty()) {
            logger.warning("default function item code mapping and other condition function item code mapping map are empty")
        } else {
            var executeFunctionItemCodeList = defaultExecuteFunctionItemCodeList
            //if matches other condition, replace the function item code mapping
            for ((conditionMap, codeMapping) in otherConditionExecuteFunctionItemCodeListMap) {
                if (inputMap.matches(conditionMap)) {
                    executeFunctionItemCodeList = codeMapping
                    break
                }
            }
            if (executeFunctionItemCodeList.isEmpty()) {
                logger.warning("function item code list is empty")
            } else {
                val (executeCodeList, functionItemCodeMapping) = executeFunctionItemCodeList.toKeyListAndMap({ it.code }, { it.code }, { it.categoryCode })
                val codeExecuteTimes = mutableMapOf<String, Int>()
                //execute function process and result
                executeCodeList.forEach { functionItemCode ->
                    val functionItem = allFunctionItemMap[functionItemCode] ?: error("Function does not exist, please check it, code:$functionItemCode")
                    val executeTimesBeforeExecute = codeExecuteTimes[functionItemCode] ?: 0
                    //update execute times after get
                    codeExecuteTimes[functionItemCode] = executeTimesBeforeExecute + 1
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
                                //fix code only for function result
                                val fixParameterKey = fixCode(parameterKey, executeTimesBeforeExecute)
                                when {
                                    optimizeInputMap.containsKey(parameterKey) -> {
                                        functionInputList.add(optimizeInputMap.getValue(parameterKey))
                                    }
                                    stableValueInputMap.containsKey(parameterKey) -> {
                                        functionInputList.add(stableValueInputMap.getValue(parameterKey))
                                    }
                                    functionResultItemMap.containsKey(fixParameterKey) -> {
                                        val result = functionResultItemMap[fixParameterKey]!!.value
                                        functionInputList.add(result)
                                    }
                                    else -> {
                                        val result = Constants.String.BLANK
                                        optimizeInputMap[parameterKey] = result
                                        functionInputList.add(result)
                                    }
//                                    else -> logger.error("Function:%s error, %s not found", functionItemCode, parameterKey)
                                }
                            }
                            val inputTypeArray = functionInputList.toTypedArray()
                            val inputJson = inputTypeArray.toJson()
                            inputJson to this.functionEngine.invokeFunction(functionItemCode, *(inputTypeArray))
                        }
                    }
                    val (fixValue, resultString) = when (result) {
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
                    logger.info("Function:%s(%s),result:%s, %s", functionItem.name, functionItemCode, resultString, inputJson)
                    val fixInputJson = if (functionItem.parameterType == FunctionItem.ParameterType.JSON_OBJECT) {
                        Constants.String.BLANK
                    } else {
                        inputJson
                    }
                    val originalFunctionReturnCode = functionItem.returnCode
                    val fixFunctionReturnCode = fixCode(functionItem.returnCode, executeTimesBeforeExecute)
                    functionResultItemMap[fixFunctionReturnCode] = FunctionResultItem(name = functionItem.name, code = fixFunctionReturnCode, originalCode = originalFunctionReturnCode, inputJson = fixInputJson, value = fixValue, codeType = functionResultCode)

                    if (functionItemType != FunctionItem.FunctionType.RESULT) {
                        return@forEach//continue, no need to save
                    }

                    //map quote function result
                    if (functionItemCodeMapping.containsKey(functionItemCode)) {
                        val functionReturnCodeMapping = functionItemCodeMapping[functionItemCode].nullToBlank()
                        if (functionReturnCodeMapping.isNotBlank()) {
                            val functionResultValue = fixValue.toDoubleSafely()
                            val functionResultItem = functionResultItemMappingMap.getOrPut(functionReturnCodeMapping) {
                                FunctionResultItem(name = functionReturnCodeMapping, code = functionReturnCodeMapping, originalCode = functionReturnCodeMapping, value = Constants.String.ZERO, codeType = functionResultCode)
                            }
                            val originalFunctionResultItemValue = functionResultItem.value.toDoubleSafely()
                            functionResultItem.value = "%.2f".format(originalFunctionResultItemValue + functionResultValue)
                        }
                    }
                    if (result == null) {
                        logger.error("Error, result is null, did you forget the keyword 'return'")
                        return FunctionResult(false, optimizeInputMap, functionResultItemMap, functionResultItemMappingMap)
                    } else {
                        //double check the if logic, maybe unused
                        if (functionItemType == FunctionItem.FunctionType.RESULT) {
                            functionResultTotalValue += result.toString().toDoubleSafely()
                            //update the total result in process
                            if (supportTotalResult) {
                                totalResultValue = stableValue + functionResultTotalValue
                                val tempTotalFunctionResultItem = functionResultItemMap[totalResultCode] ?: error("it is impossible, maybe logic error, supportTotalResult:%s".format(supportTotalResult))
                                tempTotalFunctionResultItem.value = totalResultValue.toString()
                            }
                            if (checkFunctionResultItem) {
                                if (!originalFunctionResultMap.containsKey(fixFunctionReturnCode)) {
                                    logger.error("Check original function result error, miss functionReturnCode:$fixFunctionReturnCode")
                                    return FunctionResult(false, optimizeInputMap, functionResultItemMap, functionResultItemMappingMap)
                                }
                                val originalFunctionResult = originalFunctionResultMap[fixFunctionReturnCode]
                                if (result.toString() != originalFunctionResult) {
                                    logger.error("Check original function result error, functionReturnCode:$fixFunctionReturnCode, (result)/(originalFunctionResult):($result)/($originalFunctionResult)")
//                                return FunctionResult(false, functionResultItemMap, functionResultItemMappingMap)
                                }
                            }
                        } else {
                            //maybe no else
                        }
                    }
                }
            }
        }
        if (!supportTotalResult) {//no total result
            return FunctionResult(true, optimizeInputMap, functionResultItemMap, functionResultItemMappingMap)
        } else {//replace again, through it is right
            totalResultValue = stableValue + functionResultTotalValue
        }
        val originalResultValue = originalFunctionResultMap["${totalResultCode}$RETURN_SUFFIX"]?.toDouble() ?: 0.0
        val fixTotalResultValue = if (totalResultValue.isNaN()) 0.0 else totalResultValue
        val optimizeResult = optimizeResultProcessor(fixTotalResultValue)
        logger.info("Stable value:%s, function result total value:%s", stableValue, functionResultTotalValue)
        if (supportTotalResult) {//update total function result item value
            val tempTotalFunctionResultItem = functionResultItemMap[totalResultCode] ?: error("it is impossible, maybe logic error, supportTotalResult:%s".format(supportTotalResult))
            tempTotalFunctionResultItem.value = optimizeResult
        }
        val match = abs(fixTotalResultValue - originalResultValue) < 10
        logger.info("Total result value:%.2f, fix total result value:%s, optimize result:%s, original result:%s, match:%s".format(totalResultValue, fixTotalResultValue, optimizeResult, originalResultValue, match))
        if (checkFunctionResultItem && !match) {
            return FunctionResult(false, optimizeInputMap, functionResultItemMap, functionResultItemMappingMap, totalFunctionResultItem)
        }
        return FunctionResult(true, optimizeInputMap, functionResultItemMap, functionResultItemMappingMap, totalFunctionResultItem)
    }
}
