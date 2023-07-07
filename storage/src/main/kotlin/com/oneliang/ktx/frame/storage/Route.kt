package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class Route(
    fullFilename: String, accessMode: BinaryStorage.AccessMode = BinaryStorage.AccessMode.RW, initialDocumentId: Int = 0
) : BlockStorage(
    fullFilename, accessMode, DATA_LENGTH
) {
    companion object {
        private val logger = LoggerManager.getLogger(Route::class)
        private const val DATA_LENGTH = 22
    }

    private val documentIdAtomic = AtomicInteger(initialDocumentId)
    private val documentMap = ConcurrentHashMap<Int, ByteArray>()

    /**
     * read block
     * @param index
     * @param start
     * @param byteArray
     */
    override fun readBlock(index: Int, start: Long, byteArray: ByteArray) {
        val documentId = byteArray.sliceArray(0 until 4).toInt()
        val documentSegmentInfo = byteArray.sliceArray(4 until DATA_LENGTH)
        this.documentMap[documentId] = documentSegmentInfo
    }

    /**
     * write data
     * @param segmentNo
     * @param start
     * @param end
     * @return Int
     */
    fun write(segmentNo: Short, start: Long, end: Long): Int {
        val docId = this.documentIdAtomic.incrementAndGet()

        val byteArrayOutputStream = ByteArrayOutputStream()
        byteArrayOutputStream.write(docId.toByteArray())
        byteArrayOutputStream.write(segmentNo.toByteArray())
        byteArrayOutputStream.write(start.toByteArray())
        byteArrayOutputStream.write(end.toByteArray())
        val route = byteArrayOutputStream.toByteArray()

        assert(route.size == DATA_LENGTH)

        val startPosition = docId.toLong() * DATA_LENGTH
        this.write(route, startPosition)
        this.documentMap[docId] = route.sliceArray(4 until DATA_LENGTH)
        return docId
    }
}