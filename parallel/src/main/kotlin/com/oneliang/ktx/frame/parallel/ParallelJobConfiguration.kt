package com.oneliang.ktx.frame.parallel

import com.oneliang.ktx.Constants

class ParallelJobConfiguration {
    companion object {
        val DEFAULT = ParallelJobConfiguration()
    }

    var timeoutAfterFinished = 200L
    var async = true
    var useCache = false
    var cacheDirectory = Constants.String.BLANK
}