package com.oneliang.ktx.frame.servlet.filter.lifecycle

import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

interface FilterLifecycle {

    fun initialize()

    fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse): Result

    fun destroy()

    enum class Result {
        NEXT, STOP
    }
}