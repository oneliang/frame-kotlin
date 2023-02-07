package com.oneliang.ktx.frame.search

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.storage.ConfigFileStorage
import com.oneliang.ktx.frame.storage.Storage
import com.oneliang.ktx.util.common.MD5String
import com.oneliang.ktx.util.common.calculateCompose
import com.oneliang.ktx.util.common.calculatePermutation
import com.oneliang.ktx.util.common.toHexString
import com.oneliang.ktx.util.concurrent.ResourceQueueThread
import com.oneliang.ktx.util.concurrent.atomic.LRUCacheMap
import com.oneliang.ktx.util.file.saveTo
import com.oneliang.ktx.util.file.toPropertiesAutoCreate
import com.oneliang.ktx.util.json.jsonToObject
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.util.*

class FileStorage(private var directory: String, private val modules: Array<Int> = arrayOf(100), private val cacheMaxSize: Int = 0) : Storage {

    companion object {
        private val logger = LoggerManager.getLogger(FileStorage::class)
        private const val CONFIG_PROPERTIES_NAME = "config"
        private const val CONFIG_PROPERTIES_KEY_MODULES = "modules"
    }

    init {
        val directoryFile = File(this.directory)
        if (directoryFile.exists() && directoryFile.isFile) {
            error("parameter(cacheDirectory) can not be exists file, only can be directory or exists directory.")
        }
        this.directory = directoryFile.absolutePath
        logger.info("File storage directory:%s", this.directory)
    }

    private val configFileStorage = ConfigFileStorage(this.directory, CONFIG_PROPERTIES_NAME)
    private val autoSaveThread = ResourceQueueThread(object : ResourceQueueThread.ResourceProcessor<AutoSaveItem> {
        override fun process(resource: AutoSaveItem) {
            resource.properties.saveTo(resource.file)
        }
    })

    init {
        if (this.modules.isEmpty()) {
            error("field modules can not be empty")
        }
        this.configFileStorage.setProperty(CONFIG_PROPERTIES_KEY_MODULES, this.modules.toJson())
        this.configFileStorage.save()
    }

    private val keyPropertiesMap = LRUCacheMap<String, PropertiesItem>(this.cacheMaxSize)
    private val valuePropertiesMap = LRUCacheMap<String, PropertiesItem>(this.cacheMaxSize)

    fun initialize() {
        this.autoSaveThread.start()
    }

    fun destroy() {
        this.keyPropertiesMap.clear()
        this.valuePropertiesMap.clear()
        this.autoSaveThread.stop()
    }

    private fun saveProperties(file: File, properties: Properties) {
        this.autoSaveThread.addResource(AutoSaveItem(file, properties))
    }

    private fun generateRelativeFilename(key: String): String {
        var sum = 0L
        for (index in key.indices) {
            sum += key.codePointAt(index)
        }
        val relativePath = StringBuilder()
        var least = sum
        this.modules.forEach {
            relativePath.append(Constants.Symbol.SLASH_LEFT + least % it)
            least /= it
        }
        return relativePath.toString() + Constants.Symbol.SLASH_LEFT + sum
    }

    private fun generateKeyValueRelativeFilename(relativeFilename: String, key: String): String {
        return relativeFilename + Constants.Symbol.UNDERLINE + key.toByteArray().toHexString()
    }

    private fun generateKeyFullFilename(relativeFilename: String): String {
        return this.directory + relativeFilename
    }

    private fun generateKeyValueFullFilename(relativeFilename: String, key: String): String {
        return this.directory + generateKeyValueRelativeFilename(relativeFilename, key)
    }

    /**
     * @param key
     * @param value, when value is empty, will not put to file
     * @param autoSave
     */
    private fun add(key: String, value: String, autoSave: Boolean) {
        val (keyValueRelativeFilename, keyFile, keyProperties) = readKeyPropertiesAutoCreate(key)
        val (valueFile, valueProperties) = readValuePropertiesAutoCreate(keyValueRelativeFilename, value)
        if (autoSave) {
            saveProperties(keyFile, keyProperties)
            saveProperties(valueFile, valueProperties)
        }
    }

    /**
     * @param key
     * @param value, when value is empty, will not put to file
     */
    override fun add(key: String, value: String) {
        add(key, value, true)
    }

    fun addAll(keyValueList: List<Pair<String, String>>) {
        val totalSize = keyValueList.size
        for (index in keyValueList.indices) {
            val (key, value) = keyValueList[index]
            if (index == totalSize - 1) {//last
                add(key, value, true)//when false has some bug, will lose some data, because some data in cache
            } else {
                add(key, value, true)
            }
        }
    }

    private fun readKeyPropertiesAutoCreate(key: String): Triple<String, File, Properties> {
        val relativeFilename = generateRelativeFilename(key)
        val keyFullFilename = generateKeyFullFilename(relativeFilename)
        val keyFile = File(keyFullFilename)
        val (_, propertiesItem, lastUsedTime, count) = this.keyPropertiesMap.operate(relativeFilename, create = {
            //not in cache, then read from cache
            logger.debug("Auto create key file or cache, key:%s, key index full filename:%s", key, relativeFilename)
            val properties = keyFile.toPropertiesAutoCreate()
            val value = properties.getProperty(key, Constants.String.BLANK)
            if (value.isNullOrBlank()) {
                PropertiesItem(keyFile, keyFile.toPropertiesAutoCreate()) to LRUCacheMap.InitializeItemCounter()
            } else {
                val propertyValue = value.jsonToObject(PropertyValue::class)
                PropertiesItem(keyFile, keyFile.toPropertiesAutoCreate()) to LRUCacheMap.InitializeItemCounter(propertyValue.lastUsedTime, propertyValue.count)
            }
        }, removeWhenFull = { itemCounter ->
//            logger.debug("key file size:%s, key remove item:%s", this.keyPropertiesMap.size, itemCounter.toJson())
            val (removeFile, removeProperties) = itemCounter.value
            saveProperties(removeFile, removeProperties)
        }) ?: error("it is impossible logic, please check, key:$relativeFilename")
        val keyProperties = propertiesItem.properties
        val value = keyProperties.getProperty(key, Constants.String.BLANK)
        val propertyValue = if (value.isNullOrBlank()) {
            PropertyValue()
        } else {
            value.jsonToObject(PropertyValue::class)
        }
        var keyValueRelativeFilename = propertyValue.value
        if (keyValueRelativeFilename.isBlank()) {
            keyValueRelativeFilename = generateKeyValueRelativeFilename(relativeFilename, key)
        }
        keyProperties.setProperty(key, PropertyValue(keyValueRelativeFilename, lastUsedTime, count).toJson())
        return Triple(keyValueRelativeFilename, keyFile, keyProperties)
    }

    private fun readValuePropertiesAutoCreate(keyValueRelativeFilename: String, value: String): Pair<File, Properties> {
        val valueFile = File(this.directory, keyValueRelativeFilename)
        val valueMd5 = value.MD5String()

        val (_, propertiesItem, lastUsedTime, count) = this.valuePropertiesMap.operate(keyValueRelativeFilename, create = {
            //not in cache, then read from cache
            logger.debug("Auto create value file or cache, key:%s, value full filename:%s", keyValueRelativeFilename, keyValueRelativeFilename)
            val properties = valueFile.toPropertiesAutoCreate()
            val savedValue = properties.getProperty(valueMd5, Constants.String.BLANK)
            if (savedValue.isNullOrBlank()) {
                PropertiesItem(valueFile, valueFile.toPropertiesAutoCreate()) to LRUCacheMap.InitializeItemCounter()
            } else {
                val propertyValue = savedValue.jsonToObject(PropertyValue::class)
                PropertiesItem(valueFile, valueFile.toPropertiesAutoCreate()) to LRUCacheMap.InitializeItemCounter(propertyValue.lastUsedTime, propertyValue.count)
            }
        }, removeWhenFull = { itemCounter ->
//            logger.debug("value file size:%s, value remove item:%s", this.valuePropertiesMap.size, itemCounter.hashCode().toString() + itemCounter.toJson())
            val (removeFile, removeProperties) = itemCounter.value
            saveProperties(removeFile, removeProperties)
        }) ?: error("it is impossible logic, please check, key:$keyValueRelativeFilename")
        val valueProperties = propertiesItem.properties
        if (value.isBlank()) {
            return Pair(valueFile, valueProperties)
        }
        valueProperties.setProperty(valueMd5, PropertyValue(value, lastUsedTime, count).toJson())
        return Pair(valueFile, valueProperties)
    }

    override fun search(key: String): List<String> {
        val (keyValueRelativeFilename, keyFile, keyProperties) = readKeyPropertiesAutoCreate(key)
        val (valueFile, valueProperties) = readValuePropertiesAutoCreate(keyValueRelativeFilename, Constants.String.BLANK)
        saveProperties(keyFile, keyProperties)
        saveProperties(valueFile, valueProperties)
        return valueProperties.values.map { it.toString() }
    }

    override fun hit(key: String, value: String) {
        val (keyValueRelativeFilename, keyFile, keyProperties) = readKeyPropertiesAutoCreate(key)
        val (valueFile, valueProperties) = readValuePropertiesAutoCreate(keyValueRelativeFilename, value)
        saveProperties(keyFile, keyProperties)
        saveProperties(valueFile, valueProperties)
    }

    private fun delete(key: String, value: String, autoSave: Boolean) {
        val (keyValueRelativeFilename, keyFile, keyProperties) = readKeyPropertiesAutoCreate(key)
        val (valueFile, valueProperties) = readValuePropertiesAutoCreate(keyValueRelativeFilename, value)
        val valueMd5 = value.MD5String()
        valueProperties.remove(valueMd5)
        if (autoSave) {
            saveProperties(keyFile, keyProperties)
            saveProperties(valueFile, valueProperties)
        }
    }

    override fun delete(key: String, value: String) {
        delete(key, value, true)
    }

    override fun existValues(key: String): Boolean {
        val (keyValueRelativeFilename, _, _) = readKeyPropertiesAutoCreate(key)
        val (_, valueProperties) = readValuePropertiesAutoCreate(keyValueRelativeFilename, Constants.String.BLANK)
        return valueProperties.values.isNotEmpty()
    }

    override fun update(key: String, originalValue: String, newValue: String) {
        delete(key, originalValue, false)
        add(key, newValue, true)
    }

    private class AutoSaveItem(val file: File, val properties: Properties)

    private class PropertiesItem(val file: File, val properties: Properties) {
        operator fun component1(): File {
            return this.file
        }

        operator fun component2(): Properties {
            return this.properties
        }
    }

    class PropertyValue(var value: String = Constants.String.BLANK, var lastUsedTime: Long = 0L, var count: Int = 0) {
        constructor() : this(Constants.String.BLANK, 0L, 0)
    }
}

private fun benchMark() {
//    val fileStorage = FileStorage("/D:/temp", cacheMaxSize = 10)
    val fileStorage = FileStorage("/Users/oneliang/Java/temp", cacheMaxSize = 10)
    fileStorage.initialize()
//    fileStorage.search("A").forEach {
//        println(it.jsonToObject(FileStorage.PropertyValue::class).value)
//    }
//    println(fileStorage.existValues("你好吗"))
//    fileStorage.add("你好吗", "好好")
//    fileStorage.add("A", "A")
//    fileStorage.add("A", "B")
//    fileStorage.add("A", "A\r\nB\r\nC")
//    val repeatTime = 10
//    for (i in 0..repeatTime) {
//        val begin = System.currentTimeMillis()
//        println(fileStorage.search("A").toJson() + ", cost:" + (System.currentTimeMillis() - begin))
//    }
//    fileStorage.hit("A", "A")
//    fileStorage.hit("A", "A")

    return
    val array = arrayOf("A", "B", "C", "D", "E")
    var totalSize = 0
    val set = mutableListOf<String>()
    for (i in 1..array.size) {
        val composeList = array.calculateCompose(i)
        composeList.forEach {
            val permutationList = it.calculatePermutation()
            totalSize += permutationList.size
            permutationList.forEach { permutation ->
                set += permutation.joinToString(separator = Constants.String.BLANK)
            }
        }
    }
    println(set.size)
    set.forEach {
//        fileStorage.add(it, it)
    }
    val pairList = set.map { it to it }
    fileStorage.addAll(pairList)
//    return
    var begin = System.currentTimeMillis()
    var list = fileStorage.search("A")
    var cost = System.currentTimeMillis() - begin
    println("$cost," + list.toJson())
    begin = System.currentTimeMillis()
    list = fileStorage.search("B")
    cost = System.currentTimeMillis() - begin
    println("$cost," + list.toJson())
    begin = System.currentTimeMillis()
    list = fileStorage.search("B")
    cost = System.currentTimeMillis() - begin
    println("$cost," + list.toJson())
    begin = System.currentTimeMillis()
    fileStorage.delete("B", "B")
    list = fileStorage.search("B")
    cost = System.currentTimeMillis() - begin
    println("$cost," + list.toJson())

    begin = System.currentTimeMillis()
    fileStorage.update("B", "B", "BBB")
    list = fileStorage.search("B")
    cost = System.currentTimeMillis() - begin
    println("$cost," + list.toJson())
}

fun main() {
    benchMark()
}