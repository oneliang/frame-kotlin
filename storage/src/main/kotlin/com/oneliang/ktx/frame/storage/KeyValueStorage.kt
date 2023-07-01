package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.file.saveTo
import com.oneliang.ktx.util.file.toPropertiesAutoCreate
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.util.*

class KeyValueStorage(
    fullFilename: String
) {

    companion object {
        private val logger = LoggerManager.getLogger(KeyValueStorage::class)
    }

    private val keyValuePropertiesFile: File
    private val keyValueProperties: Properties

    init {
        if (fullFilename.isBlank()) {
            error("parameter(fullFilename) can not be blank, only can be directory or exists directory.")
        }
        logger.info("Config file path:%s", fullFilename)
        this.keyValuePropertiesFile = File(fullFilename)
        this.keyValueProperties = keyValuePropertiesFile.toPropertiesAutoCreate()
    }

    /**
     * has property
     * @param key
     * @return Boolean
     */
    fun hasProperty(key: String): Boolean {
        return this.keyValueProperties.containsKey(key)
    }

    /**
     * set property
     * @param key
     * @param value
     * @param saveImmediately
     */
    fun setProperty(key: String, value: String, saveImmediately: Boolean = false) {
        this.keyValueProperties.setProperty(key, value)
        if (saveImmediately) {
            this.save()
        }
    }

    /**
     * get property
     * @param key
     * @return String
     */
    fun getProperty(key: String): String {
        return this.keyValueProperties.getProperty(key).nullToBlank()
    }

    /**
     * remove property
     * @param key
     */
    fun removeProperty(key: String) {
        this.keyValueProperties.remove(key)
    }

    /**
     * save
     */
    fun save() {
        this.keyValueProperties.saveTo(this.keyValuePropertiesFile)
    }

    /**
     * save to, can use to copy
     * @param fullFilename
     */
    fun saveTo(fullFilename: String) {
        this.keyValueProperties.saveTo(fullFilename)
    }
}