package com.oneliang.ktx.frame.configuration

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.frame.context.Context
import com.oneliang.ktx.util.common.JavaXmlUtil
import com.oneliang.ktx.util.common.ObjectUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.Map.Entry
import kotlin.reflect.KClass

class ConfigurationContext : AbstractContext() {
    companion object {
        private val logger = LoggerManager.getLogger(ConfigurationContext::class)
        internal val configurationBeanMap = ConcurrentHashMap<String, ConfigurationBean>()
    }

    private val selfConfigurationBeanMap = ConcurrentHashMap<String, ConfigurationBean>()
    /**
     * @return the initialized
     */
    private var initialized = false

    /**
     * is initialized
     */
    fun isInitialized(): Boolean {
        return this.initialized
    }

    /**
     * get configuration bean entry set
     * @return the configurationBeanMap
     */
    val configurationBeanEntrySet: Set<Entry<String, ConfigurationBean>>
        get() = configurationBeanMap.entries

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        val fixParameters = fixParameters(parameters)
        try {
            val path = this.classesRealPath + fixParameters
            val document = JavaXmlUtil.parse(path)
            val root = document.documentElement
            val configurationList = root.getElementsByTagName(ConfigurationBean.TAG_CONFIGURATION)
            if (configurationList != null) {
                val length = configurationList.length
                for (index in 0 until length) {
                    val configurationBean = ConfigurationBean()
                    val configurationNode = configurationList.item(index)
                    val configurationAttributesMap = configurationNode.attributes
                    JavaXmlUtil.initializeFromAttributeMap(configurationBean, configurationAttributesMap)
                    val context = this.classLoader.loadClass(configurationBean.contextClass).newInstance() as Context
                    val configurationBeanId = configurationBean.id
                    logger.info("Context:" + context.javaClass.name + ",id:" + configurationBeanId + " is initializing.")
                    if (context is AbstractContext) {
                        context.projectRealPath = this.projectRealPath
                        context.classesRealPath = this.classesRealPath
                    }
                    if (configurationBeanMap.containsKey(configurationBeanId)) {
                        val errorMessage = "configuration error, configuration bean id is exist, id:%s".format(configurationBeanId)
                        logger.error(errorMessage)
                        throw InitializeException(errorMessage)
                    }
                    context.initialize(configurationBean.parameters)
                    configurationBean.contextInstance = context
                    configurationBeanMap[configurationBean.id] = configurationBean
                    this.selfConfigurationBeanMap[configurationBean.id] = configurationBean
                }
            }
            this.initialized = true
        } catch (e: Throwable) {
            logger.error("parameter:%s", e, fixParameters)
            throw InitializeException(fixParameters, e)
        }
    }

    /**
     * destroy,only destroy self,recursion
     */
    override fun destroy() {
        val iterator = this.selfConfigurationBeanMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val configurationBean = entry.value
            configurationBean.contextInstance?.destroy()
            configurationBeanMap.remove(configurationBean.id)
        }
        this.selfConfigurationBeanMap.clear()
    }

    /**
     * destroy all,include destroy all configuration context,all configuration bean and all object
     */
    fun destroyAll() {
        this.destroy()
        configurationBeanMap.clear()
        objectMap.clear()
    }

    /**
     * find context
     * @param id
     * @return Context
     */
    fun findContext(id: String): Context? {
        return configurationBeanMap[id]?.contextInstance
    }


    /**
     * find context
     * @param <T>
     * @param kClass
     * @return T
    </T> */
    @Suppress("UNCHECKED_CAST")
    fun <T : Context> findContext(kClass: KClass<T>, whenFound: (T) -> Unit = {}): T? {
        var contextInstance: T? = null
        for ((_, value) in configurationBeanMap) {
            val context = value.contextInstance
            if (ObjectUtil.isEntity(context as Any, kClass.java)) {
                contextInstance = context as T
                break
            }
        }
        if (contextInstance != null) {
            whenFound(contextInstance)
        }
        return contextInstance
    }
}
