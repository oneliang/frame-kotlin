package com.oneliang.ktx.frame.downloader

import com.oneliang.ktx.util.http.HttpUtil
import com.oneliang.ktx.util.http.HttpUtil.Callback
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.InputStream

class HttpDownloader {

    companion object {
        private val logger: Logger = LoggerManager.getLogger(HttpDownloader::class)
        val DEFAULT_DOWNLOAD_LISTENER: DownloadListener = DefaultDownloadListener()
    }

    /**
     * download
     * @param httpUrl
     * @param httpHeaderList
     * @param httpParameterList
     * @param timeout
     * @param saveFullFilename
     * @param downloadListener
     */
    fun download(httpUrl: String, httpHeaderList: List<HttpUtil.HttpNameValue>, httpParameterList: List<HttpUtil.HttpNameValue>, timeout: Int, saveFullFilename: String, downloadListener: DownloadListener = DEFAULT_DOWNLOAD_LISTENER) {
        try {
            downloadListener.onStart()
            HttpUtil.sendRequestPost(httpUrl, httpHeaderList, httpParameterList, timeout, null, object : Callback {

                override fun httpOkCallback(headerFieldMap: Map<String, List<String>>, inputStream: InputStream, contentLength: Int) {
                    downloadListener.onProcess(headerFieldMap, inputStream, contentLength, saveFullFilename)
                    downloadListener.onFinish()
                }

                override fun exceptionCallback(throwable: Throwable) {
                    downloadListener.onFailure(throwable)
                }

                override fun httpNotOkCallback(responseCode: Int, headerFieldMap: Map<String, List<String>>, errorInputStream: InputStream?) {
                    logger.debug("Response code:%s", responseCode)
                }
            })
        } catch (e: Exception) {
            downloadListener.onFailure(e)
        }
    }

    interface DownloadListener {

        fun onStart()

        fun onProcess(headerFieldMap: Map<String, List<String>>, inputStream: InputStream, contentLength: Int, saveFullFilename: String)

        fun onFinish()

        fun onFailure(throwable: Throwable)
    }
}