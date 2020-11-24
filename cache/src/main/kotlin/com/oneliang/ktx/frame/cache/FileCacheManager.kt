package com.oneliang.ktx.frame.cache

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.file.FileUtil
import com.oneliang.ktx.util.file.createFileIncludeDirectory
import com.oneliang.ktx.util.file.write
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
    private val formatTime: Pair<String, String>
        get() {
            if (this.cacheRefreshCycle == CacheRefreshCycle.NONE) {
                return this.cacheRefreshCycle.timeFormat to this.cacheRefreshCycle.timeFormat
            } else if (this.cacheRefreshCycle == CacheRefreshCycle.DAY) {
                val currentDate = Date()
                val previousDateString = currentDate.getDayZeroTimePrevious(1).toUtilDate().toFormatString(this.cacheRefreshCycle.timeFormat)
                val currentDateString = currentDate.toFormatString(this.cacheRefreshCycle.timeFormat)
                return previousDateString to currentDateString
            } else {//hour
                val currentDate = Date()
                val previousDateString = currentDate.getHourZeroTimePrevious(1).toUtilDate().toFormatString(this.cacheRefreshCycle.timeFormat)
                val currentDateString = currentDate.toFormatString(this.cacheRefreshCycle.timeFormat)
                return previousDateString to currentDateString
            }
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
        logger.info("Cache directory:%s", this.cacheDirectory)
    }

    /**
     * generate cache relative filename
     */
    private fun <T : Any> generateCacheRelativeFilename(key: Any, cacheType: KClass<T>): Pair<String, String> {
        val keyString = key.toString()
        val keyMd5 = key.toString().MD5String()
        val relativePath = StringBuilder()
        if (this.depth > 0) {
            val interval = keyMd5.length / this.depth
            for (i in 0 until this.depth) {
                relativePath.append(Constants.Symbol.SLASH_LEFT + keyMd5.substring(i * interval, (i + 1) * interval))
            }
        }

        val oldCacheName = keyString + Constants.Symbol.UNDERLINE + keyMd5 + Constants.Symbol.UNDERLINE + formatTime.first + Constants.Symbol.UNDERLINE + cacheType.java.simpleName.toLowerCase()
        val newCacheName = keyString + Constants.Symbol.UNDERLINE + keyMd5 + Constants.Symbol.UNDERLINE + formatTime.second + Constants.Symbol.UNDERLINE + cacheType.java.simpleName.toLowerCase()
        val oldRelativeCacheName = relativePath.toString() + Constants.Symbol.SLASH_LEFT + oldCacheName
        val newRelativeCacheName = relativePath.toString() + Constants.Symbol.SLASH_LEFT + newCacheName
        return oldRelativeCacheName to newRelativeCacheName
    }

    /**
     * get from cache
     * @param key
     * @param cacheType
     * @return T
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getFromCache(key: Any, cacheType: KClass<T>): T? {
        val relativeCacheName = generateCacheRelativeFilename(key, cacheType)
        val oldCacheFullFilename = this.cacheDirectory + relativeCacheName.first
        val newCacheFullFilename = this.cacheDirectory + relativeCacheName.second
        val newCacheFile = File(newCacheFullFilename)
        if (!newCacheFile.exists() || newCacheFile.length() == 0L) {
            //try to delete old cache when new cache does not exist
            val oldCacheFile = File(oldCacheFullFilename)
            oldCacheFile.delete()
            return null
        }
        when (cacheType) {
            String::class -> {
                try {
                    return String(FileUtil.readFile(newCacheFullFilename), Charsets.UTF_8) as T
                } catch (e: UnsupportedEncodingException) {
                    logger.error(Constants.Base.EXCEPTION, e)
                }
            }
            ByteArray::class -> {
                return FileUtil.readFile(newCacheFullFilename) as T
            }
            else -> logger.error("get from cache unsupport the class:%s", cacheType)
        }
        return null
    }

    /**
     * save to cache
     * @param key
     * @param value
     */
    override fun <T : Any> saveToCache(key: Any, value: T) {
        val cacheType = value::class
        val relativeCacheName = generateCacheRelativeFilename(key, cacheType)
        val newCacheFullFilename = this.cacheDirectory + relativeCacheName.second
        val newCacheFile = newCacheFullFilename.toFile()
        newCacheFile.createFileIncludeDirectory()
        when (cacheType) {
            String::class -> {
                try {
                    newCacheFile.write((value as String).toByteArray())
                } catch (e: UnsupportedEncodingException) {
                    logger.error(Constants.Base.EXCEPTION, e)
                }
            }
            ByteArray::class -> {
                newCacheFile.write(value as ByteArray)
            }
            else -> logger.error("save to cache unsupport the class:%s", cacheType)
        }
    }

    enum class CacheRefreshCycle(internal val timeFormat: String) {
        NONE(Constants.String.ZERO), DAY("yyyyMMdd"), HOUR("yyyyMMddHH")
    }
}