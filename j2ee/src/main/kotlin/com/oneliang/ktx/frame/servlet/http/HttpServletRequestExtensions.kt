package com.oneliang.ktx.frame.servlet.http

import com.oneliang.ktx.util.http.HttpUtil
import javax.servlet.http.HttpServletRequest

fun HttpServletRequest.copyHeadersToHttpNameValueList(): List<HttpUtil.HttpNameValue> {
    val httpNameValueList = mutableListOf<HttpUtil.HttpNameValue>()
    val headerNames = this.headerNames
    while (headerNames.hasMoreElements()) {
        val headerName = headerNames.nextElement()
        val headerValues = this.getHeaders(headerName)
        while (headerValues.hasMoreElements()) {
            val headerValue = headerValues.nextElement()
            httpNameValueList += HttpUtil.HttpNameValue(headerName, headerValue)
        }
    }
    return httpNameValueList
}