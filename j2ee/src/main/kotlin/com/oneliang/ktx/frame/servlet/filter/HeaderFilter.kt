package com.oneliang.ktx.frame.servlet.filter

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.configuration.ConfigurationContainer
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.replaceAllLines
import com.oneliang.ktx.util.common.replaceAllSpace
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.json.JsonArray
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.IOException
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class HeaderFilter : Filter {
    companion object {
        private val logger = LoggerManager.getLogger(HeaderFilter::class)
        private const val RESPONSE_HEADER_JSON = "responseHeaderJson"
        private const val HEADER_KEY = "key"
        private const val HEADER_VALUE = "value"
        private const val ACCESS_CONTROL_ORIGIN = "accessControlOrigin"
        private const val ACCESS_CONTROL_ORIGIN_FILE = "accessControlOriginFile"
        private const val ACCESS_CONTROL_HEADERS_FILE = "accessControlHeadersFile"
    }

    private val headerMap = mutableMapOf<String, String>()
    private val accessControlOriginSet = mutableSetOf<String>()

    /**
     * Method: public void init(FilterConfig filterConfig) throws ServletException
     * @param filterConfig
     * @throws ServletException
     * This method will be initial in web.xml
     */
    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        logger.info("initialize filter:%s", this::class)
        val responseHeaderJson = filterConfig.getInitParameter(RESPONSE_HEADER_JSON).nullToBlank()
        if (responseHeaderJson.isNotBlank()) {
            val fixResponseHeaderJson = responseHeaderJson.replaceAllLines().replaceAllSpace()
            try {
                logger.info("response header json:%s", fixResponseHeaderJson)
                if (fixResponseHeaderJson.isNotBlank()) {
                    val headerJsonArray = JsonArray(fixResponseHeaderJson)
                    for (i in 0 until headerJsonArray.length()) {
                        val headerJsonObject = headerJsonArray.getJsonObject(i)
                        this.headerMap[headerJsonObject.getString(HEADER_KEY)] = headerJsonObject.getString(HEADER_VALUE)
                    }
                }
            } catch (e: Throwable) {
                logger.error("init exception", e)
            }
        }
        //access control origin step one
        this.accessControlOriginSet.clear()
        val accessControlOrigin = filterConfig.getInitParameter(ACCESS_CONTROL_ORIGIN).nullToBlank()
        if (accessControlOrigin.isNotBlank()) {
            accessControlOrigin.split(Constants.Symbol.COMMA).forEach {
                this.accessControlOriginSet += it.trim()
            }
        }
        //access control origin step two
        val accessControlOriginFile = filterConfig.getInitParameter(ACCESS_CONTROL_ORIGIN_FILE).nullToBlank()
        if (accessControlOriginFile.isNotBlank()) {
            val fixAccessControlOriginFile = accessControlOriginFile.trim()
            val accessControlOriginFullFilename = ConfigurationContainer.rootConfigurationContext.classesRealPath + fixAccessControlOriginFile
            logger.info("access control origin full filename:%s", accessControlOriginFullFilename)
            try {
                accessControlOriginFullFilename.toFile().readLines().forEach {
                    if (it.isNotBlank()) {
                        this.accessControlOriginSet += it.trim()
                    }
                }
            } catch (e: Throwable) {
                logger.error(Constants.String.EXCEPTION, e)
            }
        }
        //access control header step one
        val accessControlHeadersFile = filterConfig.getInitParameter(ACCESS_CONTROL_HEADERS_FILE).nullToBlank()
        if (this.headerMap.containsKey(Constants.Http.HeaderKey.ACCESS_CONTROL_ALLOW_HEADERS) && accessControlHeadersFile.isNotBlank()) {
            val fixAccessControlHeaderFile = accessControlHeadersFile.trim()
            val accessControlHeaderFullFilename = ConfigurationContainer.rootConfigurationContext.classesRealPath + fixAccessControlHeaderFile
            logger.info("access control header full filename:%s", accessControlHeaderFullFilename)
            try {
                val accessControlHeaderValueList = mutableListOf<String>()
                accessControlHeaderFullFilename.toFile().readLines().forEach {
                    if (it.isNotBlank()) {
                        accessControlHeaderValueList += it.trim()
                    }
                }
                this.headerMap[Constants.Http.HeaderKey.ACCESS_CONTROL_ALLOW_HEADERS] = accessControlHeaderValueList.joinToString()
            } catch (e: Throwable) {
                logger.error(Constants.String.EXCEPTION, e)
            }
        }
        logger.info("initialize filter:%s, finished, response header map is:%s", this::class, this.headerMap.toJson())
    }

    /**
     * Method: public void doFilter(ServletRequest,ServletResponse,FilterChain) throws IOException,ServletException
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException,ServletException
     * This method will be doFilter in request scope and response scope
     */
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpServletResponse = servletResponse as HttpServletResponse
        this.headerMap.forEach { (key, value) ->
            httpServletResponse.setHeader(key, value)
        }
        if (this.accessControlOriginSet.isNotEmpty()) {
            val httpServletRequest = servletRequest as HttpServletRequest
            val httpHeaderOrigin = httpServletRequest.getHeader(Constants.Http.HeaderKey.ORIGIN).nullToBlank()
            logger.info("header[Origin] is:%s", httpHeaderOrigin)
            if (httpHeaderOrigin.isNotBlank() && accessControlOriginSet.contains(httpHeaderOrigin)) {
                httpServletResponse.setHeader(Constants.Http.HeaderKey.ACCESS_CONTROL_ALLOW_ORIGIN, httpHeaderOrigin)
            } else {
                logger.error("header[Origin] is not support for %s", httpHeaderOrigin.ifBlank { "blank" })
            }
        }
        filterChain.doFilter(servletRequest, servletResponse)
    }

    /**
     * Method: public void destroy()
     */
    override fun destroy() {
        headerMap.clear()
    }
}
