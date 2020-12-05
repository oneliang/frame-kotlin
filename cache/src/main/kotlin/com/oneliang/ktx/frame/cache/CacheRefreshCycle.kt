package com.oneliang.ktx.frame.cache

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.*
import java.util.*

enum class CacheRefreshCycle(val timeFormat: String) {
    NONE(Constants.String.ZERO), DAY("yyyyMMdd"), HOUR("yyyyMMddHH"), MINUTE("yyyyMMddHHmm");

    companion object {
        /**
         * get format time
         * @return Pair<String, String>
         */
        fun formatTime(cacheRefreshCycle: CacheRefreshCycle): Pair<String, String> {
            return when (cacheRefreshCycle) {
                DAY, HOUR, MINUTE -> {
                    val currentDate = Date()
                    val previousDateString = when (cacheRefreshCycle) {
                        DAY -> {
                            currentDate.getDayZeroTimePrevious(1).toUtilDate().toFormatString(cacheRefreshCycle.timeFormat)
                        }
                        HOUR -> {
                            currentDate.getHourZeroTimePrevious(1).toUtilDate().toFormatString(cacheRefreshCycle.timeFormat)
                        }
                        else -> {
                            currentDate.getMinuteZeroTimePrevious(1).toUtilDate().toFormatString(cacheRefreshCycle.timeFormat)
                        }
                    }
                    val currentDateString = currentDate.toFormatString(cacheRefreshCycle.timeFormat)
                    previousDateString to currentDateString
                }
                else -> {
                    cacheRefreshCycle.timeFormat to cacheRefreshCycle.timeFormat
                }
            }
        }
    }
}