package com.oneliang.ktx.frame.collector

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.cache.FileCacheManager
import com.oneliang.ktx.util.http.HttpUtil
import com.oneliang.ktx.util.http.HttpUtil.AdvancedOption
import com.oneliang.ktx.util.http.HttpUtil.Callback
import com.oneliang.ktx.util.http.HttpUtil.HttpNameValue
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream

object CollectUtil {
    private val logger = LoggerManager.getLogger(CollectUtil::class)

    /**
     * collect from http
     * @param httpUrl
     * @param httpHeaderList
     * @param advancedOption
     * @return ByteArrayOutputStream
     */
    fun collectFromHttp(httpUrl: String, httpHeaderList: List<HttpNameValue> = emptyList(), advancedOption: AdvancedOption? = null): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        HttpUtil.sendRequestGet(httpUrl = httpUrl, httpHeaderList = httpHeaderList, advancedOption = advancedOption, callback = object : Callback {
            @Throws(Exception::class)
            override fun httpOkCallback(headerFieldMap: Map<String, List<String>>, inputStream: InputStream, contentLength: Int) {
                val needToUnGzip = headerFieldMap[Constants.Http.HeaderKey.CONTENT_ENCODING]?.contains(Constants.CompressType.GZIP) ?: false
                var newInputStream = inputStream
                if (needToUnGzip) {
                    newInputStream = GZIPInputStream(inputStream)
                }
                byteArrayOutputStream.use {
                    newInputStream.copyTo(it)
                }
            }

            override fun exceptionCallback(throwable: Throwable) {
                throwable.printStackTrace()
                logger.error(Constants.Base.EXCEPTION, throwable)
            }

            @Throws(Exception::class)
            override fun httpNotOkCallback(responseCode: Int, headerFieldMap: Map<String, List<String>>, errorInputStream: InputStream?) {
                logger.error("Response not ok, http:%s, response code:%s", httpUrl, responseCode)
            }
        })
        return byteArrayOutputStream.toByteArray()
    }

    /**
     * collect from http with cache
     * @param httpUrl
     * @param httpHeaderList
     * @param fileCacheManager
     * @return ByteArrayOutputStream
     */
    fun collectFromHttpWithCache(httpUrl: String, httpHeaderList: List<HttpNameValue> = emptyList(), fileCacheManager: FileCacheManager): ByteArray {
        val filename = httpUrl.replace(Constants.Symbol.SLASH_LEFT, Constants.Symbol.DOLLAR).replace(Constants.Symbol.COLON, Constants.Symbol.AT).replace(Constants.Symbol.QUESTION_MARK, Constants.Symbol.POUND_KEY)
        var byteArray = fileCacheManager.getFromCache(filename, ByteArray::class)
        if (byteArray == null) {
            byteArray = collectFromHttp(httpUrl, httpHeaderList)
            if (byteArray.isNotEmpty()) {
                fileCacheManager.saveToCache(filename, byteArray)
            }
        }
        return byteArray
    }

    class CollectUtilException : RuntimeException {
        constructor(message: String) : super(message)
        constructor(cause: Throwable) : super(cause)
        constructor(message: String, cause: Throwable) : super(message, cause)
    }
}
