package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.util.common.Hasher
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class ComplexPoint(
    private val directory: String
) {
    companion object {
        private val logger = LoggerManager.getLogger(ComplexPoint::class)
        private const val POINT_FILENAME_FORMAT = "point_%s"
    }

    private val hasher = Hasher(10)
    private val blockStorageExtMap = ConcurrentHashMap<Int, BlockStorageExt<Int, PointValueInfo>>()
    private val sortedPoint: SortedPoint = SortedPoint(this.directory)


    /**
     * add
     * @param pointId
     * @param documentId
     * @param score
     */
    fun add(pointId: Int, documentId: Int, score: Double) {
        val blockStorageExt = this.hasher.hash(pointId) {
            this.blockStorageExtMap.getOrPut(it) {
                val fullFilename = File(this.directory, POINT_FILENAME_FORMAT.format(it)).absolutePath
                BlockStorageExt(fullFilename, dataLength = PointValueInfo.DATA_LENGTH) { _: Int, _: Long, byteArray: ByteArray ->
                    PointValueInfo.fromByteArray(byteArray)
                }
            }
        }
        blockStorageExt.add(PointValueInfo(pointId, documentId, score), false)
    }

    /**
     * find
     * @param pointId
     * @param from
     * @param size
     */
    fun find(pointId: Int, from: Int, size: Int): List<SortedPoint.ValueInfo> {
        return this.sortedPoint.find(pointId, from, size)
    }

    /**
     * flush
     */
    fun flush() {
        this.blockStorageExtMap.forEach { (hashKey, blockStorageExt) ->
            blockStorageExt.flush()
            logger.info("flush finished, hash key:%s", hashKey)
            blockStorageExt.iterateValueMap { pointId, valueList ->
                val sortedValueList = valueList.sortedByDescending { it.valueScore }
                val byteArrayOutputStream = ByteArrayOutputStream()
                sortedValueList.forEach {
                    byteArrayOutputStream.write(it.valueId.toByteArray())
                    byteArrayOutputStream.write(it.valueScore.toByteArray())
                }
                this.sortedPoint.addContent(pointId, byteArrayOutputStream.toByteArray())
            }
        }
    }
}