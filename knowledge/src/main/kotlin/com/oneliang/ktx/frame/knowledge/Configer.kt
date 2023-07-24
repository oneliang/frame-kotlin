package com.oneliang.ktx.frame.knowledge

import com.oneliang.ktx.Constants

object Configer {

    enum class Type(val value: String) {
        RANDOM_INT("random_int"),
        RANDOM_FLOAT("random_float"),
        RANDOM_STRING("random_string"),
        RANDOM_TIMESTAMP("random_timestamp")
    }

    /**
     * random int
     * @param max
     * @param min
     * @return Int
     */
    fun randomInt(max: Int, min: Int = 0): Int {
        return (Math.random() * (max - min) + min).toInt()
    }

    /**
     * random float
     * @param max
     * @param min
     * @return Float
     */
    fun randomFloat(max: Float, min: Float = 0.0f): Float {
        return (Math.random() * (max - min) + min).toFloat()
    }

    /**
     * random timestamp
     * @param max
     * @param min
     * @return Long
     */
    fun randomTimestamp(max: Int, min: Int = 0): Long {
        val currentTimeMillis = System.currentTimeMillis()
        val randomMinute = ((Math.random() * (max - min) + min) * Constants.Time.MILLISECONDS_OF_MINUTE).toLong()
        return currentTimeMillis + randomMinute
    }

    /**
     * get value by config type
     * @param value
     * @param globalMap
     * @return Any
     */
    fun getValueByConfigType(value: String, globalMap: Map<String, Array<String>>): Any {
        val valueList = value.split(Constants.Symbol.COLON)
        val valueKey = valueList[0]
        return when (valueKey) {
            Type.RANDOM_INT.value -> {
                val min = valueList[1].toInt()
                val max = valueList[2].toInt()
                randomInt(max, min)
            }

            Type.RANDOM_FLOAT.value -> {
                val min = valueList[1].toFloat()
                val max = valueList[2].toFloat()
                randomFloat(max, min)
            }

            Type.RANDOM_TIMESTAMP.value -> {
                val min = valueList[1].toInt()
                val max = valueList[2].toInt()
                randomTimestamp(max, min)
            }

            Type.RANDOM_STRING.value -> {
                (globalMap[valueList[1]] ?: arrayOf(Constants.String.BLANK)).random()
            }

            else -> {
                Constants.String.BLANK
            }
        }
    }
}