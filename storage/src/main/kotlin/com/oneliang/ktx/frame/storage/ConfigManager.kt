package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.KotlinClassUtil
import com.oneliang.ktx.util.common.pullFromMappableObject
import com.oneliang.ktx.util.common.pushToMappableObject
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ConfigManager(interval: Long = 10_000L) {

    private val configMap = ConcurrentHashMap<Any, KeyValueStorage>()
    private val timer = Timer()

    init {
        this.timer.schedule(object : TimerTask() {
            override fun run() {
                this@ConfigManager.configMap.forEach { (instance, configStorage) ->
                    writeConfig(instance)
                }
            }
        }, 0, interval)
    }

    /**
     * read config
     * @param instance
     * @param fullFilename
     */
    fun <T : Any> readConfig(instance: T, fullFilename: String) {
        if (this.configMap.containsKey(instance)) {
            return
        }
        val keyValueStorage = KeyValueStorage(fullFilename)
        this.configMap[instance] = keyValueStorage
        instance.pushToMappableObject { mappableKey, fieldType ->
            val mappableValue = keyValueStorage.getProperty(mappableKey)
            KotlinClassUtil.changeType(fieldType, arrayOf(mappableValue))
        }
    }

    /**
     * write config
     * @param instance
     * @param fullFilename
     */
    fun <T : Any> writeConfig(instance: T, fullFilename: String = Constants.String.BLANK) {
        if (!this.configMap.containsKey(instance)) {
            error("This instance has not read the config from file, instance:%s".format(instance))
        }
        val configStorage = this.configMap[instance]
        instance.pullFromMappableObject { mappableKey, fieldValue ->
            configStorage?.setProperty(mappableKey, fieldValue.toString())
        }
        if (fullFilename.isBlank()) {
            configStorage?.save()
        } else {
            configStorage?.saveTo(fullFilename)
        }
    }
}