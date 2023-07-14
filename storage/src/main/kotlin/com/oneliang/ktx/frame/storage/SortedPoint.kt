package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toLongBits
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

class SortedPoint(
    directory: String,
    useCompress: Boolean = true
) : ContentStorage(directory, useCompress) {

    companion object {
        private val logger = LoggerManager.getLogger(SortedPoint::class)
        private const val DATA_LENGTH = 12
    }


    init {
        super.initialize()
    }

    /**
     * find
     * @param pointId
     * @param from [0,+]
     * @param size
     * @return List<ValueInfo>
     */
    fun find(pointId: Int, from: Int, size: Int): List<ValueInfo> {
        if (from < 0) {
            return emptyList()
        }

        val existId = this.existId(pointId)

        if (!existId) {
            return emptyList()
        }

        val contentByteArray = this.collectContent(pointId)

        assert(contentByteArray.size % DATA_LENGTH == 0)

        val contentInputStream = DataInputStream(ByteArrayInputStream(contentByteArray))

        val contentSize = contentByteArray.size / DATA_LENGTH

        if (from >= contentSize) {
            return emptyList()
        }

        val list = mutableListOf<ValueInfo>()
        //skip data
        contentInputStream.skip((from * DATA_LENGTH).toLong())

        val end = minOf(from + size, contentSize)

        for (i in from until end) {
            list += ValueInfo(contentInputStream.readInt(), contentInputStream.readDouble())
        }
        return list
    }

    /**
     * add
     * @param pointId
     * @param documentId
     * @param score
     */
    fun add(pointId: Int, documentId: Int, score: Double) {
        val existId = this.existId(pointId)
        if (existId) {
            val contentByteArray = this.collectContent(pointId)

            assert(contentByteArray.size % DATA_LENGTH == 0)

            val contentInputStream = DataInputStream(ByteArrayInputStream(contentByteArray))

            val contentSize = contentByteArray.size / DATA_LENGTH

            val list = mutableListOf<Pair<Int, Double>>()
            for (i in 0 until contentSize) {
                list += contentInputStream.readInt() to contentInputStream.readDouble()
            }
            list += documentId to score
            val sortedList = list.sortedByDescending { it.second }

            val byteArrayOutputStream = ByteArrayOutputStream()
            val dataOutputStream = DataOutputStream(byteArrayOutputStream)
            for ((sortedDocumentId, sortedDocumentScore) in sortedList) {
                dataOutputStream.writeInt(sortedDocumentId)
                dataOutputStream.writeDouble(sortedDocumentScore)
            }
            this.replaceContent(pointId, byteArrayOutputStream.toByteArray())
        } else {//create
            val byteArrayOutputStream = ByteArrayOutputStream()
            byteArrayOutputStream.write(documentId.toByteArray())
            byteArrayOutputStream.write(score.toLongBits().toByteArray())
            val data = byteArrayOutputStream.toByteArray()
            this.addContent(pointId, data)
        }
    }


    class ValueInfo(val id: Int, val score: Double)
}