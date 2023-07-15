package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.common.toLong
import com.oneliang.ktx.util.common.toShort
import com.oneliang.ktx.util.concurrent.atomic.OperationLock
import com.oneliang.ktx.util.file.FileWrapper
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class Route constructor(
    fullFilename: String,
    accessMode: FileWrapper.AccessMode = FileWrapper.AccessMode.RW,
    initialId: Int = Int.MIN_VALUE
) : BlockStorage(
    fullFilename, accessMode, DATA_LENGTH
) {
    companion object {
        private val logger = LoggerManager.getLogger(Route::class)
        private const val ID_LENGTH = 4
        private const val SEGMENT_NO_LENGTH = 2
        private const val START_LENGTH = 8
        private const val END_LENGTH = 8
        private const val DATA_LENGTH = ID_LENGTH + SEGMENT_NO_LENGTH + START_LENGTH + END_LENGTH
    }

    private lateinit var idAtomic: AtomicInteger
    private val idMap = ConcurrentHashMap<Int, ValueInfo>()
    private val segmentIdMap = ConcurrentHashMap<Short, MutableList<ValueInfo>>()
    private val writeLock = OperationLock()

    init {
        initialize()
        if (initialId >= 0) {
            this.idAtomic = AtomicInteger(initialId)
        } else {
            logger.warning("can not use dynamic value, because initial id is:%s", initialId)
        }
    }

    /**
     * read block
     * @param index
     * @param start
     * @param byteArray
     */
    override fun readBlock(index: Int, start: Long, byteArray: ByteArray) {
        val id = byteArray.sliceArray(0 until ID_LENGTH).toInt()
        val segmentNo = byteArray.sliceArray(ID_LENGTH until ID_LENGTH + SEGMENT_NO_LENGTH).toShort()
        val valueStart = byteArray.sliceArray(ID_LENGTH + SEGMENT_NO_LENGTH until ID_LENGTH + SEGMENT_NO_LENGTH + START_LENGTH).toLong()
        val valueEnd = byteArray.sliceArray(ID_LENGTH + SEGMENT_NO_LENGTH + START_LENGTH until DATA_LENGTH).toLong()
        val valueInfo = ValueInfo(id, segmentNo).also {
            it.start = valueStart
            it.end = valueEnd
        }
        this.idMap[id] = valueInfo
        val idList = this.segmentIdMap.getOrPut(segmentNo) { mutableListOf() }
        idList += valueInfo
    }

    /**
     * write data
     * @param segmentNo
     * @param start
     * @param end
     * @return Int
     */
    fun write(segmentNo: Short, start: Long, end: Long): Int {
        if (this::idAtomic.isInitialized) {
            val id = this.idAtomic.incrementAndGet()
            return this.write(id, segmentNo, start, end)
        } else {
            error("can not use dynamic value, please use the other write method, fun write(id: Int, segmentNo: Short, start: Long, end: Long): Int")
        }
    }

    /**
     * write data
     * @param id
     * @param segmentNo
     * @param start
     * @param end
     * @return Int
     */
    fun write(id: Int, segmentNo: Short, start: Long, end: Long): Int {
        return this.writeLock.operate {

            this.idAtomic.set(id)//update the id atomic

            rewriteValueInfo(id, segmentNo, start, end)

            val valueInfo = ValueInfo(id, segmentNo).also {
                it.start = start
                it.end = end
            }
            this.idMap[id] = valueInfo
            val idList = this.segmentIdMap.getOrPut(segmentNo) { mutableListOf() }
            idList += valueInfo
            id
        }
    }

    /**
     * find value info
     * @param id
     * @return ValueInfo?
     */
    fun findValueInfo(id: Int): ValueInfo? {
        return this.idMap[id]
    }

    /**
     * update all value info
     * @param separateId
     * @param dataOffset
     * @return Boolean
     */
    fun updateAllValueInfo(separateId: Int, dataOffset: Long): Boolean {
        return this.writeLock.operate {
            val separateValueInfo = this.idMap[separateId]
            if (separateValueInfo == null) {
                logger.warning("value id:%s not found.", separateId)
                return@operate false
            }
            separateValueInfo.end = separateValueInfo.end + dataOffset
            this.rewriteValueInfo(separateValueInfo)
            val idList = this.segmentIdMap[separateValueInfo.segmentNo] ?: emptyList()
            if (idList.isEmpty()) {
                logger.error("id list is empty, it is impossible, please check the logic, separate id:%s, segment no:%s", separateId, separateValueInfo.segmentNo)
                return@operate false
            }

            idList.forEach { valueInfo ->
                if (valueInfo.start > separateValueInfo.start) {
                    valueInfo.start = valueInfo.start + dataOffset
                    valueInfo.end = valueInfo.end + dataOffset
                    this.rewriteValueInfo(valueInfo)
                } else {
                    return@forEach//continue
                }
            }
            true
        }
    }

    /**
     * rewrite value info
     * @param valueInfo
     */
    private fun rewriteValueInfo(valueInfo: ValueInfo) {
        this.rewriteValueInfo(valueInfo.id, valueInfo.segmentNo, valueInfo.start, valueInfo.end)
    }

    /**
     * update value info
     * @param id
     * @param segmentNo
     * @param start
     * @param end
     */
    private fun rewriteValueInfo(id: Int, segmentNo: Short, start: Long, end: Long) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        byteArrayOutputStream.write(id.toByteArray())
        byteArrayOutputStream.write(segmentNo.toByteArray())
        byteArrayOutputStream.write(start.toByteArray())
        byteArrayOutputStream.write(end.toByteArray())
        val route = byteArrayOutputStream.toByteArray()

        assert(route.size == DATA_LENGTH)

        val startPosition = id.toLong() * DATA_LENGTH
        this.write(route, startPosition)
    }

    class ValueInfo(val id: Int, val segmentNo: Short) {
        var start: Long = 0
            internal set
        var end: Long = 0
            internal set
    }
}