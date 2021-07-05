package com.oneliang.ktx.frame.plugin

import com.oneliang.ktx.frame.broadcast.BroadcastManager
import com.oneliang.ktx.frame.broadcast.BroadcastReceiver
import com.oneliang.ktx.frame.broadcast.Message
import com.oneliang.ktx.util.jar.JarClassLoader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class PluginGroupBean(var id: String) : BroadcastReceiver {

    companion object {
        const val ACTION_PLUGIN_FILE_FINISHED = "action.plugin.file.finished"
        const val KEY_PLUGIN_FILE_ID = "key.plugin.file.id"
    }

    private val pluginFileBeanList: MutableList<PluginFileBean> = CopyOnWriteArrayList()
    private val pluginFileBeanMap: MutableMap<String, PluginFileBean> = ConcurrentHashMap<String, PluginFileBean>()
    var onLoadedListener: OnLoadedListener? = null
    private var jarClassLoader: JarClassLoader = JarClassLoader(Thread.currentThread().contextClassLoader)
    private var defaultPluginDownloader: PluginAsyncHttpDownloader? = PluginAsyncHttpDownloader()
    private var broadcastManager: BroadcastManager? = BroadcastManager()

    init {
        this.broadcastManager?.start()
    }

    fun destroy() {
        this.broadcastManager?.interrupt()
        this.broadcastManager = null
        this.defaultPluginDownloader?.interrupt()
        this.defaultPluginDownloader = null
//        this.jarClassLoader = null
        this.pluginFileBeanList.forEach {
            it.destroy()
        }
        this.pluginFileBeanList.clear()
        this.pluginFileBeanMap.clear()
        this.onLoadedListener = null
    }

    /**
     * load plugin file bean
     */
    fun loadPluginFileBean() {
        this.broadcastManager?.registerBroadcastReceiver(arrayOf(ACTION_PLUGIN_FILE_FINISHED), this)
        this.pluginFileBeanList.forEach {
            it.broadcastManager = this.broadcastManager
            it.jarClassLoader = this.jarClassLoader
            it.pluginDownloader = this.defaultPluginDownloader
            it.loadPluginBean()
        }
    }

    /**
     * receive
     */
    override fun receive(action: String, message: Message) {
        if (action == ACTION_PLUGIN_FILE_FINISHED) {
            var finished = true
            for (pluginFileBean in this.pluginFileBeanList) {
                if (!pluginFileBean.isFinished) {
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
     * add plugin file bean
     * @param pluginFileBean
     */
    fun addPluginFileBean(pluginFileBean: PluginFileBean) {
        if (this.pluginFileBeanMap.containsKey(pluginFileBean.id)) {
            error("plugin file id:(%s) is exists".format(pluginFileBean.id))
        } else {
            this.pluginFileBeanMap[pluginFileBean.id] = pluginFileBean
            this.pluginFileBeanList.add(pluginFileBean)
        }
    }

    /**
     * find plugin
     * @param pluginFileBeanId
     * @param pluginId
     * @return Plugin
     */
    fun findPlugin(pluginFileBeanId: String, pluginId: String): Plugin? {
        return this.pluginFileBeanMap[pluginFileBeanId]?.findPlugin(pluginId)
    }

    /**
     * @author oneliang
     */
    interface OnLoadedListener {
        /**
         * on finished
         * @param pluginGroupBean
         */
        fun onLoaded(pluginGroupBean: PluginGroupBean)
    }
}