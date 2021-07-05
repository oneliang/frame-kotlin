package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.plugin.Plugin
import com.oneliang.ktx.frame.plugin.PluginFileBean
import com.oneliang.ktx.frame.plugin.PluginGroupBean
import com.oneliang.ktx.util.jar.JarClassLoader
import java.net.URL

class TestPlugin : PluginGroupBean.OnLoadedListener, PluginFileBean.OnLoadedListener {

    companion object {
        const val PLUGIN_GROUP_A = "plugin.group.a"
        const val PLUGIN_FILE_A = "local.jar.file.plugin.a"
        const val PLUGIN_A = "pluginA"
    }

    fun test() {
        val pluginGroupBean = PluginGroupBean(PLUGIN_GROUP_A)
        //plugin a
        val pluginFileBean = PluginFileBean(PLUGIN_FILE_A, url = "D:/plugin-a.jar")
//        pluginFileBean.onLoadedListener = this
//        pluginGroupBean.onLoadedListener = this
        pluginGroupBean.addPluginFileBean(pluginFileBean)
        pluginGroupBean.loadPluginFileBean()
        val pluginA = pluginGroupBean.findPlugin(PLUGIN_GROUP_A, PLUGIN_A)
        pluginA?.dispatch(Plugin.Command("command_a"))
        pluginGroupBean.destroy()
//        pluginFileBean.onLoadedListener = null
//        pluginGroupBean.onLoadedListener = null
    }

    override fun onLoaded(pluginGroupBean: PluginGroupBean) {
        println(pluginGroupBean)
    }

    override fun onLoaded(pluginFileBean: PluginFileBean) {
//        if (pluginFileBean.id == PLUGIN_FILE_A) {
//            val pluginA = pluginFileBean.findPlugin(PLUGIN_A)
//            pluginA?.dispatch(Plugin.Command("command_a"))
//        }
        println("id:" + pluginFileBean.id)
    }

    fun testUnloadClass() {
        var jarClassLoader: JarClassLoader? = JarClassLoader(Thread.currentThread().contextClassLoader)
        jarClassLoader?.addURL(URL("file:///D:/plugin-a.jar"))
        var testClass: Class<*>? = jarClassLoader?.loadClass("com.oneliang.ktx.frame.test.PluginA")
        var instance = testClass?.newInstance()
        println(instance)
        jarClassLoader = null
        testClass = null
        instance = null
        System.gc()
        Thread.sleep(5000)
        jarClassLoader = JarClassLoader(Thread.currentThread().contextClassLoader)
        jarClassLoader.addURL(URL("file:///D:/plugin-a.jar"))
        testClass = jarClassLoader.loadClass("com.oneliang.ktx.frame.test.PluginA")
        instance = testClass?.newInstance()
        println(instance)
        jarClassLoader = null
        testClass = null
        instance = null
    }
}

fun main() {
    TestPlugin().test()
    System.gc()
    Thread.sleep(2000)
//    TestPlugin().testUnloadClass()
//    System.gc()
}