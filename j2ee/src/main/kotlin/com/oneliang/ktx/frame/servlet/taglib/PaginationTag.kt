package com.oneliang.ktx.frame.servlet.taglib

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.bean.Page
import com.oneliang.ktx.util.logging.LoggerManager
import javax.servlet.jsp.JspException
import javax.servlet.jsp.tagext.BodyTagSupport

class PaginationTag : BodyTagSupport() {
    companion object {
        private val logger = LoggerManager.getLogger(PaginationTag::class)
        private const val BLANK_2 = "&nbsp;&nbsp;"
        private const val BLANK_8 = BLANK_2 + BLANK_2 + BLANK_2 + BLANK_2
        private const val TIPS_PAGE = "Page:"
        private const val TIPS_ROWS = "Rows:"
    }
    /**
     * @return the action
     */
    /**
     * @param action the action to set
     */
    var action: String = Constants.String.BLANK
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
     * @return the size
     */
    /**
     * @param size the size to set
     */
    var size = 1
    /**
     * @return the firstIcon
     */
    /**
     * @param firstIcon the firstIcon to set
     */
    var firstIcon = "first"
    /**
     * @return the lastIcon
     */
    /**
     * @param lastIcon the lastIcon to set
     */
    var lastIcon = "last"
    /**
     * @return the previousIcon
     */
    /**
     * @param previousIcon the previousIcon to set
     */
    var previousIcon = "previous"
    /**
     * @return the nextIcon
     */
    /**
     * @param nextIcon the nextIcon to set
     */
    var nextIcon = "next"
    /**
     * @return the linkString
     */
    /**
     * @param linkString the linkString to set
     */
    var linkString: String = Constants.String.BLANK

    @Throws(JspException::class)
    override fun doStartTag(): Int {
        return SKIP_BODY
    }

    @Throws(JspException::class)
    override fun doEndTag(): Int {
        val instance = if (this.scope == Constants.RequestScope.SESSION) {
            this.pageContext.session.getAttribute(value)
        } else {
            this.pageContext.request.getAttribute(value)
        }
        if (instance is Page) {
            val page = instance
            val paginationHtml = StringBuilder()
            val action = if (this.action.indexOf("?") > -1) {
                this.action + "&page="
            } else {
                this.action + "?page="
            }
            //first and previous
            val first = "<a href=\"" + action + page.firstPage + "\" " + linkString + ">" + this.firstIcon + "</a>" + BLANK_2
            val previous = "<a href=\"" + action + (page.page - 1) + "\" " + linkString + ">" + this.previousIcon + "</a>" + BLANK_2
            paginationHtml.append(first)
            paginationHtml.append(previous)
            //middle
            if (this.size > page.totalPages) {
                this.size = page.totalPages
            }
            val middlePosition: Int = if (this.size % 2 == 0) {//even
                this.size / 2
            } else {//odd
                this.size / 2 + 1
            }
            var startPage = 0
            if (page.page <= middlePosition) {
                startPage = 1
            } else if (page.page > middlePosition) {
                if (page.page > page.totalPages - middlePosition) {
                    startPage = page.totalPages - this.size + 1
                } else {
                    startPage = page.page - middlePosition + 1
                }
            }
            for (i in 0 until this.size) {
                val showPage = startPage + i
                val middle = if (showPage == page.page) {
                    "<a href=\"$action$showPage\" $linkString><font color=\"red\">[$BLANK_2$showPage$BLANK_2]</font></a>$BLANK_2"
                } else {
                    "<a href=\"$action$showPage\" $linkString>[$BLANK_2$showPage$BLANK_2]</a>$BLANK_2"
                }
                paginationHtml.append(middle)
            }
            //next and last(total)
            val next = "<a href=\"" + action + (page.page + 1) + "\" " + linkString + ">" + this.nextIcon + "</a>" + BLANK_2
            val last = "<a href=\"" + action + page.totalPages + "\" " + linkString + ">" + this.lastIcon + "</a>"
            paginationHtml.append(next)
            paginationHtml.append(last)
            val other = BLANK_8 + TIPS_PAGE + page.page + "/" + page.totalPages + BLANK_8 + TIPS_ROWS + (page.pageFirstRow + 1) + "~" + (if (page.page * page.rowsPerPage < page.totalRows) page.page * page.rowsPerPage else page.totalRows) + "/" + page.totalRows
            paginationHtml.append(other)
            //goto page
            try {
                this.pageContext.out.println(paginationHtml.toString())
            } catch (e: Exception) {
                logger.error(Constants.Base.EXCEPTION, e)
            }

        }
        return EVAL_PAGE
    }
}
