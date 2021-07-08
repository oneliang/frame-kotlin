package com.oneliang.ktx.frame.script.engine

import com.oneliang.ktx.Constants
import java.util.concurrent.ConcurrentHashMap

class KotlinFunctionEngine(private val classLoader: ClassLoader? = null) : FunctionEngine {

    private val scriptInvokeNameMap = ConcurrentHashMap<String, Pair<String, String>>()
    private val methodNameMap = ConcurrentHashMap<String, Pair<String, String>>()
    private val classMethodMap = ConcurrentHashMap<String, MutableMap<String, Array<Class<*>>>>()
    override fun eval(script: String) {
        val stringList = script.split(Constants.Symbol.COLON + Constants.Symbol.COLON)
        val (className, methodName) = if (stringList.size == 2) {
            stringList[0] to stringList[1]
        } else {
            error("not support script:(%s), must use this format (\"className::methodName\")".format(script))
        }
        this.classMethodMap.getOrPut(className) {
            val methodMap = mutableMapOf<String, Array<Class<*>>>()
            val clazz = if (this.classLoader == null) {
                Class.forName(className)
            } else {
                this.classLoader.loadClass(className)
            }

            val methods = clazz.declaredMethods
            for (method in methods) {
                val currentMethodName = method.name
                if (methodMap.containsKey(currentMethodName)) {
                    error("not support duplicate method name, class:%s, duplicate method name:%s".format(className, currentMethodName))
                }
                methodMap[currentMethodName] = method.parameterTypes
            }
            methodMap
        }
        this.scriptInvokeNameMap[script] = className to methodName
        this.methodNameMap[methodName] = className to methodName
    }

    override fun invokeFunction(name: String, vararg args: Any?): Any? {
        val (className, methodName) = this.scriptInvokeNameMap[name] ?: this.methodNameMap[name] ?: error("function does not exist, name:%s".format(name))
        val methodMap = this.classMethodMap[className] ?: error("class not found, class name:%s".format(className))
        val parameterTypes = methodMap[methodName] ?: error("method not found, class name:%s, method name:%s".format(className, methodName))
        val method = if (this.classLoader == null) {
            Class.forName(className).getMethod(methodName, *parameterTypes)
        } else {
            this.classLoader.loadClass(className).getMethod(methodName, *parameterTypes)
        }
        return method.invoke(null, *args)
    }
}