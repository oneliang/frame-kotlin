package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.plugin.Plugin
import com.oneliang.ktx.frame.plugin.PluginFile
import com.oneliang.ktx.frame.plugin.PluginGroup
import com.oneliang.ktx.util.jar.JarClassLoader
import java.net.URL

class TestPlugin : PluginGroup.OnLoadedListener, PluginFile.OnLoadedListener {

    companion object {
        const val PLUGIN_GROUP_A = "plugin.group.a"
        const val PLUGIN_FILE_A = "local.jar.file.plugin.a"
        const val PLUGIN_A = "TestPlugin"
    }

    private var pluginGroup: PluginGroup? = null

    fun test() {
        if (this.pluginGroup == null) {
            this.pluginGroup = PluginGroup(PLUGIN_GROUP_A)
            this.pluginGroup?.initialize()
        }
        //plugin a
        var pluginFile = PluginFile(PLUGIN_FILE_A, url = "D:/a.jar")
//        pluginFile.onLoadedListener = this
//        pluginGroup.onLoadedListener = this
        this.pluginGroup?.addOrUpdatePluginFile(pluginFile)
        this.pluginGroup?.loadPluginFile()
        var pluginA = this.pluginGroup?.findPlugin(PLUGIN_GROUP_A, PLUGIN_A)
        pluginA?.dispatch(Plugin.Command("command_a"))
//        this.pluginGroup?.destroy()
//        System.gc()
//        this.pluginGroup = null
//        pluginFile.onLoadedListener = null
//        pluginGroup.onLoadedListener = null


//        pluginFile = PluginFile(PLUGIN_FILE_A, url = "D:/b/libraries-formula-plugin-base.jar")
//        this.pluginGroup?.addOrUpdatePluginFile(pluginFile)
//        System.gc()
//        this.pluginGroup?.loadPluginFile()
//        pluginA = this.pluginGroup?.findPlugin(PLUGIN_GROUP_A, PLUGIN_A)
//        pluginA?.dispatch(Plugin.Command("command_b"))
//        this.pluginGroup?.destroy()
//        System.gc()
    }

    override fun onLoaded(pluginGroup: PluginGroup) {
        println(pluginGroup)
    }

    override fun onLoaded(pluginFile: PluginFile) {
//        if (pluginFile.id == PLUGIN_FILE_A) {
//            val pluginA = pluginFile.findPlugin(PLUGIN_A)
//            pluginA?.dispatch(Plugin.Command("command_a"))
//        }
        println("id:" + pluginFile.id)
    }

    fun testUnloadClass() {
        var jarClassLoader: JarClassLoader? = JarClassLoader(Thread.currentThread().contextClassLoader)
        jarClassLoader?.addURL(URL("file:///D:/plugin-a.jar"))
        val classList = mutableListOf<Class<*>?>()
        var testClass: Class<*>? = jarClassLoader?.loadClass("com.oneliang.ktx.frame.test.PluginA")
        classList += testClass
        var instance = testClass?.newInstance()
        println(instance)
        jarClassLoader = null
        testClass = null
        instance = null
        classList.clear()
        System.gc()
//        println(classList.size)
//        Thread.sleep(5000)
//        jarClassLoader = JarClassLoader(Thread.currentThread().contextClassLoader)
//        jarClassLoader.addURL(URL("file:///D:/plugin-a.jar"))
//        testClass = jarClassLoader.loadClass("com.oneliang.ktx.frame.test.PluginA")
//        instance = testClass?.newInstance()
//        println(instance)
//        jarClassLoader = null
//        testClass = null
//        instance = null
    }
}

fun main() {
    var testPlugin: TestPlugin? = TestPlugin()
    testPlugin?.test()
//    testPlugin = null
    System.gc()
//    Thread.sleep(2000)
//    val fileDetector = FileDetector("D:/b", ".jar")
//    fileDetector.start()
//    fileDetector.detectProcessor = object : FileDetector.DetectProcessor {
//        override fun afterUpdateFileProcess(filePath: String) {
//            println(filePath)
//        }
//    }
//    TestPlugin().testUnloadClass()
//    System.gc()
}