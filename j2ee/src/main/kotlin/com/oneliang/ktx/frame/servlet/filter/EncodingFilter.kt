package com.oneliang.ktx.frame.servlet.filter

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.IOException
import javax.servlet.*

/**
 *
 *
 * Class: EncodingFilter class
 *
 *
 * com.lwx.frame.filter.EncodingFilter.java
 * This is a encoding filter in commonFrame
 * @author Dandelion
 * @since 2008-07-31
 */
class EncodingFilter : Filter {
    companion object {
        private val logger = LoggerManager.getLogger(EncodingFilter::class)
        private const val DEFAULT_ENCODING = Constants.Encoding.UTF8
        private const val ENCODING = "encoding"
        private const val IGNORE = "ignore"
    }

    private var encoding: String = DEFAULT_ENCODING
    private var filterConfig: FilterConfig? = null
    private var ignore = false
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
        this.filterConfig = filterConfig
        // read from web.xml to initial the key 'encoding' and 'ignore'
        val encoding = filterConfig.getInitParameter(ENCODING)
        val ignore = filterConfig.getInitParameter(IGNORE)
        if (encoding == null) {
            this.encoding = DEFAULT_ENCODING
        } else {
            this.encoding = encoding
        }
        when {
            ignore == null -> this.ignore = false
            ignore.equals("true", ignoreCase = true) -> this.ignore = true
            ignore.equals("yes", ignoreCase = true) -> this.ignore = true
            else -> this.ignore = false
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
        if (!this.ignore) {
            servletRequest.characterEncoding = this.encoding
            servletResponse.characterEncoding = this.encoding
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse)
        } catch (sx: ServletException) {
            this.filterConfig!!.servletContext.log(sx.message)
        } catch (iox: IOException) {
            this.filterConfig!!.servletContext.log(iox.message)
        }
    }

    /**
     *
     * Method: public void destroy()
     * This method will be reset the encoding=null and filterconfig=null
     */
    override fun destroy() {
        this.encoding = DEFAULT_ENCODING
        this.filterConfig = null
    }
}
