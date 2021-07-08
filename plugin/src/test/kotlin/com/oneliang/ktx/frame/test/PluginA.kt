package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.plugin.Plugin

class PluginA(override val id: String = "TestPlugin") : Plugin {
    override fun initialize() {
        println("initialize")
        val otherClass = OtherClass()
        println(otherClass)
    }

    override fun dispatch(command: Plugin.Command) {
        println("dispatch")
    }

    override fun publicAction(): Array<String> {
        println("publicAction")
        return emptyArray()
    }

    override fun destroy() {
        println("destroy")
    }
}