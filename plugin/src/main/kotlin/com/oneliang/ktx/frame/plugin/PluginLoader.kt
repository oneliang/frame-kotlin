package com.oneliang.ktx.frame.plugin

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.file.FileDetector

class PluginLoader(private val directory: String) : FileDetector.DetectProcessor {

    companion object {
        private const val FILE_SUFFIX = Constants.Symbol.DOT + Constants.File.JAR
    }

    private val pluginGroupId = this.hashCode().toString()
    private val fileDetector = FileDetector(this.directory, FILE_SUFFIX)
    private var pluginGroup: PluginGroup? = null

    init {
        this.fileDetector.detectProcessor = this
        this.fileDetector.start()
    }

    override fun afterUpdateFileProcess(filePath: String) {
        this.pluginGroup?.destroy()
        this.pluginGroup = null
        System.gc()
        this.pluginGroup = PluginGroup(this.pluginGroupId)
        this.pluginGroup?.initialize()
        val pluginFile = PluginFile(filePath.toFile().name, url = filePath)
        this.pluginGroup?.addOrUpdatePluginFile(pluginFile)
        this.pluginGroup?.loadPluginFile()
//        println("update:$filePath")
    }

    fun findPlugin(filename: String, pluginId: String): Plugin? {
        return this.pluginGroup?.findPlugin(filename, pluginId)
    }
}

fun main() {
    val pluginLoader = PluginLoader("D:/b")
}