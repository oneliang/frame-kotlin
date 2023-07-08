package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.common.toLong
import com.oneliang.ktx.util.common.toShort
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class Route constructor(
    fullFilename: String, accessMode: BinaryStorage.AccessMode = BinaryStorage.AccessMode.RW, initialValueId: Int = 0
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

    private val valueIdAtomic = AtomicInteger(initialValueId)
    private val valueMap = ConcurrentHashMap<Int, ValueInfo>()

    init {
        initialize()
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
        val valueInfo = ValueInfo(segmentNo, valueStart, valueEnd)
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
        val valueId = this.valueIdAtomic.incrementAndGet()

        val byteArrayOutputStream = ByteArrayOutputStream()
        byteArrayOutputStream.write(valueId.toByteArray())
        byteArrayOutputStream.write(segmentNo.toByteArray())
        byteArrayOutputStream.write(start.toByteArray())
        byteArrayOutputStream.write(end.toByteArray())
        val route = byteArrayOutputStream.toByteArray()

        assert(route.size == DATA_LENGTH)

        val startPosition = valueId.toLong() * DATA_LENGTH
        this.write(route, startPosition)
        this.valueMap[valueId] = ValueInfo(segmentNo, start, end)
        return valueId
    }

    /**
     * find value info
     * @param valueId
     * @return ValueInfo?
     */
    fun findValueInfo(valueId: Int): ValueInfo? {
        return this.valueMap[valueId]
    }

    class ValueInfo(val segmentNo: Short, val start: Long, val end: Long)
}