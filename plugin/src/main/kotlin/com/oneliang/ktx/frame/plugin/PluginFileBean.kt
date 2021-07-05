package com.oneliang.ktx.frame.plugin

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.broadcast.BroadcastManager
import com.oneliang.ktx.frame.broadcast.Message
import com.oneliang.ktx.util.common.isInterfaceImplement
import com.oneliang.ktx.util.jar.JarClassLoader
import com.oneliang.ktx.util.jar.JarUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class PluginFileBean(val id: String, val type: Type = Type.JAR, val source: Source = Source.LOCAL, val url: String = Constants.String.BLANK) {

    companion object {
        private val logger = LoggerManager.getLogger(PluginFileBean::class)
    }

    enum class Type(val value: Int) {
        SOURCE_CODE(0), JAR(1)
    }

    enum class Source(val value: Int) {
        LOCAL(0), HTTP(1), OTHER(2)
    }

    private val pluginBeanMap: MutableMap<String, PluginBean> = ConcurrentHashMap<String, PluginBean>()
    internal var broadcastManager: BroadcastManager? = null
    internal lateinit var jarClassLoader: JarClassLoader

    var saveFullFilename: String = Constants.String.BLANK
    var onLoadedListener: OnLoadedListener? = null
    var pluginDownloader: PluginDownloader? = null
    var isFinished = false
        set(finished) {
            field = finished
            if (isFinished) {
                val message = Message()
                message.actionList += PluginGroupBean.ACTION_PLUGIN_FILE_FINISHED
                message.putObject(PluginGroupBean.KEY_PLUGIN_FILE_ID, id)
//                if (this::broadcastManager.isInitialized) {
                this.broadcastManager?.sendBroadcast(message)
//                }
            }
        }

    fun interrupt() {
        this.broadcastManager = null
        this.pluginBeanMap.forEach { (_, pluginBean) ->
            pluginBean.pluginInstance = null
        }
        this.pluginBeanMap.clear()
        this.onLoadedListener = null
        this.pluginDownloader = null
    }

    /**
     * find plugin
     * @param pluginId
     * @return Plugin
     */
    fun findPlugin(pluginId: String): Plugin? {
        var plugin: Plugin? = null
        if (this.pluginBeanMap.containsKey(pluginId)) {
            val pluginBean = this.pluginBeanMap[pluginId]
            if (pluginBean != null) {
                plugin = pluginBean.pluginInstance
            }
        }
        return plugin
    }

    /**
     * load plugin bean
     */
    fun loadPluginBean() {
        when (this.type) {
            Type.SOURCE_CODE -> loadPluginBeanByCode()
            Type.JAR -> loadPluginBeanByJar()
        }
    }

    /**
     * load plugin bean by code
     */
    private fun loadPluginBeanByCode() {
        this.pluginBeanMap.forEach { (_, pluginBean) ->
            val plugin = pluginBean.pluginInstance
            plugin?.initialize()
        }
        this.isFinished = true
//        if (this::onLoadedListener.isInitialized) {
        this.onLoadedListener?.onLoaded(this)
//        }
    }

    /**
     * load plugin bean by jar
     */
    private fun loadPluginBeanByJar() {
        when (this.source) {
            Source.HTTP -> {
//                if (this::pluginDownloader.isInitialized) {
                this.pluginDownloader?.download(this)
//                }
            }
            Source.LOCAL -> {
                try {
                    val classList: List<KClass<*>> = JarUtil.extractClassFromJarFile(this.jarClassLoader!!, this.url, useCache = false)
                    for (kClass in classList) {
                        if (kClass.java.isInterfaceImplement(Plugin::class.java)) {
                            val plugin = kClass.java.newInstance() as Plugin
                            val pluginBean = PluginBean()
                            pluginBean.id = plugin.id
                            pluginBean.pluginInstance = plugin
                            addPluginBean(pluginBean)
                            plugin.initialize()
                        }
                    }
                } catch (e: Exception) {
                    logger.error(Constants.String.EXCEPTION, e)
                }
                this.isFinished = true
//                if (this::onLoadedListener.isInitialized) {
                this.onLoadedListener?.onLoaded(this)
//                }
            }
            else -> {
                logger.warning("type:%s not support yet", this.type)
            }
        }
    }

    /**
     * @param pluginBean
     */
    fun addPluginBean(pluginBean: PluginBean) {
        this.pluginBeanMap[pluginBean.id] = pluginBean
    }

    /**
     * @return the pluginBeanMap
     */
    fun getPluginBeanMap(): Map<String, PluginBean> {
        return this.pluginBeanMap
    }

    /**
     * @author oneliang
     */
    interface OnLoadedListener {
        fun onLoaded(pluginFileBean: PluginFileBean)
    }
}