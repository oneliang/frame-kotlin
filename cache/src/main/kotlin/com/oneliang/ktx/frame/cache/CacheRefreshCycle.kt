package com.oneliang.ktx.frame.cache

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.getDayZeroTimePrevious
import com.oneliang.ktx.util.common.getHourZeroTimePrevious
import com.oneliang.ktx.util.common.toFormatString
import com.oneliang.ktx.util.common.toUtilDate
import java.util.*

enum class CacheRefreshCycle(val timeFormat: String) {
    NONE(Constants.String.ZERO), DAY("yyyyMMdd"), HOUR("yyyyMMddHH");

    companion object {
        /**
         * get format time
         * @return Pair<String, String>
         */
        fun formatTime(cacheRefreshCycle: CacheRefreshCycle): Pair<String, String> {
            return when (cacheRefreshCycle) {
                NONE -> {
                    cacheRefreshCycle.timeFormat to cacheRefreshCycle.timeFormat
                }
                DAY -> {
                    val currentDate = Date()
                    val previousDateString = currentDate.getDayZeroTimePrevious(1).toUtilDate().toFormatString(cacheRefreshCycle.timeFormat)
                    val currentDateString = currentDate.toFormatString(cacheRefreshCycle.timeFormat)
                    previousDateString to currentDateString
                }
                else -> {//hour
                    val currentDate = Date()
                    val previousDateString = currentDate.getHourZeroTimePrevious(1).toUtilDate().toFormatString(cacheRefreshCycle.timeFormat)
                    val currentDateString = currentDate.toFormatString(cacheRefreshCycle.timeFormat)
                    previousDateString to currentDateString
                }
            }
        }
    }

}