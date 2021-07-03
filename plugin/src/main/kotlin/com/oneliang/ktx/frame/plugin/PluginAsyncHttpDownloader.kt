package com.oneliang.ktx.frame.plugin

import com.oneliang.ktx.frame.downloader.AsyncHttpDownloader
import com.oneliang.ktx.frame.downloader.DefaultDownloadListener

class PluginAsyncHttpDownloader : PluginDownloader {
    private var asyncHttpDownloader: AsyncHttpDownloader? = AsyncHttpDownloader(1, Runtime.getRuntime().availableProcessors())

    init {
        this.asyncHttpDownloader?.start()
    }

    fun interrupt() {
        this.asyncHttpDownloader?.interrupt()
        this.asyncHttpDownloader = null
    }

    override fun download(pluginFileBean: PluginFileBean) {
        val httpUrl = pluginFileBean.url
        val saveFullFilename = pluginFileBean.saveFullFilename
        this.asyncHttpDownloader?.download(httpUrl, saveFullFilename = saveFullFilename, downloadListener = object : DefaultDownloadListener() {
            override fun onFinish() {
                super.onFinish()
                pluginFileBean.isFinished = true
                val onLoadedListener = pluginFileBean.onLoadedListener
                onLoadedListener?.onLoaded(pluginFileBean)
            }

            override fun onFailure(throwable: Throwable) {
                super.onFailure(throwable)
                onFinish()
            }
        })
    }
}