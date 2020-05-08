package com.oneliang.ktx.frame.servlet.taglib

import com.oneliang.ktx.Constants
import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

class IfEmptyTag : BodyTagSupport() {

    /**
     * @return the value
     */
    /**
     * @param value the value to set
     */
    var value: String = Constants.String.BLANK
    /**
     * @return the scope
     */
    /**
     * @param scope the scope to set
     */
    var scope: String = Constants.String.BLANK

    /**
     * doStartTag
     */
    @Throws(JspException::class)
    override fun doStartTag(): Int {
        val o: Any? = if (this.scope == Constants.RequestScope.SESSION) {
            this.pageContext.session.getAttribute(this.value)
        } else {
            this.pageContext.request.getAttribute(this.value)
        }
        var eval = EVAL_PAGE
        if (o == null) {
            eval = EVAL_BODY_INCLUDE
        } else {
            if (o is List<*>) {
                val list = o as List<*>?
                if (list!!.isEmpty()) {
                    eval = EVAL_BODY_INCLUDE
                }
            }
        }
        return eval
    }

    /**
     * doEndTag
     */
    @Throws(JspException::class)
    override fun doEndTag(): Int {
        return EVAL_PAGE
    }
}
