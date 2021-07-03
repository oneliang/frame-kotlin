package com.oneliang.ktx.frame.downloader

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.readWithBuffer
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * @author oneliang
 */
open class DefaultDownloadListener : HttpDownloader.DownloadListener {

    companion object {
        private val logger: Logger = LoggerManager.getLogger(DefaultDownloadListener::class)
    }

    override fun onStart() {
        logger.debug("download on start")
    }

    override fun onProcess(headerFieldMap: Map<String, List<String>>, inputStream: InputStream, contentLength: Int, saveFullFilename: String) {
        try {
            val file = File(saveFullFilename)
            file.createNewFile()
            val fileOutputStream = FileOutputStream(file)
            inputStream.readWithBuffer(Constants.Capacity.BYTES_PER_KB, fileOutputStream)
            fileOutputStream.close()
        } catch (exception: Exception) {
            onFailure(exception)
        }
    }

    override fun onFinish() {
        logger.debug("download on finish")
    }

    override fun onFailure(throwable: Throwable) {
        logger.error(Constants.String.EXCEPTION, throwable)
    }
}