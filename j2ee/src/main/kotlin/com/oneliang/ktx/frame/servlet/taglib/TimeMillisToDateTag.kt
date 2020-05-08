package com.oneliang.ktx.frame.servlet.taglib

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.toFormatString
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.*
import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

class TimeMillisToDateTag : BodyTagSupport() {
    companion object {
        private val logger = LoggerManager.getLogger(TimeMillisToDateTag::class)
    }
    /**
     * @return the value
     */
    /**
     * @param value the value to set
     */
    var value: String = Constants.String.BLANK
    /**
     * @return the format
     */
    /**
     * @param format the format to set
     */
    var format = Constants.Time.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND

    @Throws(JspException::class)
    override fun doStartTag(): Int {
        if (this.value.isNotBlank()) {
            try {
                val dateString = Date(this.value.toLong()).toFormatString(this.format)
                this.pageContext.out.print(dateString)
            } catch (e: Exception) {
                logger.error(Constants.Base.EXCEPTION, e)
            }
        }
        return EVAL_PAGE
    }

    @Throws(JspException::class)
    override fun doEndTag(): Int {
        return EVAL_PAGE
    }
}
