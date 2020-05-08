package com.oneliang.ktx.frame.uri

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.util.common.JavaXmlUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap

class UriMappingContext : AbstractContext() {
    companion object {
        private val logger = LoggerManager.getLogger(UriMappingContext::class)
        internal val uriMappingBeanMap: MutableMap<String, UriMappingBean> = ConcurrentHashMap()

        /**
         * find uri to
         * @param uriFrom
         * @return String
         */
        fun findUriTo(uriFrom: String): String {
            var uriTo: String = Constants.String.BLANK
            run loop@{
                uriMappingBeanMap.forEach { (from, uriMappingBean) ->
                    val fromRegex = Constants.Symbol.XOR + from + Constants.Symbol.DOLLAR
                    if (uriFrom.matches(fromRegex.toRegex())) {
                        uriTo = uriMappingBean.to
                        uriTo = uriFrom.replace(fromRegex.toRegex(), uriTo)
                        return@loop
                    }
                }
            }
            return uriTo
        }
    }

    override fun initialize(parameters: String) {
        val fixParameters = fixParameters(parameters)
        try {
            val path = this.classesRealPath + fixParameters
            val document = JavaXmlUtil.parse(path)
            val root = document.documentElement
            val uriBeanElementList = root.getElementsByTagName(UriMappingBean.TAG_URI)
            if (uriBeanElementList == null) {
                logger.error("uri bean element list is null")
                return
            }
            val length = uriBeanElementList.length
            for (index in 0 until length) {
                val beanElement = uriBeanElementList.item(index)
                val uriMappingBean = UriMappingBean()
                val attributeMap = beanElement.attributes
                JavaXmlUtil.initializeFromAttributeMap(uriMappingBean, attributeMap)
                uriMappingBeanMap[uriMappingBean.from] = uriMappingBean
            }
        } catch (e: Throwable) {
            logger.error("parameter:%s", e, fixParameters)
            throw InitializeException(fixParameters, e)
        }
    }

    override fun destroy() {
        uriMappingBeanMap.clear()
    }
}
