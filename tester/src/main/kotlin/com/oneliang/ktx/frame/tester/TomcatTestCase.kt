package com.oneliang.ktx.frame.tester

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.http.HttpUtil

abstract class TomcatTestCase {

    lateinit var baseUrl: String

    fun head(httpUrl: String, httpHeaderList: List<HttpUtil.HttpNameValue> = emptyList(), byteArray: ByteArray = ByteArray(0), timeout: Int = HttpUtil.DEFAULT_TIMEOUT, advancedOption: HttpUtil.AdvancedOption? = null): ByteArray {
        return HttpUtil.sendRequestWithWholeBytes(httpUrl, Constants.Http.RequestMethod.HEAD.value, httpHeaderList, byteArray, timeout, advancedOption)
    }

    fun put(httpUrl: String, httpHeaderList: List<HttpUtil.HttpNameValue> = emptyList(), byteArray: ByteArray = ByteArray(0), timeout: Int = HttpUtil.DEFAULT_TIMEOUT, advancedOption: HttpUtil.AdvancedOption? = null): ByteArray {
        return HttpUtil.sendRequestWithWholeBytes(httpUrl, Constants.Http.RequestMethod.PUT.value, httpHeaderList, byteArray, timeout, advancedOption)
    }

    fun delete(httpUrl: String, httpHeaderList: List<HttpUtil.HttpNameValue> = emptyList(), byteArray: ByteArray = ByteArray(0), timeout: Int = HttpUtil.DEFAULT_TIMEOUT, advancedOption: HttpUtil.AdvancedOption? = null): ByteArray {
        return HttpUtil.sendRequestWithWholeBytes(httpUrl, Constants.Http.RequestMethod.DELETE.value, httpHeaderList, byteArray, timeout, advancedOption)
    }

    fun options(httpUrl: String, httpHeaderList: List<HttpUtil.HttpNameValue> = emptyList(), byteArray: ByteArray = ByteArray(0), timeout: Int = HttpUtil.DEFAULT_TIMEOUT, advancedOption: HttpUtil.AdvancedOption? = null): ByteArray {
        return HttpUtil.sendRequestWithWholeBytes(httpUrl, Constants.Http.RequestMethod.OPTIONS.value, httpHeaderList, byteArray, timeout, advancedOption)
    }

    fun trace(httpUrl: String, httpHeaderList: List<HttpUtil.HttpNameValue> = emptyList(), byteArray: ByteArray = ByteArray(0), timeout: Int = HttpUtil.DEFAULT_TIMEOUT, advancedOption: HttpUtil.AdvancedOption? = null): ByteArray {
        return HttpUtil.sendRequestWithWholeBytes(httpUrl, Constants.Http.RequestMethod.TRACE.value, httpHeaderList, byteArray, timeout, advancedOption)
    }

    fun get(httpUrl: String, httpHeaderList: List<HttpUtil.HttpNameValue> = emptyList(), httpParameterList: List<HttpUtil.HttpNameValue> = emptyList(), timeout: Int = HttpUtil.DEFAULT_TIMEOUT, advancedOption: HttpUtil.AdvancedOption? = null): ByteArray {
        return HttpUtil.sendRequestGetWithReturnBytes(httpUrl, httpHeaderList, httpParameterList, timeout, advancedOption)
    }

    abstract fun test()
}