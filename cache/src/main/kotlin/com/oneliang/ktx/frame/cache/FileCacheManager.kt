package com.oneliang.ktx.frame.cache

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.file.*
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.io.UnsupportedEncodingException
import java.util.*
import kotlin.reflect.KClass

class FileCacheManager constructor(private var cacheDirectory: String, private val depth: Int = 0, private val defaultCacheRefreshTime: Long = 0L) : CacheManager {
    companion object {
        private val logger = LoggerManager.getLogger(FileCacheManager::class)
        private const val CACHE_PROPERTIES_NAME = "cache.txt"
    }

    private var cachePropertiesFile: File
    private var cacheProperties: Properties

    init {
        if (cacheDirectory.isBlank()) {
            error("parameter(cacheDirectory) can not be blank, only can be directory or exists directory.")
        }
        val cacheDirectoryFile = File(this.cacheDirectory)
        if (cacheDirectoryFile.exists() && cacheDirectoryFile.isFile) {
            error("parameter(cacheDirectory) can not be exists file, only can be directory or exists directory.")
        }
        this.cacheDirectory = cacheDirectoryFile.absolutePath
        logger.info("Cache directory:%s", this.cacheDirectory)
        this.cachePropertiesFile = File(this.cacheDirectory, CACHE_PROPERTIES_NAME)
        this.cacheProperties = cachePropertiesFile.toPropertiesAutoCreate()
    }

    /**
     * generate cache relative filename
     */
    private fun <T : Any> generateCacheRelativeFilename(key: Any, cacheType: KClass<T>, cacheRefreshTime: Long): String {
        val keyString = key.toString()
        val keyMd5 = key.toString().MD5String()
        val relativePath = StringBuilder()
        if (this.depth > 0) {
            val interval = keyMd5.length / this.depth
            for (i in 0 until this.depth) {
                relativePath.append(Constants.Symbol.SLASH_LEFT + keyMd5.substring(i * interval, (i + 1) * interval))
            }
        }
        //when cache refresh time is negative, use default cache refresh time
        val fixCacheRefreshTime = if (cacheRefreshTime < 0) this.defaultCacheRefreshTime else cacheRefreshTime
        val formatTime = if (fixCacheRefreshTime <= 0) {
            Constants.String.ZERO
        } else {
            val time = System.currentTimeMillis()
            time.getZeroTime(fixCacheRefreshTime).toUtilDate().toFormatString(Constants.Time.UNION_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        }
        val cacheName = keyString + Constants.Symbol.UNDERLINE + keyMd5 + Constants.Symbol.UNDERLINE + formatTime + Constants.Symbol.UNDERLINE + cacheType.java.simpleName.toLowerCase()
        return relativePath.toString() + Constants.Symbol.SLASH_LEFT + cacheName
    }

    /**
     * get from cache
     * @param key
     * @param cacheType
     * @param cacheRefreshTime
     * @return T
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getFromCache(key: Any, cacheType: KClass<T>, cacheRefreshTime: Long): T? {
        val relativeCacheName = generateCacheRelativeFilename(key, cacheType, cacheRefreshTime)
        val cacheFullFilename = this.cacheDirectory + relativeCacheName
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
            else -> logger.error("get from cache unsupport the class:%s", cacheType)
        }
        return null
    }

    /**
     * save to cache
     * @param key
     * @param value
     * @param cacheRefreshTime
     */
    override fun <T : Any> saveToCache(key: Any, value: T, cacheRefreshTime: Long) {
        //first try to delete old cache, maybe old and new cache file is the same
        val keyString = key.toString()
        val oldCacheFullFilename = this.cacheProperties.getProperty(keyString)
        if (!oldCacheFullFilename.isNullOrBlank()) {
            this.cacheProperties.remove(keyString)
            val oldCacheFile = File(oldCacheFullFilename)
            oldCacheFile.delete()
        }
        //save new cache
        val cacheType = value::class
        val relativeCacheName = generateCacheRelativeFilename(key, cacheType, cacheRefreshTime)
        val cacheFullFilename = this.cacheDirectory + relativeCacheName
        val cacheFile = cacheFullFilename.toFile()
        cacheFile.createFileIncludeDirectory()
        when (cacheType) {
            String::class -> {
                try {
                    cacheFile.write((value as String).toByteArray())
                } catch (e: UnsupportedEncodingException) {
                    logger.error(Constants.Base.EXCEPTION, e)
                }
            }
            ByteArray::class -> {
                cacheFile.write(value as ByteArray)
            }
            else -> logger.error("save to cache unsupport the class:%s", cacheType)
        }
        //update new cache
        this.cacheProperties.setProperty(keyString, cacheFullFilename)
        this.cacheProperties.saveTo(this.cachePropertiesFile)
    }
}