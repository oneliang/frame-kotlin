package com.oneliang.ktx.frame.bean

/**
 * @author Dandelion
 * @since 2008-11-10
 */
class Page {

    companion object {
        const val DEFAULT_ROWS = 20
    }
    /**
     * @return the page
     */
    /**
     * @param page the page to set
     */
    var page = 1//request parameter
    /**
     * @return the firstPage
     */
    /**
     * @param firstPage the firstPage to set
     */
    var firstPage = 1//view use
    /**
     * @return the totalPages
     */
    /**
     * @param totalPages the totalPages to set
     */
    var totalPages = 1//view use
    /**
     * @return the totalRows
     */
    /**
     * @param totalRows the totalRows to set
     */
    var totalRows = 0//view use
    /**
     * @return the rowsPerPage
     */
    /**
     * @param rowsPerPage the rowsPerPage to set
     */
    var rowsPerPage = DEFAULT_ROWS//view use
    /**
     * @return the start
     */
    /**
     * @param start the start to set
     */
    var start = 0//ext use
    /**
     * goto page
     * @return page*sizePerPage
     */
    val pageFirstRow: Int
        get() {
            if (this.page < 1 || this.totalPages <= 0) {
                this.page = 1
            } else if (this.page > this.totalPages) {
                this.page = this.totalPages
            }
            return (this.page - 1) * this.rowsPerPage
        }

    fun initialize(totalRows: Int, rowsPerPage: Int) {
        this.firstPage = 1
        this.totalRows = totalRows
        this.rowsPerPage = rowsPerPage
        val totalPagesCount = this.totalRows % this.rowsPerPage
        if (totalPagesCount == 0) {
            this.totalPages = this.totalRows / this.rowsPerPage
        } else {
            this.totalPages = this.totalRows / this.rowsPerPage + 1
        }
    }
}
