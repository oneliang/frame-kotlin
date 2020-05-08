package com.oneliang.ktx.frame.servlet.filter

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.replaceAllLines
import com.oneliang.ktx.util.common.replaceAllSpace
import com.oneliang.ktx.util.http.HttpUtil
import com.oneliang.ktx.util.json.JsonArray
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
        private const val ACCESS_CONTROL = "accessControl"
    }

    private val headerList = mutableListOf<HttpUtil.HttpNameValue>()
    private val accessControlSet = mutableSetOf<String>()

    /**
     *
     * Method: public void init(FilterConfig filterConfig) throws ServletException
     * @param filterConfig
     * @throws ServletException
     * This method will be initial the key 'encoding' and 'ignore' in web.xml
     */
    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        logger.info("initialize filter:${this::class}")
        val responseHeaderJson = filterConfig.getInitParameter(RESPONSE_HEADER_JSON).nullToBlank()
        if (responseHeaderJson.isNotBlank()) {
            val fixResponseHeaderJson = responseHeaderJson.replaceAllLines().replaceAllSpace()
            try {
                logger.info("response header json:$fixResponseHeaderJson")
                if (fixResponseHeaderJson.isNotBlank()) {
                    val headerJsonArray = JsonArray(fixResponseHeaderJson)
                    for (i in 0 until headerJsonArray.length()) {
                        val headerJsonObject = headerJsonArray.getJsonObject(i)
                        headerList.add(HttpUtil.HttpNameValue(headerJsonObject.getString(HEADER_KEY), headerJsonObject.getString(HEADER_VALUE)))
                    }
                }
            } catch (e: Throwable) {
                logger.error("init exception", e)
            }
        }

        val accessControl = filterConfig.getInitParameter(ACCESS_CONTROL).nullToBlank()
        if (accessControl.isNotBlank()) {
            this.accessControlSet.clear()
            accessControl.split(Constants.Symbol.COMMA).forEach {
                this.accessControlSet += it.trim()
            }
        }
    }

    /**
     *
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
        headerList.forEach {
            httpServletResponse.setHeader(it.name, it.value)
        }
        if (accessControlSet.isNotEmpty()) {
            val httpServletRequest = servletRequest as HttpServletRequest
            val httpHeaderOrigin = httpServletRequest.getHeader(Constants.Http.HeaderKey.ORIGIN).nullToBlank()
            logger.info("header[Origin] is:%s", httpHeaderOrigin)
            if (httpHeaderOrigin.isNotBlank() && accessControlSet.contains(httpHeaderOrigin)) {
                httpServletResponse.setHeader(Constants.Http.HeaderKey.ACCESS_CONTROL_ALLOW_ORIGIN, httpHeaderOrigin)
            } else {
                logger.error("header[Origin] is not support for ${if (httpHeaderOrigin.isBlank()) "blank" else httpHeaderOrigin}")
            }
        }
        filterChain.doFilter(servletRequest, servletResponse)
    }

    /**
     * Method: public void destroy()
     */
    override fun destroy() {
        headerList.clear()
    }
}
