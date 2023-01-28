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
     * @param method
     * @param httpHeaderList
     * @param requestByteArray
     * @param advancedOption
     * @return ByteArrayOutputStream
     */
    private fun collectFromHttp(httpUrl: String, method: String = Constants.Http.RequestMethod.GET.value, httpHeaderList: List<HttpNameValue> = emptyList(), requestByteArray: ByteArray = ByteArray(0), advancedOption: AdvancedOption? = null): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val callback = object : Callback {
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
                logger.error(Constants.String.EXCEPTION, throwable)
            }

            @Throws(Exception::class)
            override fun httpNotOkCallback(responseCode: Int, headerFieldMap: Map<String, List<String>>, errorInputStream: InputStream?) {
                logger.error("Response not ok, http:%s, response code:%s", httpUrl, responseCode)
            }
        }
        if (method == Constants.Http.RequestMethod.GET.value) {
            HttpUtil.sendRequestGet(httpUrl = httpUrl, httpHeaderList = httpHeaderList, advancedOption = advancedOption, callback = callback)
        } else {
            HttpUtil.sendRequestPostWithBytes(httpUrl = httpUrl, httpHeaderList = httpHeaderList, byteArray = requestByteArray, advancedOption = advancedOption, callback = callback)
        }
        return byteArrayOutputStream.toByteArray()
    }

    /**
     * collect from http with cache
     * @param httpUrl
     * @param cacheKey keep the same cache key can delete old cache
     * @param httpHeaderList
     * @param requestByteArray
     * @param fileCacheManager
     * @param cacheRefreshTime
     * @return ByteArrayOutputStream
     */
    fun collectFromHttpWithCache(httpUrl: String, method: String = Constants.Http.RequestMethod.GET.value, cacheKey: String = Constants.String.BLANK, httpHeaderList: List<HttpNameValue> = emptyList(), requestByteArray: ByteArray = ByteArray(0), fileCacheManager: FileCacheManager? = null, cacheRefreshTime: Long = -1L): ByteArray {
        val newCacheKey = cacheKey.ifBlank {
            httpUrl.replace(Constants.Symbol.SLASH_LEFT, Constants.Symbol.DOLLAR).replace(Constants.Symbol.COLON, Constants.Symbol.AT).replace(Constants.Symbol.QUESTION_MARK, Constants.Symbol.POUND)
        }
        var cacheByteArray = fileCacheManager?.getFromCache(newCacheKey, ByteArray::class, cacheRefreshTime)
        if (cacheByteArray == null) {
            logger.debug("collect from http:%s", httpUrl)
            cacheByteArray = collectFromHttp(httpUrl, method, httpHeaderList, requestByteArray)
            if (cacheByteArray.isNotEmpty()) {
                try {
                    fileCacheManager?.saveToCache(newCacheKey, cacheByteArray, cacheRefreshTime)
                } catch (throwable: Throwable) {
                    logger.error("save cache error, cache key:%s", throwable, newCacheKey)
                }
            }
        } else {
            logger.debug("collect from cache, cache key:%s", newCacheKey)
        }
        return cacheByteArray
    }

    /**
     * collect from http with cache
     * use for collect rule
     * @return List<CollectData>
     */
    fun collectFromHttpWithCache(httpUrl: String, method: String = Constants.Http.RequestMethod.GET.value, cacheKey: String = Constants.String.BLANK, httpHeaderList: List<HttpNameValue> = emptyList(), requestByteArray: ByteArray = ByteArray(0), fileCacheManager: FileCacheManager? = null, cacheRefreshTime: Long = -1L, collectRuleList: List<CollectRule> = emptyList()): List<CollectData> {
        return this.collectFromHttpWithCache(httpUrl, method, cacheKey, httpHeaderList, requestByteArray, fileCacheManager, cacheRefreshTime, collectRuleList, object : CollectDataTransformer<List<CollectData>> {
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
    fun <T> collectFromHttpWithCache(
        httpUrl: String,
        method: String = Constants.Http.RequestMethod.GET.value,
        cacheKey: String = Constants.String.BLANK,
        httpHeaderList: List<HttpNameValue> = emptyList(),
        requestByteArray: ByteArray = ByteArray(0),
        fileCacheManager: FileCacheManager? = null,
        cacheRefreshTime: Long = -1L,
        collectRuleList: List<CollectRule> = emptyList(),
        collectDataTransformer: CollectDataTransformer<T>
    ): T {
        logger.info("collecting http url:%s", httpUrl)
        val responseByteArray = collectFromHttpWithCache(httpUrl, method, cacheKey, httpHeaderList, requestByteArray, fileCacheManager, cacheRefreshTime)
        val responseString = String(responseByteArray)
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
            CollectRule.Type.CONTENT.value -> {
                listOf(content)
            }
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

    class CollectRule(val type: Int = Type.CONTENT.value) {
        enum class Type(val value: Int) {
            CONTENT(0), REGEX(1), XPATH(2)
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

