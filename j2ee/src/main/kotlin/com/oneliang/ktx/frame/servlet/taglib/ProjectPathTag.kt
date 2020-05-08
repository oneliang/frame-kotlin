package com.oneliang.ktx.frame.servlet.taglib

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.logging.LoggerManager
import javax.servlet.http.HttpServletRequest
import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

class ProjectPathTag : BodyTagSupport() {
    companion object {
        private val logger = LoggerManager.getLogger(ProjectPathTag::class)
    }

    /**
     * doStartTag
     */
    @Throws(JspException::class)
    override fun doStartTag(): Int {
        val request = pageContext.request as HttpServletRequest
        val path = request.contextPath
        try {
            pageContext.out.print(path)
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
        }
        return EVAL_PAGE
    }
}
