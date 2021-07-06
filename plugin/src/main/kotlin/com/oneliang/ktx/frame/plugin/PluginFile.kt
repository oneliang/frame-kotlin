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

class PluginFile(val id: String, val type: Type = Type.JAR, val source: Source = Source.LOCAL, val url: String = Constants.String.BLANK) {

    companion object {
        private val logger = LoggerManager.getLogger(PluginFile::class)
    }

    enum class Type(val value: Int) {
        SOURCE_CODE(0), JAR(1)
    }

    enum class Source(val value: Int) {
        LOCAL(0), HTTP(1), OTHER(2)
    }

    private val pluginWrapperMap: MutableMap<String, PluginWrapper> = ConcurrentHashMap<String, PluginWrapper>()
    internal var broadcastManager: BroadcastManager? = null
    internal var jarClassLoader: JarClassLoader? = null

    var saveFullFilename: String = Constants.String.BLANK
    var onLoadedListener: OnLoadedListener? = null
    var pluginDownloader: PluginDownloader? = null
    var isFinished = false
        set(finished) {
            field = finished
            if (isFinished) {
                val message = Message()
                message.actionList += PluginGroup.ACTION_PLUGIN_FILE_FINISHED
                message.putObject(PluginGroup.KEY_PLUGIN_FILE_ID, id)
//                if (this::broadcastManager.isInitialized) {
                this.broadcastManager?.sendBroadcast(message)
//                }
            }
        }

    fun destroy() {
        this.broadcastManager = null
        this.jarClassLoader = null
        this.pluginWrapperMap.forEach { (_, plugin) ->
            plugin.pluginInstance = null
        }
        this.pluginWrapperMap.clear()
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
        if (this.pluginWrapperMap.containsKey(pluginId)) {
            val pluginWrapper = this.pluginWrapperMap[pluginId]
            if (pluginWrapper != null) {
                plugin = pluginWrapper.pluginInstance
            }
        }
        return plugin
    }

    /**
     * load plugin
     */
    fun loadPlugin() {
        when (this.type) {
            Type.SOURCE_CODE -> loadPluginByCode()
            Type.JAR -> loadPluginByJar()
        }
    }

    /**
     * load plugin by code
     */
    private fun loadPluginByCode() {
        this.pluginWrapperMap.forEach { (_, pluginWrapper) ->
            val plugin = pluginWrapper.pluginInstance
            plugin?.initialize()
        }
        this.isFinished = true
//        if (this::onLoadedListener.isInitialized) {
        this.onLoadedListener?.onLoaded(this)
//        }
    }

    /**
     * load plugin by jar
     */
    private fun loadPluginByJar() {
        when (this.source) {
            Source.HTTP -> {
//                if (this::pluginDownloader.isInitialized) {
                this.pluginDownloader?.download(this)
//                }
            }
            Source.LOCAL -> {
                val jarClassLoader = this.jarClassLoader ?: error("jar class loader is null, plugin file id:%s".format(this.id))
                try {
                    val classList: List<KClass<*>> = JarUtil.extractClassFromJarFile(jarClassLoader, this.url, useCache = false)
                    for (kClass in classList) {
                        if (kClass.java.isInterfaceImplement(Plugin::class.java)) {
                            val plugin = kClass.java.newInstance() as Plugin
                            logger.info("initializing plugin, id:%s, class:%s", plugin.id, kClass)
                            val pluginWrapper = PluginWrapper()
                            pluginWrapper.id = pluginWrapper.id
                            pluginWrapper.pluginInstance = plugin
                            addPlugin(pluginWrapper)
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
     * @param pluginWrapper
     */
    fun addPlugin(pluginWrapper: PluginWrapper) {
        this.pluginWrapperMap[pluginWrapper.id] = pluginWrapper
    }

    /**
     * @return the pluginMap
     */
    fun getPluginMap(): Map<String, PluginWrapper> {
        return this.pluginWrapperMap
    }

    /**
     * @author oneliang
     */
    interface OnLoadedListener {
        fun onLoaded(pluginFile: PluginFile)
    }
}