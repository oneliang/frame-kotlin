package com.oneliang.ktx.frame.servlet.taglib

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.i18n.MessageContext
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.*
import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

class MessageTag : BodyTagSupport() {
    companion object {
        private val logger = LoggerManager.getLogger(MessageTag::class)
        private const val LOCALE = "locale"
    }
    /**
     * @return the key
     */
    /**
     * @param key the key to set
     */
    var key: String = Constants.String.BLANK
    /**
     * @return the locale
     */
    /**
     * @param locale the locale to set
     */
    var locale: String = Constants.String.BLANK

    @Throws(JspException::class)
    override fun doStartTag(): Int {
        return SKIP_BODY
    }

    @Throws(JspException::class)
    override fun doEndTag(): Int {
        try {
            val locale: String
            val localeParameter = this.pageContext.request.getParameter(LOCALE)
            val localeKey = Locale.getDefault().toString()
            locale = if (this.locale.isBlank()) {
                if (localeParameter.isBlank()) {
                    localeKey
                } else {
                    localeParameter
                }
            } else {
                this.locale
            }
            val properties = MessageContext.getMessageProperties(locale)
            var value = this.key
            if (properties != null) {
                value = properties.getProperty(this.key)
            }
            this.pageContext.out.print(value)
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
        }

        return EVAL_PAGE
    }
}
