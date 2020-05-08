package com.oneliang.ktx.frame.servlet.action

import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

interface ActionInterface {

    /**
     *
     *
     * abstract Method: This method is abstract
     *
     *
     * This method is to execute
     */
    @Throws(ActionExecuteException::class)
    fun execute(request: ServletRequest, response: ServletResponse): String

    /**
     * @author oneliang
     */
    enum class HttpRequestMethod(val code: Int) {
        PUT(0x01), DELETE(0x02), GET(0x04), POST(0x08), HEAD(0x10), OPTIONS(0x20), TRACE(0x40)
    }

    companion object {

        /**
         * common action name
         */
        val ACTION = "action"
        /**
         * request type
         */
        val REQUEST_TYPE = "requestType"
        /**
         * common action type
         */
        val ACTION_LIST = "list"
        val ACTION_ADD = "add"
        val ACTION_ADD_SUBMIT = "addSubmit"
        val ACTION_MODIFY = "modify"
        val ACTION_MODIFY_SUBMIT = "modifySubmit"
        val ACTION_DELETE = "delete"
        val ACTION_VIEW = "view"
        val ACTION_EXPORT = "export"
        val ACTION_GOTO_PAGE = "gotoPage"
        /**
         * request type include ajax and not ajax
         */
        val REQUEST_TYPE_AJAX = "ajax"
        val REQUEST_TYPE_NOT_AJAX = "notAjax"

        /**
         * common forward type
         */
        val FORWARD_LIST = "list"
        val FORWARD_ADD = "add"
        val FORWARD_ADD_SUBMIT = "add.submit"
        val FORWARD_MODIFY = "modify"
        val FORWARD_MODIFY_SUBMIT = "modify.submit"
        val FORWARD_DELETE = "delete"
        val FORWARD_VIEW = "view"
        val FORWARD_GOTO_PAGE = "goto.page"
    }
}
