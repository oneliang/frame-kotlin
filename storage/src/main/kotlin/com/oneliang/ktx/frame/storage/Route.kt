package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.common.toLong
import com.oneliang.ktx.util.common.toShort
import com.oneliang.ktx.util.concurrent.atomic.OperationLock
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class Route constructor(
    fullFilename: String,
    accessMode: BinaryStorage.AccessMode = BinaryStorage.AccessMode.RW,
    initialValueId: Int = Int.MIN_VALUE
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

    private lateinit var valueIdAtomic: AtomicInteger
    private val valueMap = ConcurrentHashMap<Int, ValueInfo>()
    private val writeLock = OperationLock()

    init {
        initialize()
        if (initialValueId >= 0) {
            this.valueIdAtomic = AtomicInteger(initialValueId)
        } else {
            logger.warning("can not use dynamic value, because initial value id is:%s", initialValueId)
        }
    }

    /**
     * read block
     * @param index
     * @param start
     * @param byteArray
     */
    override fun readBlock(index: Int, start: Long, byteArray: ByteArray) {
        val documentId = byteArray.sliceArray(0 until ID_LENGTH).toInt()
        val segmentNo = byteArray.sliceArray(ID_LENGTH until ID_LENGTH + SEGMENT_NO_LENGTH).toShort()
        val valueStart = byteArray.sliceArray(ID_LENGTH + SEGMENT_NO_LENGTH until ID_LENGTH + SEGMENT_NO_LENGTH + START_LENGTH).toLong()
        val valueEnd = byteArray.sliceArray(ID_LENGTH + SEGMENT_NO_LENGTH + START_LENGTH until DATA_LENGTH).toLong()
        val valueInfo = ValueInfo(segmentNo).also {
            it.start = valueStart
            it.end = valueEnd
        }
        this.valueMap[documentId] = valueInfo
    }

    /**
     * write data
     * @param segmentNo
     * @param start
     * @param end
     * @return Int
     */
    fun write(segmentNo: Short, start: Long, end: Long): Int {
        if (this::valueIdAtomic.isInitialized) {
            val valueId = this.valueIdAtomic.incrementAndGet()
            return this.write(valueId, segmentNo, start, end)
        } else {
            error("can not use dynamic value, please use the other write method, fun write(valueId: Int, segmentNo: Short, start: Long, end: Long): Int")
        }
    }

    /**
     * write data
     * @param valueId
     * @param segmentNo
     * @param start
     * @param end
     * @return Int
     */
    fun write(valueId: Int, segmentNo: Short, start: Long, end: Long): Int {
        return this.writeLock.operate {
            val byteArrayOutputStream = ByteArrayOutputStream()
            byteArrayOutputStream.write(valueId.toByteArray())
            byteArrayOutputStream.write(segmentNo.toByteArray())
            byteArrayOutputStream.write(start.toByteArray())
            byteArrayOutputStream.write(end.toByteArray())
            val route = byteArrayOutputStream.toByteArray()

            assert(route.size == DATA_LENGTH)

            val startPosition = valueId.toLong() * DATA_LENGTH
            this.write(route, startPosition)
            this.valueMap[valueId] = ValueInfo(segmentNo).also {
                it.start = start
                it.end = end
            }
            valueId
        }
    }

    /**
     * find value info
     * @param valueId
     * @return ValueInfo?
     */
    fun findValueInfo(valueId: Int): ValueInfo? {
        return this.valueMap[valueId]
    }

    /**
     * update all value info
     * @param separateValueId
     * @param dataOffset
     * @return Boolean
     */
    fun updateAllValueInfo(separateValueId: Int, dataOffset: Long): Boolean {
        return this.writeLock.operate {
            val separateValueInfo = this.valueMap[separateValueId]
            if (separateValueInfo == null) {
                logger.warning("value id:%s not found.", separateValueId)
                return@operate false
            }
            separateValueInfo.end = separateValueInfo.end + dataOffset
            this.valueMap.forEach { (id, valueInfo) ->
                if (valueInfo.start > separateValueInfo.start) {
                    valueInfo.start = valueInfo.start + dataOffset
                    valueInfo.end = valueInfo.end + dataOffset
                } else {
                    return@forEach//continue
                }
            }
            true
        }
    }

    class ValueInfo(val segmentNo: Short) {
        var start: Long = 0
            internal set
        var end: Long = 0
            internal set
    }
}