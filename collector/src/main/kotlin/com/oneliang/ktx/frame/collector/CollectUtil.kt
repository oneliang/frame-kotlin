package com.oneliang.ktx.frame.collector

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.cache.FileCacheManager
import com.oneliang.ktx.util.common.parseRegexGroup
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
     * @param cacheKey keep the same cache key can delete old cache
     * @param httpHeaderList
     * @param fileCacheManager
     * @return ByteArrayOutputStream
     */
    fun collectFromHttpWithCache(httpUrl: String, cacheKey: String = Constants.String.BLANK, httpHeaderList: List<HttpNameValue> = emptyList(), fileCacheManager: FileCacheManager? = null): ByteArray {
        val filename = httpUrl.replace(Constants.Symbol.SLASH_LEFT, Constants.Symbol.DOLLAR).replace(Constants.Symbol.COLON, Constants.Symbol.AT).replace(Constants.Symbol.QUESTION_MARK, Constants.Symbol.POUND_KEY)
        val newCacheKey = if (cacheKey.isBlank()) filename else cacheKey
        var byteArray = fileCacheManager?.getFromCache(newCacheKey, ByteArray::class)
        if (byteArray == null) {
            logger.debug("collect from http:%s", httpUrl)
            byteArray = collectFromHttp(httpUrl, httpHeaderList)
            if (byteArray.isNotEmpty()) {
                fileCacheManager?.saveToCache(newCacheKey, byteArray)
            }
        } else {
            logger.debug("collect from cache, cache key:%s", newCacheKey)
        }
        return byteArray
    }

    /**
     * collect from http with cache
     * use for collect rule
     * @return List<CollectData>
     */
    fun collectFromHttpWithCache(httpUrl: String, cacheKey: String = Constants.String.BLANK, httpHeaderList: List<HttpNameValue> = emptyList(), fileCacheManager: FileCacheManager? = null, collectRuleList: List<CollectRule> = emptyList()): List<CollectData> {
        return this.collectFromHttpWithCache(httpUrl, cacheKey, httpHeaderList, fileCacheManager, collectRuleList, object : CollectDataTransformer<List<CollectData>> {
            override fun transform(collectDataList: List<CollectData>): List<CollectData> {
                return collectDataList
            }
        })
    }

    /**
     * collect from http with cache
     * use for collect rule
     * @return T
     */
    fun <T> collectFromHttpWithCache(httpUrl: String, cacheKey: String = Constants.String.BLANK, httpHeaderList: List<HttpNameValue> = emptyList(), fileCacheManager: FileCacheManager? = null, collectRuleList: List<CollectRule> = emptyList(), collectDataTransformer: CollectDataTransformer<T>): T {
        logger.info("collecting http url:%s", httpUrl)
        val byteArray = collectFromHttpWithCache(httpUrl, cacheKey, httpHeaderList, fileCacheManager)
        val responseString = String(byteArray)
        val collectDataList = mutableListOf<CollectData>()
        for (collectRule in collectRuleList) {
            val collectData = CollectData()
            collectByCollectRule(responseString, collectRule, collectData)
            collectDataList += collectData
        }
        return collectDataTransformer.transform(collectDataList)
    }

    private fun collectByCollectRule(content: String, collectRule: CollectRule, collectData: CollectData) {
        if (collectRule.includeOriginalData) {
            collectData.originalData = content
        }
        val list = when (collectRule.type) {
            CollectRule.Type.REGEX.value -> {
                parseByRegex(content, collectRule.rule)
            }
            CollectRule.Type.XPATH.value -> {
                parseByXPath(content)
            }
            else -> {
                emptyList()
            }
        }
        collectData.resultList = list
        collectData.resultInstance = collectRule.resultTransformer?.transform(collectData.resultList)
        val innerCollectRule = collectRule.innerCollectRule
        if (innerCollectRule != null) {
            list.forEach {
                val resultCollectData = CollectData()
                collectData.resultCollectDataList += resultCollectData
                collectByCollectRule(it, innerCollectRule, resultCollectData)
            }
        }
    }

    private fun parseByRegex(content: String, regex: String): List<String> {
        return content.parseRegexGroup(regex)
    }

    private fun parseByXPath(content: String): List<String> {
        return emptyList()
    }

    class CollectUtilException : RuntimeException {
        constructor(message: String) : super(message)
        constructor(cause: Throwable) : super(cause)
        constructor(message: String, cause: Throwable) : super(message, cause)
    }

    class CollectRule(val type: Int = Type.REGEX.value) {
        enum class Type(val value: Int) {
            REGEX(0), XPATH(1)
        }

        var rule = Constants.String.BLANK
        var includeOriginalData = true
        var innerCollectRule: CollectRule? = null
        var resultTransformer: ResultTransformer<*>? = null
    }

    class CollectData {
        var originalData = Constants.String.BLANK
        var resultList = emptyList<String>()
        var resultInstance: Any? = null
        val resultCollectDataList = mutableListOf<CollectData>()
    }

    interface ResultTransformer<T> {
        fun transform(resultList: List<String>): T
    }

    interface CollectDataTransformer<T> {
        fun transform(collectDataList: List<CollectData>): T
    }
}

