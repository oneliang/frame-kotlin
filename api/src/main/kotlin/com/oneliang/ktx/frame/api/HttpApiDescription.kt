package com.oneliang.ktx.frame.api

import com.oneliang.ktx.Constants
import com.oneliang.ktx.pojo.KeyValue
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.file.readContentIgnoreLine
import java.io.FileNotFoundException

class HttpApiDescription {
    companion object {
        const val BEGIN = "begin:"
        private const val NAME = "name:"
        private const val KEY = "key:"
        private const val URI = "uri:"
        private const val METHOD = "method:"
        private const val HEADERS = "headers:"
        private const val CONTENT_TYPE = "contentType:"
        private const val REQUEST_PARAMETERS = "requestParameters:"
        private const val RESPONSE_DATAS = "responseDatas:"
        private const val CLASS_IMPORTS = "classImports:"
        private const val FUNCTION_PARAMETER_STRING = "functionParameterString:"
        internal const val TEMPLATE_DESCRIPTION_TYPE_CLASS = "CLASS"
        internal const val FLAG_NAME = 1 shl 0
        internal const val FLAG_KEY = 1 shl 1
        internal const val FLAG_URI = 1 shl 2
        internal const val FLAG_METHOD = 1 shl 3
        internal const val FLAG_HEADERS = 1 shl 4
        internal const val FLAG_CONTENT_TYPE = 1 shl 5
        internal const val FLAG_REQUEST_PARAMETERS = 1 shl 6
        internal const val FLAG_REQUEST_PARAMETERS_SUB_CLASS_1 = 1 shl 7
        internal const val FLAG_REQUEST_PARAMETERS_SUB_CLASS_2 = 1 shl 8
        internal const val FLAG_RESPONSE_DATAS = 1 shl 10
        internal const val FLAG_RESPONSE_DATAS_SUB_CLASS_1 = 1 shl 11
        internal const val FLAG_RESPONSE_DATAS_SUB_CLASS_2 = 1 shl 12
        internal const val FLAG_CLASS_IMPORTS = 1 shl 15
        internal const val FLAG_FUNCTION_PARAMETER_STRING = 1 shl 16
        internal val keywordMap = mapOf(
            NAME to FLAG_NAME,
            KEY to FLAG_KEY,
            URI to FLAG_URI,
            METHOD to FLAG_METHOD,
            HEADERS to FLAG_HEADERS,
            CONTENT_TYPE to FLAG_CONTENT_TYPE,
            REQUEST_PARAMETERS to FLAG_REQUEST_PARAMETERS,
            RESPONSE_DATAS to FLAG_RESPONSE_DATAS,
            CLASS_IMPORTS to FLAG_CLASS_IMPORTS,
            FUNCTION_PARAMETER_STRING to FLAG_FUNCTION_PARAMETER_STRING
        )
    }

    var name = Constants.String.BLANK
    var key = Constants.String.BLANK
    var uri = Constants.String.BLANK
    var method = Constants.Http.RequestMethod.POST.value
    var headers = emptyArray<KeyValue>()
    var contentType = Constants.Http.ContentType.APPLICATION_JSON
    var requestParameters = emptyArray<KeyValueDescription>()
    var responseDatas = emptyArray<KeyValueDescription>()
    var functionParameterString = Constants.String.BLANK

    class KeyValueDescription(
        key: String = Constants.String.BLANK,
        value: String = Constants.String.BLANK,
        var description: String = Constants.String.BLANK
    ) : KeyValue(key, value) {
        var subParameters = emptyArray<KeyValueDescription>()
    }
}

private fun HttpApiDescription.Companion.parseParameter(line: String): HttpApiDescription.KeyValueDescription {
    val parameter = line.split(Constants.String.SPACE)
    val description = line.substring(parameter[0].length + Constants.String.SPACE.length + parameter[1].length).trim()
    return HttpApiDescription.KeyValueDescription(parameter[0], parameter[1], description)
}

private fun HttpApiDescription.Companion.processSubClass(
    line: String,
    currentFlag: Int,
    flagSubClassKeyMap: MutableMap<Int, String>,
    parameterList: MutableList<HttpApiDescription.KeyValueDescription>,
    subClass1: Int,
    subClass2: Int
): Pair<Int, Array<HttpApiDescription.KeyValueDescription>?> {
    val spaceIndex = line.indexOf(Constants.String.SPACE)
    val colonIndex = line.lastIndexOf(Constants.Symbol.COLON)
    //space priority higher than colon
//    val parameterList = currentHttpApiDescription.requestParameters.toMutableList()
    if (spaceIndex < 0 && colonIndex > 0 || colonIndex in 1 until spaceIndex) {//no space but has colon or colon index less than space, has array
        val parameterKey = line.substring(0, colonIndex)
        if (currentFlag and subClass1 == subClass1) {//sub class flag has open, reset sub class flag
            if (parameterKey == flagSubClassKeyMap[subClass1].nullToBlank()) {//the same key, so finished sub class 1
                return currentFlag and subClass1.inv() to null//remove sub class 1 flag
            } else {//2
                if (parameterKey == flagSubClassKeyMap[subClass2].nullToBlank()) {
                    return currentFlag and subClass2.inv() to null//remove sub class 2 flag
                } else {
                    flagSubClassKeyMap[subClass2] = parameterKey
                    val description = line.substring(colonIndex + 1)
                    val lastParameter = parameterList.last()
                    val subParameterList = lastParameter.subParameters.toMutableList()
                    subParameterList += HttpApiDescription.KeyValueDescription(parameterKey, TEMPLATE_DESCRIPTION_TYPE_CLASS, description)
                    lastParameter.subParameters = subParameterList.toTypedArray()
                    return currentFlag or subClass2 to null
                }
            }
        } else {//first in sub class
            flagSubClassKeyMap[subClass1] = parameterKey
            val description = line.substring(colonIndex + 1)
            parameterList += HttpApiDescription.KeyValueDescription(parameterKey, TEMPLATE_DESCRIPTION_TYPE_CLASS, description)
            return currentFlag or subClass1 to parameterList.toTypedArray()
        }
    } else {
        if (currentFlag and subClass2 == subClass2) {//use sub class 2
            if (parameterList.isNotEmpty()) {
                val lastParameter = parameterList.last()
                val subParameterList = lastParameter.subParameters.toMutableList()
                val lastSubParameter = subParameterList.last()
                val subSubParameterList = lastSubParameter.subParameters.toMutableList()
                subSubParameterList += parseParameter(line)
                lastSubParameter.subParameters = subSubParameterList.toTypedArray()
                return 0 to null
            } else {
                error("some invoke sequence error, please check it")
            }
        } else if (currentFlag and subClass1 == subClass1) {//use sub class 1
            if (parameterList.isNotEmpty()) {
                val lastParameter = parameterList.last()
                val subParameterList = lastParameter.subParameters.toMutableList()
                subParameterList += parseParameter(line)
                lastParameter.subParameters = subParameterList.toTypedArray()
                return 0 to null
            } else {
                error("some invoke sequence error, please check it")
            }
        } else {//has space, space priority higher than colon
            parameterList += parseParameter(line)
            return 0 to parameterList.toTypedArray()
        }
    }
}

fun HttpApiDescription.Companion.buildListFromFile(fullFilename: String): Triple<String, Collection<String>, List<HttpApiDescription>> {
    val httpApiDescriptionList = mutableListOf<HttpApiDescription>()
    var httpApiDescription: HttpApiDescription? = null
    val file = fullFilename.toFile()
    val className = file.name.substring(0, file.name.indexOf(Constants.Symbol.DOT))
    val importHashSet = hashSetOf<String>()
    if (file.exists() && file.isFile) {
        var currentFlag = 0
        val flagSubClassKeyMap = mutableMapOf<Int, String>()
        file.readContentIgnoreLine {
            try {
                val line = it.trim()
                if (line.isBlank() || line.startsWith(Constants.Symbol.POUND_KEY, true)) {
                    return@readContentIgnoreLine true//continue
                }
                when {
                    line.startsWith(BEGIN) -> {
                        val newHttpApiDescription = HttpApiDescription()
                        httpApiDescriptionList += newHttpApiDescription
                        httpApiDescription = newHttpApiDescription
                    }
                    else -> {
                        //keyword process
                        var keywordSign = false
                        for (key in keywordMap.keys) {
                            if (line.startsWith(key, true)) {
                                currentFlag = 0//reset
                                currentFlag = currentFlag or keywordMap[key]!!
                                keywordSign = true
                                break
                            }
                        }
                        if (keywordSign) {
                            return@readContentIgnoreLine true
                        }
                        val currentHttpApiDescription = httpApiDescription ?: return@readContentIgnoreLine true
                        when {
                            currentFlag and FLAG_NAME == FLAG_NAME -> {
                                currentHttpApiDescription.name = line
                            }
                            currentFlag and FLAG_KEY == FLAG_KEY -> {
                                currentHttpApiDescription.key = line
                            }
                            currentFlag and FLAG_URI == FLAG_URI -> {
                                currentHttpApiDescription.uri = line
                            }
                            currentFlag and FLAG_METHOD == FLAG_METHOD -> {
                                currentHttpApiDescription.method = line
                            }
                            currentFlag and FLAG_HEADERS == FLAG_HEADERS -> {
                                if (!line.equals(Constants.String.NULL, true)) {
                                    val header = line.split(Constants.String.SPACE)
                                    val headerList = currentHttpApiDescription.headers.toMutableList()
                                    val value = line.substring(header[0].length + Constants.String.SPACE.length).trim()
                                    headerList += KeyValue(header[0], value)
                                    currentHttpApiDescription.headers = headerList.toTypedArray()
                                }
                            }
                            currentFlag and FLAG_CONTENT_TYPE == FLAG_CONTENT_TYPE -> {
                                currentHttpApiDescription.contentType = line
                            }
                            currentFlag and FLAG_REQUEST_PARAMETERS == FLAG_REQUEST_PARAMETERS -> {
                                val parameterList = currentHttpApiDescription.requestParameters.toMutableList()
                                val (modifyCurrentFlag, parameterTypeArray) = processSubClass(
                                    line,
                                    currentFlag,
                                    flagSubClassKeyMap,
                                    parameterList,
                                    FLAG_REQUEST_PARAMETERS_SUB_CLASS_1,
                                    FLAG_REQUEST_PARAMETERS_SUB_CLASS_2
                                )
                                if (modifyCurrentFlag != 0) {
                                    currentFlag = modifyCurrentFlag
                                }
                                if (parameterTypeArray != null) {
                                    currentHttpApiDescription.requestParameters = parameterTypeArray
                                }
                            }
                            currentFlag and FLAG_RESPONSE_DATAS == FLAG_RESPONSE_DATAS -> {
                                val parameterList = currentHttpApiDescription.responseDatas.toMutableList()
                                val (modifyCurrentFlag, parameterTypeArray) = processSubClass(
                                    line,
                                    currentFlag,
                                    flagSubClassKeyMap,
                                    parameterList,
                                    FLAG_RESPONSE_DATAS_SUB_CLASS_1,
                                    FLAG_RESPONSE_DATAS_SUB_CLASS_2,
                                )
                                if (modifyCurrentFlag != 0) {
                                    currentFlag = modifyCurrentFlag
                                }
                                if (parameterTypeArray != null) {
                                    currentHttpApiDescription.responseDatas = parameterTypeArray
                                }
                            }
                            currentFlag and FLAG_CLASS_IMPORTS == FLAG_CLASS_IMPORTS -> {
                                importHashSet += line
                            }
                            currentFlag and FLAG_FUNCTION_PARAMETER_STRING == FLAG_FUNCTION_PARAMETER_STRING -> {
                                currentHttpApiDescription.functionParameterString = Constants.Symbol.COMMA + Constants.String.SPACE + line
                            }
                        }
                    }
                }
                true
            } catch (e: Throwable) {
                error("parse error, line:%s".format(it))
            }
        }
    } else {
        throw FileNotFoundException("file does not exists or file is a directory, file:%s".format(fullFilename))
    }
    return Triple(className, importHashSet, httpApiDescriptionList)
}
