package com.oneliang.ktx.frame.cache

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.*
import java.util.*

enum class CacheRefreshCycle(val timeFormat: String) {
    NONE(Constants.String.ZERO), DAY("yyyyMMdd"), HOUR("yyyyMMddHH"), MINUTE("yyyyMMddHHmm");

    companion object {
        /**
         * get format time
         * @return String
         */
        fun formatTime(cacheRefreshCycle: CacheRefreshCycle): String {
            return when (cacheRefreshCycle) {
                DAY, HOUR, MINUTE -> {
                    val currentDate = Date()
                    val currentDateString = currentDate.toFormatString(cacheRefreshCycle.timeFormat)
                    currentDateString
                }
                else -> {
                    cacheRefreshCycle.timeFormat
                }
            }
        }
    }
}