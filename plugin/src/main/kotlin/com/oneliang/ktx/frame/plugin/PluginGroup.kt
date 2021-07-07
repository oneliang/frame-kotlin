package com.oneliang.ktx.frame.plugin

import com.oneliang.ktx.frame.broadcast.BroadcastManager
import com.oneliang.ktx.frame.broadcast.BroadcastReceiver
import com.oneliang.ktx.frame.broadcast.Message
import com.oneliang.ktx.util.jar.JarClassLoader
import java.util.concurrent.ConcurrentHashMap

class PluginGroup(var id: String) : BroadcastReceiver {

    companion object {
        const val ACTION_PLUGIN_FILE_FINISHED = "action.plugin.file.finished"
        const val KEY_PLUGIN_FILE_ID = "key.plugin.file.id"
    }

    private val pluginFileMap: MutableMap<String, PluginFile> = ConcurrentHashMap<String, PluginFile>()
    var onLoadedListener: OnLoadedListener? = null
    private var jarClassLoader: JarClassLoader? = null
    private var defaultPluginDownloader: PluginAsyncHttpDownloader? = null
    private var broadcastManager: BroadcastManager? = null

    @Synchronized
    fun initialize() {
        this.jarClassLoader = JarClassLoader(Thread.currentThread().contextClassLoader)
        this.defaultPluginDownloader = PluginAsyncHttpDownloader()
        this.broadcastManager = BroadcastManager()
        this.defaultPluginDownloader?.start()
        this.broadcastManager?.start()
    }

    @Synchronized
    fun destroy() {
        this.broadcastManager?.interrupt()
        this.broadcastManager = null
        this.defaultPluginDownloader?.interrupt()
        this.defaultPluginDownloader = null
        this.jarClassLoader = null
        this.pluginFileMap.forEach { (_, pluginFile) ->
            pluginFile.destroy()
        }
        this.pluginFileMap.clear()
        this.onLoadedListener = null
    }

    /**
     * load plugin file
     */
    fun loadPluginFile() {
        this.broadcastManager?.registerBroadcastReceiver(arrayOf(ACTION_PLUGIN_FILE_FINISHED), this)
        this.pluginFileMap.forEach { (_, pluginFile) ->
            pluginFile.loadPlugin()
        }
    }

    /**
     * receive
     */
    override fun receive(action: String, message: Message) {
        if (action == ACTION_PLUGIN_FILE_FINISHED) {
            var finished = true
            for ((_, pluginFile) in this.pluginFileMap) {
                if (!pluginFile.isFinished) {
                    finished = false
                    break
                }
            }
            if (finished) {
                this.broadcastManager?.unregisterBroadcastReceiver(this)
                this.onLoadedListener?.onLoaded(this)
            }
        }
    }

    /**
     * add or update plugin file
     * @param pluginFile
     */
    fun addOrUpdatePluginFile(pluginFile: PluginFile) {
        pluginFile.broadcastManager = this.broadcastManager
        pluginFile.jarClassLoader = this.jarClassLoader
        pluginFile.pluginDownloader = this.defaultPluginDownloader
        if (this.pluginFileMap.containsKey(pluginFile.id)) {
            this.pluginFileMap[pluginFile.id]?.destroy()
            this.pluginFileMap[pluginFile.id] = pluginFile
        } else {
            this.pluginFileMap[pluginFile.id] = pluginFile
        }
    }

    /**
     * find plugin
     * @param pluginFileId
     * @param pluginId
     * @return Plugin
     */
    fun findPlugin(pluginFileId: String, pluginId: String): Plugin? {
        return this.pluginFileMap[pluginFileId]?.findPlugin(pluginId)
    }

    /**
     * get jar class loader
     * @return JarClassLoader may be null
     */
    fun getJarClassLoader(): JarClassLoader? {
        return this.jarClassLoader
    }

    /**
     * @author oneliang
     */
    interface OnLoadedListener {
        /**
         * on finished
         * @param pluginGroup
         */
        fun onLoaded(pluginGroup: PluginGroup)
    }
}