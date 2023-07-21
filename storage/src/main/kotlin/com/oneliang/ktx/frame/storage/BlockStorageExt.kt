package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.util.concurrent.atomic.OperationLock
import com.oneliang.ktx.util.file.FileWrapper
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class BlockStorageExt<K : Any, V : BlockStorageExt.Value<K>>(
    fullFilename: String,
    accessMode: FileWrapper.AccessMode = FileWrapper.AccessMode.RW,
    dataLength: Int,
    private val valueFlushSize: Int = VALUE_FLUSH_SIZE,
    private val valueCreateBlock: (index: Int, start: Long, byteArray: ByteArray) -> V
) : BlockStorage(
    fullFilename, accessMode, dataLength
) {
    companion object {
        private val logger = LoggerManager.getLogger(BlockStorageExt::class)
        private const val VALUE_FLUSH_SIZE = 10000
    }

    private val valueMap = ConcurrentHashMap<K, CopyOnWriteArrayList<V>>()
    private val unsyncValueMap = ConcurrentHashMap<K, CopyOnWriteArrayList<V>>()
    private val flushLock = OperationLock()

    init {
        initialize()
    }

    /**
     * update value list
     * @param key
     * @param value
     */
    private fun updateValueMap(key: K, value: V) {
        val list = this.valueMap.getOrPut(key) { CopyOnWriteArrayList<V>() }
        list += value
    }

    /**
     * update unsync value list
     * @param key
     * @param value
     */
    private fun updateUnsyncValueMap(key: K, value: V) {
        val list = this.unsyncValueMap.getOrPut(key) { CopyOnWriteArrayList<V>() }
        list += value
    }

    /**
     * read block
     * @param index
     * @param start
     * @param byteArray
     */
    override fun readBlock(index: Int, start: Long, byteArray: ByteArray) {
        val value = this.valueCreateBlock(index, start, byteArray)
        val key = value.key
        this.updateValueMap(key, value)
    }

    /**
     * add
     * @param value
     * @param flush
     */
    fun add(value: V, flush: Boolean = true) {
        val byteArray = value.toByteArray()
        val key = value.key
        if (flush) {
            val (_, _) = this.writeBlock(byteArray)//append to the end of file
            this.updateValueMap(key, value)
        } else {
            this.updateUnsyncValueMap(key, value)
        }
        logger.verbose("add, byte array size:%s", byteArray.size)
    }

    /**
     * add
     * @param valueList
     * @param flush
     */
    fun add(valueList: List<V>, flush: Boolean = true) {
        if (flush) {
            addValueListWithFlush(valueList)
        } else {
            for (value in valueList) {
                this.add(value, false)
            }
        }

    }

    /**
     * add value list with flush
     * @param valueList
     */
    private fun addValueListWithFlush(valueList: List<V>) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        for (value in valueList) {
            byteArrayOutputStream.write(value.toByteArray())
            val key = value.key
            this.updateValueMap(key, value)
        }
        this.write(byteArrayOutputStream.toByteArray())
    }

    /**
     * find
     * @param key
     * @return List<V>
     */
    fun find(key: K): List<V> {
        return this.valueMap[key] ?: this.unsyncValueMap[key] ?: emptyList()
    }

    /**
     * iterate value map
     * @param block
     */
    fun iterateValueMap(block: (key: K, valueList: List<V>) -> Unit) {
        this.valueMap.forEach(block)
    }

    /**
     * flush to file, flush unsync value to file and value map
     * @param afterFlushBlock
     */
    fun flush(afterFlushBlock: (valueList: List<V>) -> Unit = {}) {
        val begin = System.currentTimeMillis()
        this.flushLock.operate {
            val list = mutableListOf<V>()
            this.unsyncValueMap.forEach { (key, valueList) ->
                list += valueList
                if (list.size >= this.valueFlushSize) {
                    addValueListWithFlush(valueList)
                    afterFlushBlock(valueList)
                    logger.verbose("flushing key:%s, value list size:%s", key, valueList.size)
                    list.clear()
                }
            }
            addValueListWithFlush(list)
            afterFlushBlock(list)
            this.unsyncValueMap.clear()
            logger.verbose("flush finished")
        }
        logger.verbose("cost:%s", (System.currentTimeMillis() - begin))
    }


//    /**
//     * sync from
//     */
//    fun syncFrom() {
//        this.syncLock.operate {
//            this.valueMap.clear()
//            this.initialize()
//        }
//    }

    abstract class Value<K : Any> {
        abstract val key: K
        abstract fun toByteArray(): ByteArray
    }
}