package com.oneliang.ktx.frame.servlet.taglib

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.toFormatString
import com.oneliang.ktx.util.common.toUtilDate
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.*
import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

class DateFormatTag : BodyTagSupport() {

    companion object {
        private val logger = LoggerManager.getLogger(DateFormatTag::class)
    }
    /**
     * @return the value
     */
    /**
     * @param value the value to set
     */
    var value: String = Constants.String.BLANK
    /**
     * @return the originalFormat
     */
    /**
     * @param originalFormat the originalFormat to set
     */
    var originalFormat: String = Constants.String.BLANK
    /**
     * @return the format
     */
    /**
     * @param format the format to set
     */
    var format = Constants.Time.YEAR_MONTH_DAY
    /**
     * @return the originalLanguage
     */
    /**
     * @param originalLanguage the originalLanguage to set
     */
    var originalLanguage: String? = null
    /**
     * @return the language
     */
    /**
     * @param language the language to set
     */
    var language: String? = null
    /**
     * @return the originalCountry
     */
    /**
     * @param originalCountry the originalCountry to set
     */
    var originalCountry: String? = null
    /**
     * @return the country
     */
    /**
     * @param country the country to set
     */
    var country: String? = null

    /**
     * doStartTag
     */
    @Throws(JspException::class)
    override fun doStartTag(): Int {
        if (this.value.isNotBlank()) {
            try {
                val originalLocale = if (!this.originalLanguage.isNullOrBlank() && !this.originalCountry.isNullOrBlank()) {
                    Locale(this.originalLanguage, this.originalCountry!!)
                } else if (!this.originalLanguage.isNullOrBlank()) {
                    Locale(this.originalLanguage)
                } else {
                    Locale.getDefault()
                }
                val locale = if (!this.language.isNullOrBlank() && !this.country.isNullOrBlank()) {
                    Locale(this.language, this.country!!)
                } else if (!this.language.isNullOrBlank()) {
                    Locale(this.language)
                } else {
                    Locale.getDefault()
                }
                val originalFormat = if (this.originalFormat.isNotBlank()) {
                    this.originalFormat
                } else {
                    Constants.Time.DEFAULT_DATE_FORMAT
                }
                val date = this.value.toUtilDate(originalFormat, originalLocale)
                val dateString = date.toFormatString(this.format, locale)
                this.pageContext.out.print(dateString)
            } catch (e: Exception) {
                logger.error(Constants.Base.EXCEPTION, e)
            }
        }
        return EVAL_PAGE
    }
}
