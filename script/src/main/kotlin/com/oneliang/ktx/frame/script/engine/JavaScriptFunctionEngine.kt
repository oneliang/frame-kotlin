package com.oneliang.ktx.frame.script.engine

import javax.script.Invocable
import javax.script.ScriptEngineManager

class JavaScriptFunctionEngine : FunctionEngine {

    private val scriptEngineManager = ScriptEngineManager()
    private val engine = scriptEngineManager.getEngineByName("js")
    override fun eval(script: String) {
        this.engine.eval(script)
    }

    override fun invokeFunction(name: String, vararg args: Any?): Any? {
        if (this.engine !is Invocable) {
            return null
        }
        return this.engine.invokeFunction(name, *args)
    }
}