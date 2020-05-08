package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.util.common.JavaXmlUtil
import com.oneliang.ktx.util.logging.LoggerManager
import kotlin.reflect.KClass

open class MappingContext : AbstractContext() {
    companion object {
        private val logger = LoggerManager.getLogger(MappingContext::class)
        internal val classNameMappingBeanMap = mutableMapOf<String, MappingBean>()
        internal val simpleNameMappingBeanMap = mutableMapOf<String, MappingBean>()

        init {
            val totalMappingBean = Total.toMappingBean()
            val totalClassName = totalMappingBean.type
            classNameMappingBeanMap[totalClassName] = totalMappingBean
            simpleNameMappingBeanMap[Total::class.java.simpleName] = totalMappingBean
        }
    }

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        val fixParameters = fixParameters(parameters)
        try {
            val path = this.classesRealPath + fixParameters
            val document = JavaXmlUtil.parse(path)
            val root = document.documentElement
            val beanElementList = root.getElementsByTagName(MappingBean.TAG_BEAN) ?: return
            val length = beanElementList.length
            for (index in 0 until length) {
                val beanElement = beanElementList.item(index)
                val mappingBean = MappingBean()
                val attributeMap = beanElement.attributes
                JavaXmlUtil.initializeFromAttributeMap(mappingBean, attributeMap)
                //bean column
                val childNodeList = beanElement.childNodes ?: continue
                val childNodeLength = childNodeList.length
                for (childNodeIndex in 0 until childNodeLength) {
                    val childNode = childNodeList.item(childNodeIndex)
                    val nodeName = childNode.nodeName
                    if (nodeName == MappingColumnBean.TAG_COLUMN) {
                        val mappingColumnBean = MappingColumnBean()
                        val childNodeAttributeMap = childNode.attributes
                        JavaXmlUtil.initializeFromAttributeMap(mappingColumnBean, childNodeAttributeMap)
                        mappingBean.addMappingColumnBean(mappingColumnBean)
                    }
                }
                val className = mappingBean.type
                classNameMappingBeanMap[className] = mappingBean
                simpleNameMappingBeanMap[this.classLoader.loadClass(className).simpleName] = mappingBean
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
        classNameMappingBeanMap.clear()
        simpleNameMappingBeanMap.clear()
    }

    /**
     * findMappingBean
     * @param <T>
     * @param kClass
     * @return MappingBean
    </T> */
    fun <T : Any> findMappingBean(kClass: KClass<T>): MappingBean? {
        val className = kClass.java.name
        return classNameMappingBeanMap[className]
    }

    /**
     * @param name full name or simple name
     * @return MappingBean
     */
    fun findMappingBean(name: String): MappingBean? {
        var bean: MappingBean?
        bean = classNameMappingBeanMap[name]
        if (bean == null) {
            bean = simpleNameMappingBeanMap[name]
        }
        return bean
    }
}
