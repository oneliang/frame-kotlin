package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.util.file.saveTo
import com.oneliang.ktx.util.file.toPropertiesAutoCreate
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.util.*

class ConfigFileStorage(private var directory: String, configFilename: String = "config") {

    companion object {
        private val logger = LoggerManager.getLogger(ConfigFileStorage::class)
    }

    private val configPropertiesFile: File
    private val configProperties: Properties

    init {
        if (this.directory.isBlank()) {
            error("parameter(directory) can not be blank, only can be directory or exists directory.")
        }
        val directoryFile = File(this.directory)
        if (directoryFile.exists() && directoryFile.isFile) {
            error("parameter(directory) can not be exists file, only can be directory or exists directory.")
        }
        this.directory = directoryFile.absolutePath
        logger.info("Config file storage directory:%s", this.directory)
        this.configPropertiesFile = File(this.directory, configFilename)
        this.configProperties = configPropertiesFile.toPropertiesAutoCreate()
    }

    fun setProperty(key: String, value: String) {
        this.configProperties.setProperty(key, value)
    }

    fun getProperty(key: String): String {
        return this.configProperties.getProperty(key)
    }

    fun removeProperty(key: String) {
        this.configProperties.remove(key)
    }

    fun save() {
        this.configProperties.saveTo(this.configPropertiesFile)
    }
}