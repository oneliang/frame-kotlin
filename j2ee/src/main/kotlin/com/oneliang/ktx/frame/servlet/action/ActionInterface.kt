package com.oneliang.ktx.frame.servlet.action

import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

interface ActionInterface {
    /**
     * abstract Method: This method is abstract
     * This method is to execute
     * @param servletRequest
     * @param servletResponse
     * @return String
     */
    @Throws(ActionExecuteException::class)
    fun execute(servletRequest: ServletRequest, servletResponse: ServletResponse): String

    /**
     * @author oneliang
     */
    enum class HttpRequestMethod(val code: Int) {
        PUT(0x01), DELETE(0x02), GET(0x04), POST(0x08), HEAD(0x10), OPTIONS(0x20), TRACE(0x40)
    }
}
