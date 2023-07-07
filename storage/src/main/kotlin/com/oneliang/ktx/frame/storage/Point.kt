package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toHexString
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.common.toShort
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap

class Point(
    fullFilename: String, accessMode: BinaryStorage.AccessMode = BinaryStorage.AccessMode.RW, private val pageValueCount: Int = 100
) : BlockStorage(
    fullFilename, accessMode, PAGE_INFO_LENGTH + ONE_VALUE_LENGTH * pageValueCount
) {
    companion object {
        private val logger = LoggerManager.getLogger(Point::class)
        private const val POINT_ID_LENGTH = 4//4+2+2
        private const val PAGE_NO_LENGTH = 2
        private const val VALUE_COUNT_LENGTH = 2
        private const val PAGE_INFO_LENGTH = 8//4+2+2
        private const val ONE_VALUE_LENGTH = 12
    }

    private val pointIdPageInfoMap = ConcurrentHashMap<Int, MutableList<PointPageInfo>>()
    private val pointIdLastPageInfoMap = ConcurrentHashMap<Int, Pair<Short, PointPageInfo>>()

    /**
     * update all page info map
     * @param pointId
     * @param pageNo
     * @param valueCount
     * @param start
     */
    private fun updateAllPageInfoMap(pointId: Int, pageNo: Short, valueCount: Short, start: Long) {
        val pointPageInfo = PointPageInfo(pointId, pageNo, valueCount, start)
        val pointPageInfoList = this.pointIdPageInfoMap.getOrPut(pointId) { mutableListOf() }
        pointPageInfoList += pointPageInfo

        val pointPageInfoPair = this.pointIdLastPageInfoMap.getOrPut(pointId) { pageNo to pointPageInfo }

        val (maxPageNo, oldPointPageInfo) = pointPageInfoPair
        if (pageNo > maxPageNo) {
            this.pointIdLastPageInfoMap[pointId] = pageNo to pointPageInfo
            logger.debug("point id:%s, page no:%s, previous max page no:%s, value count:%s", pointId, pageNo, maxPageNo, valueCount)
        } else {//same page, need to update value count
            oldPointPageInfo.valueCount = valueCount
            logger.debug("same page, point id:%s, page no:%s, previous max page no:%s, value count:%s", pointId, pageNo, maxPageNo, valueCount)
        }
    }

    /**
     * read block
     * @param index
     * @param start
     * @param byteArray
     */
    override fun readBlock(index: Int, start: Long, byteArray: ByteArray) {
        val pointId = byteArray.sliceArray(0 until 4).toInt()
        val pageNo = byteArray.sliceArray(4 until 6).toShort()
        val valueCount = byteArray.sliceArray(6 until PAGE_INFO_LENGTH).toShort()

        this.updateAllPageInfoMap(pointId, pageNo, valueCount, start)
    }

    /**
     * write data
     * @param pointId
     * @param documentId
     * @param score
     * @return Int
     */
    fun write(pointId: Int, documentId: Int, score: Double) {
        val pointPageInfoPair = this.pointIdLastPageInfoMap[pointId]
        var pageNo: Short = 1
        var valueCount: Short = 1
        if (pointPageInfoPair != null) {//update
            pageNo = pointPageInfoPair.second.pageNo
            valueCount = pointPageInfoPair.second.valueCount
            val pointPageStart = pointPageInfoPair.second.start
            if (valueCount >= pageValueCount) {
                pageNo = (pageNo + 1).toShort()
                valueCount = 1
                val byteArrayOutputStream = ByteArrayOutputStream()
                byteArrayOutputStream.write(pointId.toByteArray())
                byteArrayOutputStream.write(pageNo.toByteArray())
                byteArrayOutputStream.write(valueCount.toByteArray())//value count
                byteArrayOutputStream.write(documentId.toByteArray())
                byteArrayOutputStream.write(score.toBits().toByteArray())
                byteArrayOutputStream.write(ByteArray(this.blockSize - PAGE_INFO_LENGTH - ONE_VALUE_LENGTH))
                val byteArray = byteArrayOutputStream.toByteArray()
                this.writeBlock(byteArray)//append to the end of file

                logger.debug("point new page, point id:%s, page no:%s, byte array:%s", pointId, pageNo, byteArray.toHexString())

                this.updateAllPageInfoMap(pointId, pageNo, valueCount, pointPageStart)
            } else {
                //update value count, seek and write
                valueCount = (valueCount + 1).toShort()
                this.write(valueCount.toByteArray(), pointPageStart + POINT_ID_LENGTH + PAGE_NO_LENGTH)

                //write value, seek and write
                val valueStart = pointPageStart + POINT_ID_LENGTH + PAGE_NO_LENGTH + VALUE_COUNT_LENGTH + (valueCount - 1) * ONE_VALUE_LENGTH
                val byteArrayOutputStream = ByteArrayOutputStream()
                byteArrayOutputStream.write(documentId.toByteArray())
                byteArrayOutputStream.write(score.toBits().toByteArray())
                val byteArray = byteArrayOutputStream.toByteArray()
                this.write(byteArray, valueStart)

                logger.debug("point new value, point id:%s, page no:%s, value start:%s, byte array:%s", pointId, pageNo, valueStart, byteArray.toHexString())

                this.updateAllPageInfoMap(pointId, pageNo, valueCount, pointPageStart)
            }
        } else {//create
            val byteArrayOutputStream = ByteArrayOutputStream()
            byteArrayOutputStream.write(pointId.toByteArray())
            byteArrayOutputStream.write(pageNo.toByteArray())
            byteArrayOutputStream.write(valueCount.toByteArray())//value count
            byteArrayOutputStream.write(documentId.toByteArray())
            byteArrayOutputStream.write(score.toBits().toByteArray())
            byteArrayOutputStream.write(ByteArray(this.blockSize - PAGE_INFO_LENGTH - ONE_VALUE_LENGTH))
            val byteArray = byteArrayOutputStream.toByteArray()
            val (pointPageStart, _) = this.writeBlock(byteArray)//append to the end of file

            logger.debug("point new page about no point, point id:%s, page no:%s, value start:%s, byte array:%s", pointId, pageNo, pointPageStart, byteArray.toHexString())

            this.updateAllPageInfoMap(pointId, pageNo, valueCount, pointPageStart)
        }
    }

    class PointPageInfo(
        val pointId: Int,
        var pageNo: Short = 0,
        var valueCount: Short = 0,
        val start: Long = 0L
    )
}