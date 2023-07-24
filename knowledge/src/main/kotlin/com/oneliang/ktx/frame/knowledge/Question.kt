package com.oneliang.ktx.frame.knowledge

import com.oneliang.ktx.Constants

class Question {
    var key: String = Constants.String.BLANK
    var config: Map<String, String> = emptyMap()
    var operator: Map<String, Array<String>> = emptyMap()
    var repeat: Int = 1

    companion object {
        const val KEY_CONFIG = "config"
        const val KEY_OPERATOR = "operator"
    }
}