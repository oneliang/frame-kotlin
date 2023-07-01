package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class Route(
    fullFilename: String,
    accessMode: BinaryStorage.AccessMode = BinaryStorage.AccessMode.RW,
    initialDocumentId: Int = 0
) {
    companion object {
        private val logger = LoggerManager.getLogger(Route::class)
        private const val DATA_LENGTH = 22
    }

    private val documentIdAtomic = AtomicInteger(initialDocumentId)
    private val binaryStorage = BinaryStorage(fullFilename, accessMode)
    private val documentMap = ConcurrentHashMap<Int, ByteArray>()

    init {
        read()
    }

    /**
     * read
     */
    fun read() {
        val begin = System.currentTimeMillis()
        val fileLength = this.binaryStorage.file.length()

        assert(fileLength % DATA_LENGTH == 0L)

        val size = fileLength / DATA_LENGTH
        for (i in 0 until size) {
            val start = i * DATA_LENGTH
            val end = (i + 1) * DATA_LENGTH
            val byteArray = this.binaryStorage.read(start, end)
            val documentId = byteArray.sliceArray(0 until 4).toInt()
            val documentSegmentInfo = byteArray.sliceArray(4 until DATA_LENGTH)
            this.documentMap[documentId] = documentSegmentInfo
        }
        logger.info("read cost:%s", System.currentTimeMillis() - begin)
    }

    /**
     * write data
     * @param segmentNo
     * @param start
     * @param end
     * @return
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

        this.binaryStorage.write(route)
        this.documentMap[docId] = route.sliceArray(4 until DATA_LENGTH)
        return docId
    }

    /**
     * finalize
     */
    fun finalize() {
        this.binaryStorage.finalize()
    }
}