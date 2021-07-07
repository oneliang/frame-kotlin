package com.oneliang.ktx.frame.script.engine

import com.oneliang.ktx.Constants
import java.util.concurrent.ConcurrentHashMap

class KotlinFunctionEngine : FunctionEngine {

    private val scriptInvokeNameMap = ConcurrentHashMap<String, Pair<String, String>>()
    private val methodNameMap = ConcurrentHashMap<String, Pair<String, String>>()
    override fun eval(script: String) {
        val stringList = script.split(Constants.Symbol.COLON + Constants.Symbol.COLON)
        val (className, methodName) = if (stringList.size == 2) {
            stringList[0] to stringList[1]
        } else {
            error("not support script:(%s), must use this format (\"className::methodName\")".format(script))
        }
        this.scriptInvokeNameMap[script] = className to methodName
        this.methodNameMap[methodName] = className to methodName
    }

    override fun invokeFunction(name: String, vararg args: Any?): Any? {
        val (className, methodName) = this.scriptInvokeNameMap[name] ?: this.methodNameMap[name] ?: error("function does not exist, name:%s".format(name))
        val method = Class.forName(className).getMethod(methodName)
        return method.invoke(null, *args)
    }
}