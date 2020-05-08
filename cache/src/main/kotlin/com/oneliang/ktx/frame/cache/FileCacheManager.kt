package com.oneliang.ktx.frame.cache

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.MD5String
import com.oneliang.ktx.util.common.toFormatString
import com.oneliang.ktx.util.file.FileUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.io.UnsupportedEncodingException
import java.util.*
import kotlin.reflect.KClass

class FileCacheManager constructor(private var cacheDirectory: String, private val depth: Int = 0, private var cacheRefreshCycle: CacheRefreshCycle = CacheRefreshCycle.NONE) : CacheManager {
    companion object {
        private val logger = LoggerManager.getLogger(FileCacheManager::class)
    }

    /**
     * get format time
     * @return String
     */
    private val formatTime: String
        get() {
            if (this.cacheRefreshCycle == CacheRefreshCycle.NONE) {
                return this.cacheRefreshCycle.timeFormat
            }
            return Date().toFormatString(this.cacheRefreshCycle.timeFormat)
        }

    init {
        if (cacheDirectory.isBlank()) {
            error("parameter(cacheDirectory) can not be blank, only can be directory or exists directory.")
        }
        val cacheDirectoryFile = File(cacheDirectory)
        if (cacheDirectoryFile.exists() && cacheDirectoryFile.isFile) {
            error("parameter(cacheDirectory) can not be exists file, only can be directory or exists directory.")
        }
        this.cacheDirectory = cacheDirectoryFile.absolutePath
        logger.info(String.format("Cache directory:%s", this.cacheDirectory))
    }

    /**
     * generate cache relative filename
     */
    private fun <T : Any> generateCacheRelativeFilename(key: Any, cacheType: KClass<T>): String {
        val keyString = key.toString()
        val keyMd5 = key.toString().MD5String()
        val relativePath = StringBuilder()
        if (this.depth > 0) {
            val interval = keyMd5.length / this.depth
            for (i in 0 until this.depth) {
                relativePath.append(Constants.Symbol.SLASH_LEFT + keyMd5.substring(i * interval, (i + 1) * interval))
            }
        }
        val cacheName = keyString + Constants.Symbol.UNDERLINE + keyMd5 + Constants.Symbol.UNDERLINE + formatTime + Constants.Symbol.UNDERLINE + cacheType.java.simpleName.toLowerCase()
        return relativePath.toString() + Constants.Symbol.SLASH_LEFT + cacheName
    }

    /**
     * get from cache
     * @param key
     * @param cacheType
     * @return T
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getFromCache(key: Any, cacheType: KClass<T>): T? {
        val cacheFullFilename = this.cacheDirectory + generateCacheRelativeFilename(key, cacheType)
        val cacheFile = File(cacheFullFilename)
        if (!cacheFile.exists() || cacheFile.length() == 0L) {
            return null
        }
        when (cacheType) {
            String::class -> {
                try {
                    return String(FileUtil.readFile(cacheFullFilename), Charsets.UTF_8) as T
                } catch (e: UnsupportedEncodingException) {
                    logger.error(Constants.Base.EXCEPTION, e)
                }
            }
            ByteArray::class -> {
                return FileUtil.readFile(cacheFullFilename) as T
            }
            else -> logger.error(String.format("get from cache unsupport the class:%s", cacheType))
        }
        return null
    }

    /**
     * save to cache
     * @param key
     * @param value
     */
    override fun <T : Any> saveToCache(key: Any, value: T) {
        val valueKClass = value::class
        val cacheFullFilename = this.cacheDirectory + generateCacheRelativeFilename(key, valueKClass)
        FileUtil.createFile(cacheFullFilename)
        when (valueKClass) {
            String::class -> {
                try {
                    FileUtil.writeFile(cacheFullFilename, (value as String).toByteArray(Charsets.UTF_8))
                } catch (e: UnsupportedEncodingException) {
                    logger.error(Constants.Base.EXCEPTION, e)
                }
            }
            ByteArray::class -> {
                FileUtil.writeFile(cacheFullFilename, value as ByteArray)
            }
            else -> logger.error(String.format("save to cache unsupport the class:%s", valueKClass))
        }
    }

    enum class CacheRefreshCycle(internal val timeFormat: String) {
        NONE(Constants.String.ZERO), DAY("yyyyMMdd"), HOUR("yyyyMMddHH")
    }
}