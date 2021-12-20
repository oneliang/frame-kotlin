package com.oneliang.ktx.frame.servlet.filter.lifecycle

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.isInterfaceImplement
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.IOException
import javax.servlet.*

/**
 * LifecycleDetectFilter
 * @author oneliang
 * @since 2021-12-20
 */
class LifecycleDetectFilter : Filter {

    companion object {
        private val logger = LoggerManager.getLogger(LifecycleDetectFilter::class)
        private const val FILTER_LIFECYCLE_CLASS_NAME = "filterLifecycleClassName"
    }

    private lateinit var filterLifecycle: FilterLifecycle

    /**
     * initial from config file
     */
    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        logger.info("initialize filter:%s", this::class)
        val filterLifecycleClassName = filterConfig.getInitParameter(FILTER_LIFECYCLE_CLASS_NAME).nullToBlank()
        logger.info("filter lifecycle className:%s", filterLifecycleClassName)
        try {
            val lifecycleDetectClass = Thread.currentThread().contextClassLoader.loadClass(filterLifecycleClassName)
            if (lifecycleDetectClass.isInterfaceImplement(FilterLifecycle::class.java)) {
                this.filterLifecycle = lifecycleDetectClass.newInstance() as FilterLifecycle
                this.filterLifecycle.initialize()
            } else {
                logger.error("Need to set filter parameter(lifecycleDetectClassName) and the class need to implement FilterLifecycle::class")
            }
        } catch (e: Throwable) {
            logger.error(Constants.String.EXCEPTION, e)
        }
    }

    /**
     * do filter
     */
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain) {
        val result = this.filterLifecycle.doFilter(request, response)
        if (result != FilterLifecycle.Result.STOP) {
            filterChain.doFilter(request, response)
        }
    }

    override fun destroy() {
        this.filterLifecycle.destroy()
    }
}