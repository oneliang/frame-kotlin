package com.oneliang.ktx.frame.servlet.taglib

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.logging.LoggerManager
import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

class RemoteHostTag : BodyTagSupport() {
    companion object {
        private val logger = LoggerManager.getLogger(RemoteHostTag::class)
    }

    /**
     * doStartTag
     */
    @Throws(JspException::class)
    override fun doStartTag(): Int {
        val remoteHost = pageContext.request.remoteHost
        try {
            pageContext.out.print(remoteHost)
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
        }

        return EVAL_PAGE
    }
}