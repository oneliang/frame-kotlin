package com.oneliang.ktx.frame.servlet.filter

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.matchesPattern
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.IOException
import javax.servlet.*
import javax.servlet.http.HttpServletRequest

/**
 * source filter
 * @author Dandelion
 * @since 2010-06-27
 */
class SourceFilter : Filter {

    companion object {
        private val logger = LoggerManager.getLogger(SourceFilter::class)
        private const val EXCLUDE_PATH = "excludePath"
        private const val ERROR_FORWARD = "errorForward"
    }

    private var excludePaths: Array<String> = emptyArray()
    private var errorForward: String = Constants.String.BLANK

    /**
     * initial from config file
     */
    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        logger.info("initialize filter:${this::class}")
        val excludePaths = filterConfig.getInitParameter(EXCLUDE_PATH).nullToBlank()
        this.errorForward = filterConfig.getInitParameter(ERROR_FORWARD).nullToBlank()
        if (excludePaths.isNotBlank()) {
            val excludePathArray = excludePaths.split(Constants.Symbol.COMMA)
            this.excludePaths = Array(excludePathArray.size) { Constants.String.BLANK }
            for ((i, excludePath) in excludePathArray.withIndex()) {
                this.excludePaths[i] = excludePath.trim()
            }
        }
    }

    /**
     * do filter
     */
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        //		HttpServletResponse httpResponse = (HttpServletResponse)response
        //		HttpSession session = httpRequest.getSession()
        //		httpResponse.setHeader("Cache-Control","no-cache")
        //		httpResponse.setHeader("Pragma","no-cache")
        //		httpResponse.setDateHeader ("Expires", -1)
        val projectPath = httpRequest.contextPath
        val requestUri = httpRequest.requestURI
        var excludePathThrough = false
        if (this.excludePaths.isNotEmpty()) {
            for (excludePath in this.excludePaths) {
                val path = projectPath + excludePath
                if (requestUri.matchesPattern(path)) {
                    excludePathThrough = true
                    break
                }
            }
        }
        logger.info("Doing filter, request uri:$requestUri, exclude:$excludePathThrough, project path:$projectPath")
        if (excludePathThrough) {
            filterChain.doFilter(request, response)
        } else {
            httpRequest.getRequestDispatcher(this.errorForward).forward(request, response)
        }
    }

    override fun destroy() {
        this.excludePaths = emptyArray()
        this.errorForward = Constants.String.BLANK
    }
}
