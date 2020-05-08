package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.frame.bean.Page
import com.oneliang.ktx.frame.servlet.ActionUtil
import com.oneliang.ktx.util.common.KotlinClassUtil
import com.oneliang.ktx.util.common.toObject
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

/**
 *
 *
 * Class: abstract class,make sub class union
 *
 *
 * com.lwx.frame.servlet.CommonAction
 * abstract class
 * @author Dandelion
 * @since 2008-07-31
 */
abstract class CommonAction : BaseAction(), ActionInterface {

    protected val classProcessor = KotlinClassUtil.DEFAULT_KOTLIN_CLASS_PROCESSOR

    /**
     * Method: get page
     * @return Page
     */
    protected val page: Page
        get() {
            val request = ActionUtil.servletRequest
            return getPage(request)
        }

    /**
     * Method: set the instance object to the request
     * @param request
     */
    protected fun <T : Any> setObjectToRequest(request: ServletRequest, key: String, value: T) {
        request.setAttribute(key, value)
    }

    /**
     * Method: set the request values to object
     *
     * @param <T>
     * @param object
    </T> */
    protected fun <T : Any> requestValuesToObject(instance: T) {
        val request = ActionUtil.servletRequest
        this.requestValuesToObject(request, instance)
    }

    /**
     * Method: set the request values to object
     * @param <T>
     * @param request
     * @param object
    </T> */
    protected fun <T : Any> requestValuesToObject(request: ServletRequest, instance: T) {
        val map = request.parameterMap
        map.toObject(instance, classProcessor)
    }

    /**
     * Method: set the instance object to the session
     * @param <T>
     * @param request
     * @param key
     * @param value
    </T> */
    protected fun <T : Any> setObjectToSession(request: ServletRequest, key: String, value: T) {
        (request as HttpServletRequest).session.setAttribute(key, value)
    }

    /**
     * Method: get the instance object to the session by key
     * @param request
     * @param key
     * @return Object
     */
    protected fun getObjectFromSession(request: ServletRequest, key: String): Any {
        return (request as HttpServletRequest).session.getAttribute(key)
    }

    /**
     * Method: remove object from session
     * @param request
     * @param key
     */
    protected fun removeObjectFromSession(request: ServletRequest, key: String) {
        (request as HttpServletRequest).session.removeAttribute(key)
    }

    /**
     * Method: get the parameter from request
     * @param request
     * @param parameter
     * @return String
     */
    protected fun getParameter(request: ServletRequest, parameter: String): String {
        return request.getParameter(parameter)
    }

    /**
     * Method:get the parameter values from request
     * @param request
     * @param parameter
     * @return String[]
     */
    protected fun getParameterValues(request: ServletRequest, parameter: String): Array<String> {
        return request.getParameterValues(parameter)
    }

    /**
     * request.getRequestDispatcher(path).forward(request,response)
     * @param request
     * @param response
     * @param path
     * @throws ActionExecuteException
     */
    @Throws(ActionExecuteException::class)
    protected fun forward(request: ServletRequest, response: ServletResponse, path: String) {
        try {
            request.getRequestDispatcher(path).forward(request, response)
        } catch (e: Exception) {
            throw ActionExecuteException(e)
        }

    }

    /**
     * write
     * @param response
     * @param string
     * @throws ActionExecuteException
     */
    @Throws(ActionExecuteException::class)
    protected fun write(response: ServletResponse, string: String) {
        try {
            response.writer.write(string)
        } catch (e: Exception) {
            throw ActionExecuteException(e)
        }

    }

    /**
     * Method: get page
     * @param request
     * @return Page
     */
    private fun getPage(request: ServletRequest): Page {
        val page = Page()
        this.requestValuesToObject<Any>(request, page)
        return page
    }
}
