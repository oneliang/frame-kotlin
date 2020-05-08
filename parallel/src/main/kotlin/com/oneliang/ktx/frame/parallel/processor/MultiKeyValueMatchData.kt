package com.oneliang.ktx.frame.parallel.processor

import com.oneliang.ktx.Constants

val EMPTY_MULTI_KEY_VALUE_MATCH_DATA = MultiKeyValueMatchData()

class MultiKeyValueMatchData {
    var complexKey = Constants.String.BLANK
    var keyValueMatchMap = emptyMap<String, String>()
    var dataMap = emptyMap<String, String>()
}