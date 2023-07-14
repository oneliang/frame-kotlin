package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.file.FileWrapper
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap

class Point(
    fullFilename: String, accessMode: FileWrapper.AccessMode = FileWrapper.AccessMode.RW, private val pageValueCount: Int = 100
) : BlockStorage(
    fullFilename, accessMode, PAGE_INFO_LENGTH + ONE_VALUE_LENGTH * pageValueCount
) {
    companion object {
        private val logger = LoggerManager.getLogger(Point::class)
        private const val POINT_ID_LENGTH = 4//4+2+2
        private const val PAGE_NO_LENGTH = 2
        private const val VALUE_COUNT_LENGTH = 2
        private const val PAGE_INFO_LENGTH = POINT_ID_LENGTH + PAGE_NO_LENGTH + VALUE_COUNT_LENGTH//4+2+2
        private const val VALUE_ID_LENGTH = 4//int
        private const val VALUE_SCORE_LENGTH = 8//double
        private const val ONE_VALUE_LENGTH = VALUE_ID_LENGTH + VALUE_SCORE_LENGTH
    }

    private val pointIdPageInfoMap = ConcurrentHashMap<Int, MutableList<PointPageInfo>>()
    private val pointIdLastPageInfoMap = ConcurrentHashMap<Int, Pair<Short, PointPageInfo>>()

    init {
        initialize()
    }

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
     * add
     * @param pointId
     * @param documentId
     * @param score
     * @return Int
     */
    fun add(pointId: Int, documentId: Int, score: Double) {
        val pointPageInfoPair = this.pointIdLastPageInfoMap[pointId]
        var pageNo: Short = 1
        var valueCount: Short = 1
        if (pointPageInfoPair != null) {//update
            pageNo = pointPageInfoPair.second.pageNo
            valueCount = pointPageInfoPair.second.valueCount
            val pointPageStart = pointPageInfoPair.second.start
            if (valueCount >= this.pageValueCount) {
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

    /**
     * find
     * @param pointId
     * @param from [0,+]
     * @param size
     * @return List<ValueInfo>
     */
    fun find(pointId: Int, from: Int, size: Int): List<ValueInfo> {
        val valueInfoList = mutableListOf<ValueInfo>()
        val pointPageInfoList = this.pointIdPageInfoMap[pointId] ?: emptyList()
        val fromIndex = from
        val endIndex = from + size
        var count = 0
        run loop@{
            pointPageInfoList.forEach {
                if (fromIndex > (it.pageNo * this.pageValueCount - 1) || endIndex < (it.pageNo - 1) * this.pageValueCount) {
                    return@forEach//continue
                }
                val start = it.start + PAGE_INFO_LENGTH
                val end = start + it.valueCount * ONE_VALUE_LENGTH
                val byteArray = this.read(start, end)
                for (valueIndex in 0 until it.valueCount) {
                    val id = byteArray.sliceArray(ONE_VALUE_LENGTH * valueIndex until ONE_VALUE_LENGTH * valueIndex + VALUE_ID_LENGTH).toInt()
                    val score = byteArray.sliceArray(ONE_VALUE_LENGTH * valueIndex + VALUE_ID_LENGTH until ONE_VALUE_LENGTH * valueIndex + ONE_VALUE_LENGTH).toDouble()
                    valueInfoList += ValueInfo(id, score)
                    count++
                    if (count == size) {
                        return@loop//break all
                    }
                }
            }
        }
        return valueInfoList
    }

    class ValueInfo(val id: Int, val score: Double)

    class PointPageInfo(
        val pointId: Int,
        var pageNo: Short = 0,
        var valueCount: Short = 0,
        val start: Long = 0L
    )
}