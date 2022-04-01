package com.oneliang.ktx.frame.plugin

import com.oneliang.ktx.frame.downloader.AsyncHttpDownloader
import com.oneliang.ktx.frame.downloader.DefaultDownloadListener

class PluginAsyncHttpDownloader : PluginDownloader {
    private var asyncHttpDownloader: AsyncHttpDownloader? = AsyncHttpDownloader(1, Runtime.getRuntime().availableProcessors())

    fun start() {
        this.asyncHttpDownloader?.start()
    }

    fun stop() {
        this.asyncHttpDownloader?.stop()
        this.asyncHttpDownloader = null
    }

    override fun download(pluginFile: PluginFile) {
        val httpUrl = pluginFile.url
        val saveFullFilename = pluginFile.saveFullFilename
        this.asyncHttpDownloader?.download(httpUrl, saveFullFilename = saveFullFilename, downloadListener = object : DefaultDownloadListener() {
            override fun onFinish() {
                super.onFinish()
                pluginFile.isFinished = true
                val onLoadedListener = pluginFile.onLoadedListener
                onLoadedListener?.onLoaded(pluginFile)
            }

            override fun onFailure(throwable: Throwable) {
                super.onFailure(throwable)
                onFinish()
            }
        })
    }
}