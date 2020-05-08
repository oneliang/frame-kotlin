package com.oneliang.ktx.frame.collector

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.file.FileUtil
import com.oneliang.ktx.util.http.HttpUtil
import com.oneliang.ktx.util.http.HttpUtil.AdvancedOption
import com.oneliang.ktx.util.http.HttpUtil.Callback
import com.oneliang.ktx.util.http.HttpUtil.HttpNameValue
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream

object CollectUtil {
    private val logger = LoggerManager.getLogger(CollectUtil::class)
    /**
     * collect from http
     *
     * @param httpUrl
     * @param httpHeaderList
     * @param advancedOption
     * @return ByteArrayOutputStream
     */
    fun collectFromHttp(httpUrl: String, httpHeaderList: List<HttpNameValue> = emptyList(), advancedOption: AdvancedOption? = null): ByteArrayOutputStream {
        val byteArrayOutputStream = ByteArrayOutputStream()
        HttpUtil.sendRequestGet(httpUrl = httpUrl, httpHeaderList = httpHeaderList, advancedOption = advancedOption, callback = object : Callback {
            @Throws(Exception::class)
            override fun httpOkCallback(headerFieldMap: Map<String, List<String>>, inputStream: InputStream, contentLength: Int) {
                var needToUnzip = false
                if (headerFieldMap.containsKey(Constants.Http.HeaderKey.CONTENT_ENCODING)) {
                    needToUnzip = headerFieldMap[Constants.Http.HeaderKey.CONTENT_ENCODING]?.contains(Constants.CompressType.GZIP) ?: false
                }
                var newInputStream = inputStream
                if (needToUnzip) {
                    newInputStream = GZIPInputStream(inputStream)
                }
                byteArrayOutputStream.use {
                    FileUtil.copyStream(newInputStream, it)
                }
            }

            override fun exceptionCallback(throwable: Throwable) {
                throwable.printStackTrace()
                logger.error(Constants.Base.EXCEPTION, throwable)
            }

            @Throws(Exception::class)
            override fun httpNotOkCallback(responseCode: Int, headerFieldMap: Map<String, List<String>>, errorInputStream: InputStream?) {
                logger.error(String.format("response not ok, http:%s, response code:%s", httpUrl, responseCode))
            }
        })
        return byteArrayOutputStream
    }

    /**
     * collect from http with cache
     *
     * @param httpUrl
     * @param httpHeaderList
     * @param cacheDirectory
     * @return ByteArrayOutputStream
     */
    fun collectFromHttpWithCache(httpUrl: String, httpHeaderList: List<HttpNameValue> = emptyList(), cacheDirectory: String): ByteArrayOutputStream {
        val byteArrayOutputStream: ByteArrayOutputStream
        val filename = httpUrl.replace(Constants.Symbol.SLASH_LEFT, Constants.Symbol.DOLLAR).replace(Constants.Symbol.COLON, Constants.Symbol.AT).replace(Constants.Symbol.QUESTION_MARK, "#")
        val fullFilename = cacheDirectory + "/" + filename + Constants.File.TXT
        if (FileUtil.isExist(fullFilename)) {
            byteArrayOutputStream = collectFromLocal(fullFilename)
        } else {
            byteArrayOutputStream = collectFromHttp(httpUrl, httpHeaderList)
            val byteArray = byteArrayOutputStream.toByteArray()
            if (byteArray.isNotEmpty()) {
                FileUtil.writeFile(fullFilename, byteArray)
            }
        }
        return byteArrayOutputStream
    }

    /**
     * collect from local
     *
     * @param fullFilename
     * @return ByteArrayOutputStream
     */
    fun collectFromLocal(fullFilename: String): ByteArrayOutputStream {
        val byteArrayOutputStream = ByteArrayOutputStream()
        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = FileInputStream(fullFilename)
            val buffer = ByteArray(Constants.Capacity.BYTES_PER_KB)
            var dataLength = fileInputStream.read(buffer, 0, buffer.size)
            while (dataLength != -1) {
                byteArrayOutputStream.write(buffer, 0, dataLength)
                byteArrayOutputStream.flush()
                dataLength = fileInputStream.read(buffer, 0, buffer.size)
            }
        } catch (e: Exception) {
            throw CollectUtilException(fullFilename, e)
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close()
                }
                byteArrayOutputStream.close()
            } catch (e: Exception) {
                throw CollectUtilException(fullFilename, e)
            }
        }
        return byteArrayOutputStream
    }

    class CollectUtilException : RuntimeException {
        constructor(message: String) : super(message)
        constructor(cause: Throwable) : super(cause)
        constructor(message: String, cause: Throwable) : super(message, cause)
    }
}
