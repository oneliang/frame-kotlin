package com.oneliang.ktx.frame.tester

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.http.HttpUtil

abstract class HttpTestCase {

    lateinit var baseUrlMap: Map<String, String>

    /**
     * http head method
     * @param httpUrl
     * @param httpHeaderList
     * @param byteArray
     * @param timeout
     * @param advancedOption
     * @return ByteArray
     */
    fun head(
        httpUrl: String,
        httpHeaderList: List<HttpUtil.HttpNameValue> = emptyList(),
        byteArray: ByteArray = ByteArray(0),
        timeout: Int = HttpUtil.DEFAULT_TIMEOUT,
        advancedOption: HttpUtil.AdvancedOption? = null
    ): ByteArray {
        return HttpUtil.sendRequestWithWholeBytes(httpUrl, Constants.Http.RequestMethod.HEAD.value, httpHeaderList, byteArray, timeout, advancedOption)
    }

    /**
     * http put method
     * @param httpUrl
     * @param httpHeaderList
     * @param byteArray
     * @param timeout
     * @param advancedOption
     * @return ByteArray
     */
    fun put(
        httpUrl: String,
        httpHeaderList: List<HttpUtil.HttpNameValue> = emptyList(),
        byteArray: ByteArray = ByteArray(0),
        timeout: Int = HttpUtil.DEFAULT_TIMEOUT,
        advancedOption: HttpUtil.AdvancedOption? = null
    ): ByteArray {
        return HttpUtil.sendRequestWithWholeBytes(httpUrl, Constants.Http.RequestMethod.PUT.value, httpHeaderList, byteArray, timeout, advancedOption)
    }

    /**
     * http delete method
     * @param httpUrl
     * @param httpHeaderList
     * @param byteArray
     * @param timeout
     * @param advancedOption
     * @return ByteArray
     */
    fun delete(
        httpUrl: String,
        httpHeaderList: List<HttpUtil.HttpNameValue> = emptyList(),
        byteArray: ByteArray = ByteArray(0),
        timeout: Int = HttpUtil.DEFAULT_TIMEOUT,
        advancedOption: HttpUtil.AdvancedOption? = null
    ): ByteArray {
        return HttpUtil.sendRequestWithWholeBytes(httpUrl, Constants.Http.RequestMethod.DELETE.value, httpHeaderList, byteArray, timeout, advancedOption)
    }

    /**
     * http options method
     * @param httpUrl
     * @param httpHeaderList
     * @param byteArray
     * @param timeout
     * @param advancedOption
     * @return ByteArray
     */
    fun options(
        httpUrl: String,
        httpHeaderList: List<HttpUtil.HttpNameValue> = emptyList(),
        byteArray: ByteArray = ByteArray(0),
        timeout: Int = HttpUtil.DEFAULT_TIMEOUT,
        advancedOption: HttpUtil.AdvancedOption? = null
    ): ByteArray {
        return HttpUtil.sendRequestWithWholeBytes(httpUrl, Constants.Http.RequestMethod.OPTIONS.value, httpHeaderList, byteArray, timeout, advancedOption)
    }

    /**
     * http trace method
     * @param httpUrl
     * @param httpHeaderList
     * @param byteArray
     * @param timeout
     * @param advancedOption
     * @return ByteArray
     */
    fun trace(
        httpUrl: String,
        httpHeaderList: List<HttpUtil.HttpNameValue> = emptyList(),
        byteArray: ByteArray = ByteArray(0),
        timeout: Int = HttpUtil.DEFAULT_TIMEOUT,
        advancedOption: HttpUtil.AdvancedOption? = null
    ): ByteArray {
        return HttpUtil.sendRequestWithWholeBytes(httpUrl, Constants.Http.RequestMethod.TRACE.value, httpHeaderList, byteArray, timeout, advancedOption)
    }

    /**
     * http get method
     * @param httpUrl
     * @param httpHeaderList
     * @param httpParameterList
     * @param timeout
     * @param advancedOption
     * @return ByteArray
     */
    fun get(
        httpUrl: String,
        httpHeaderList: List<HttpUtil.HttpNameValue> = emptyList(),
        httpParameterList: List<HttpUtil.HttpNameValue> = emptyList(),
        timeout: Int = HttpUtil.DEFAULT_TIMEOUT,
        advancedOption: HttpUtil.AdvancedOption? = null
    ): ByteArray {
        return HttpUtil.sendRequestGetWithReturnBytes(httpUrl, httpHeaderList, httpParameterList, timeout, advancedOption)
    }

    /**
     * http post method
     * @param httpUrl
     * @param httpHeaderList
     * @param byteArray
     * @param timeout
     * @param advancedOption
     * @return ByteArray
     */
    fun post(
        httpUrl: String,
        httpHeaderList: List<HttpUtil.HttpNameValue> = emptyList(),
        byteArray: ByteArray = ByteArray(0),
        timeout: Int = HttpUtil.DEFAULT_TIMEOUT,
        advancedOption: HttpUtil.AdvancedOption? = null
    ): ByteArray {
        return HttpUtil.sendRequestPostWithWholeBytes(httpUrl, httpHeaderList, byteArray, timeout, advancedOption)
    }


    abstract fun test()
}