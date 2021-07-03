package com.oneliang.ktx.frame.downloader

import com.oneliang.ktx.util.concurrent.ThreadPool
import com.oneliang.ktx.util.http.HttpUtil

class AsyncHttpDownloader(private val minThreads: Int, private val maxThreads: Int) {

    companion object {
        private const val DEFAULT_MIN_THREADS = 1
        private const val DEFAULT_TIMEOUT = 20000
        private val httpDownloader = HttpDownloader()
    }

    private var threadPool: ThreadPool = ThreadPool()

    init {
        this.threadPool.minThreads = if (minThreads <= 0) DEFAULT_MIN_THREADS else minThreads
        this.threadPool.maxThreads = if (maxThreads < minThreads) minThreads else maxThreads
    }


    /**
     * start
     */
    fun start() {
        this.threadPool.start()
    }

    /**
     * interrupt
     */
    fun interrupt() {
        this.threadPool.interrupt()
    }

    /**
     * download
     * @param httpUrl
     * @param httpHeaderList
     * @param httpParameterList
     * @param timeout
     * @param saveFullFilename full file
     * @param downloadListener
     */
    fun download(httpUrl: String, httpHeaderList: List<HttpUtil.HttpNameValue> = emptyList(), httpParameterList: List<HttpUtil.HttpNameValue> = emptyList(), timeout: Int = DEFAULT_TIMEOUT, saveFullFilename: String, downloadListener: HttpDownloader.DownloadListener = HttpDownloader.DEFAULT_DOWNLOAD_LISTENER) {
        this.threadPool.addThreadTask {
            httpDownloader.download(httpUrl, httpHeaderList, httpParameterList, timeout, saveFullFilename, downloadListener)
        }
    }
}