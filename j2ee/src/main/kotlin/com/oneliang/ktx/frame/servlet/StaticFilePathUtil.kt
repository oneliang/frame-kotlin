package com.oneliang.ktx.frame.servlet

import com.oneliang.ktx.Constants
import java.util.concurrent.ConcurrentHashMap

import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

object StaticFilePathUtil {

    private const val RESPONSE_STATIC_CONTENTTYPE = "text/html;charset=" + Constants.Encoding.UTF8

    /**
     * key:uri,value:
     */
    private val staticFilePathMap = ConcurrentHashMap<String, String>()

    /**
     * is contains static file path
     * @param staticFilePath
     * @return boolean
     */
    internal fun isContainsStaticFilePath(staticFilePath: String): Boolean {
        return staticFilePathMap.containsKey(staticFilePath)
    }

    /**
     * add static file path
     * @param key
     * @param staticFilePath
     */
    internal fun addStaticFilePath(key: String, staticFilePath: String) {
        staticFilePathMap[key] = staticFilePath
    }

    /**
     * get static file path
     * @param key
     * @return static file path
     */
    fun getStaticFilePath(key: String): String? {
        return staticFilePathMap[key]
    }

    /**
     * update static file path
     * @param key
     * @param staticFilePath
     */
    fun updateStaticFilePath(key: String, staticFilePath: String) {
        staticFilePathMap[key] = staticFilePath
    }

    /**
     * staticize
     * @param path
     * @param staticFilePath
     * @param request
     * @param response
     * @return boolean
     */
    internal fun staticize(path: String, staticFilePath: String, request: ServletRequest, response: ServletResponse): Boolean {
        response.contentType = RESPONSE_STATIC_CONTENTTYPE
        val result = ActionUtil.includeJspAndSave(path, staticFilePath, request, response)
        return result
    }
}
