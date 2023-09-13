package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants

open class MappingIndexBean {
    companion object {
        const val TAG_INDEX = "index"
    }

    var columns: String = Constants.String.BLANK
    var otherCommands: String = Constants.String.BLANK
}
