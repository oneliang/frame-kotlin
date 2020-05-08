package com.oneliang.ktx.frame.servlet.taglib

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.logging.LoggerManager
import javax.servlet.http.HttpServletRequest
import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

class BasePathTag : BodyTagSupport() {

    companion object {
        private val logger = LoggerManager.getLogger(BasePathTag::class)
    }

    /**
     * doStartTag
     */
    @Throws(JspException::class)
    override fun doStartTag(): Int {
        val request = pageContext.request as HttpServletRequest
        val path = request.contextPath
        val basePath = request.scheme + Constants.Symbol.COLON + Constants.Symbol.SLASH_LEFT + Constants.Symbol.SLASH_LEFT + request.getServerName() + Constants.Symbol.COLON + request.getServerPort() + path + Constants.Symbol.SLASH_LEFT
        try {
            pageContext.out.print(basePath)
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
        }

        return EVAL_PAGE
    }
}
