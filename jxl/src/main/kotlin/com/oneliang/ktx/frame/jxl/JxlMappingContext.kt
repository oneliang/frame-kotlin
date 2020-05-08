package com.oneliang.ktx.frame.jxl

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.util.common.JavaXmlUtil
import com.oneliang.ktx.util.jxl.JxlMappingBean
import com.oneliang.ktx.util.jxl.JxlMappingColumnBean
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class JxlMappingContext : AbstractContext() {
    companion object {
        private val logger = LoggerManager.getLogger(JxlMappingContext::class)
        internal val typeImportJxlMappingBeanMap = ConcurrentHashMap<String, JxlMappingBean>()
        internal val nameImportJxlMappingBeanMap = ConcurrentHashMap<String, JxlMappingBean>()
        internal val typeExportJxlMappingBeanMap = ConcurrentHashMap<String, JxlMappingBean>()
        internal val nameExportJxlMappingBeanMap = ConcurrentHashMap<String, JxlMappingBean>()
    }

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        val fixParameters = fixParameters(parameters)
        try {
            var path = fixParameters
            path = classesRealPath + path
            val document = JavaXmlUtil.parse(path)
            val root = document.documentElement
            val beanElementList = root.getElementsByTagName(JxlMappingBean.TAG_BEAN) ?: return
            val length = beanElementList.length
            for (index in 0 until length) {
                val beanElement = beanElementList.item(index)
                val jxlMappingBean = JxlMappingBean()
                val attributeMap = beanElement.attributes
                JavaXmlUtil.initializeFromAttributeMap(jxlMappingBean, attributeMap)
                //bean column
                val childNodeList = beanElement.childNodes
                if (childNodeList != null) {
                    val childNodeLength = childNodeList.length
                    for (childNodeIndex in 0 until childNodeLength) {
                        val childNode = childNodeList.item(childNodeIndex)
                        val nodeName = childNode.nodeName
                        if (nodeName != JxlMappingColumnBean.TAG_COLUMN) {
                            continue
                        }
                        val jxlMappingColumnBean = JxlMappingColumnBean()
                        val childNodeAttributeMap = childNode.attributes
                        JavaXmlUtil.initializeFromAttributeMap(jxlMappingColumnBean, childNodeAttributeMap)
                        jxlMappingBean.addJxlMappingColumnBean(jxlMappingColumnBean)
                    }
                }
                val useFor = jxlMappingBean.useFor
                val type = jxlMappingBean.type
                if (useFor == JxlMappingBean.USE_FOR_IMPORT) {
                    typeImportJxlMappingBeanMap[type] = jxlMappingBean
                    nameImportJxlMappingBeanMap[this.classLoader.loadClass(type).simpleName] = jxlMappingBean
                } else if (useFor == JxlMappingBean.USE_FOR_EXPORT) {
                    typeExportJxlMappingBeanMap[type] = jxlMappingBean
                    nameExportJxlMappingBeanMap[this.classLoader.loadClass(type).simpleName] = jxlMappingBean
                }
            }
        } catch (e: Throwable) {
            logger.error("parameter:%s", e, fixParameters)
            throw InitializeException(fixParameters, e)
        }
    }

    /**
     * destroy
     */
    override fun destroy() {
        typeImportJxlMappingBeanMap.clear()
        nameImportJxlMappingBeanMap.clear()
        typeExportJxlMappingBeanMap.clear()
        nameExportJxlMappingBeanMap.clear()
    }

    /**
     * findImportJxlMappingBean
     * @param <T>
     * @param kClass
     * @return JxlMappingBean
    </T> */
    fun <T : Any> findImportJxlMappingBean(kClass: KClass<T>): JxlMappingBean? {
        val className = kClass.java.name
        return typeImportJxlMappingBeanMap[className]
    }

    /**
     * @param name full name or simple name
     * @return JxlMappingBean
     * @throws Exception
     */
    @Throws(Exception::class)
    fun findImportJxlMappingBean(name: String): JxlMappingBean? {
        var bean: JxlMappingBean? = typeImportJxlMappingBeanMap[name]
        if (bean == null) {
            bean = nameImportJxlMappingBeanMap[name]
        }
        return bean
    }

    /**
     * findExportJxlMappingBean
     * @param <T>
     * @param kClass
     * @return JxlMappingBean
    </T> */
    fun <T : Any> findExportJxlMappingBean(kClass: KClass<T>): JxlMappingBean? {
        val className = kClass.java.name
        return typeExportJxlMappingBeanMap[className]
    }

    /**
     * @param name full name or simple name
     * @return JxlMappingBean
     * @throws Exception
     */
    @Throws(Exception::class)
    fun findExportJxlMappingBean(name: String): JxlMappingBean? {
        var bean: JxlMappingBean? = typeExportJxlMappingBeanMap[name]
        if (bean == null) {
            bean = nameExportJxlMappingBeanMap[name]
        }
        return bean
    }
}
