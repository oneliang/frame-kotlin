package com.oneliang.ktx.frame.cache

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.file.FileUtil
import com.oneliang.ktx.util.file.createFileIncludeDirectory
import com.oneliang.ktx.util.file.write
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.io.UnsupportedEncodingException
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class FileCacheManager constructor(private var cacheDirectory: String, private val depth: Int = 0, private val cacheRefreshTime: Long = 0L) : CacheManager {
    companion object {
        private val logger = LoggerManager.getLogger(FileCacheManager::class)
    }

    private val oldCacheFullFilenameMap = ConcurrentHashMap<Any, String>()

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
        val formatTime = if (this.cacheRefreshTime <= 0) {
            Constants.String.ZERO
        } else {
            val time = System.currentTimeMillis()
            time.getZeroTime(this.cacheRefreshTime).toUtilDate().toFormatString(Constants.Time.UNION_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
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
        val relativeCacheName = generateCacheRelativeFilename(key, cacheType)
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
     */
    override fun <T : Any> saveToCache(key: Any, value: T) {
        val cacheType = value::class
        val relativeCacheName = generateCacheRelativeFilename(key, cacheType)
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
        //try to delete old cache when new cache does not exist
        val oldCacheFullFilename = this.oldCacheFullFilenameMap[key]
        if (oldCacheFullFilename != null) {
            this.oldCacheFullFilenameMap.remove(key)
            val oldCacheFile = File(oldCacheFullFilename)
            oldCacheFile.delete()
        }
        this.oldCacheFullFilenameMap[key] = cacheFullFilename
    }
}