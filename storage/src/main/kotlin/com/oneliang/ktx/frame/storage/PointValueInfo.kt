package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toDouble
import com.oneliang.ktx.util.common.toInt
import java.io.ByteArrayOutputStream

class PointValueInfo(
    val pointId: Int,
    val valueId: Int,
    val valueScore: Double = 0.0
) : BlockStorageExt.Value<Int>() {
    companion object {
        private const val POINT_ID_LENGTH = 4
        private const val VALUE_ID_LENGTH = 4//int
        private const val VALUE_SCORE_LENGTH = 8//double
        const val DATA_LENGTH = POINT_ID_LENGTH + VALUE_ID_LENGTH + VALUE_SCORE_LENGTH

        fun fromByteArray(byteArray: ByteArray): PointValueInfo {
            val pointId = byteArray.sliceArray(0 until POINT_ID_LENGTH).toInt()
            val valueId = byteArray.sliceArray(POINT_ID_LENGTH until (POINT_ID_LENGTH + VALUE_ID_LENGTH)).toInt()
            val valueScore = byteArray.sliceArray((POINT_ID_LENGTH + VALUE_ID_LENGTH) until DATA_LENGTH).toDouble()
            return PointValueInfo(pointId, valueId, valueScore)
        }
    }

    override val key: Int
        get() {
            return this.pointId
        }

    override fun toByteArray(): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        byteArrayOutputStream.write(this.pointId.toByteArray())
        byteArrayOutputStream.write(this.valueId.toByteArray())
        byteArrayOutputStream.write(this.valueScore.toBits().toByteArray())
        return byteArrayOutputStream.toByteArray()
    }
}