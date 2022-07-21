package com.oneliang.ktx.frame.container

import com.oneliang.ktx.Constants

internal open class BaseData {
    companion object

    var id = Constants.String.BLANK
    var action = Constants.String.BLANK
}

internal fun BaseData.Companion.build(id: String, action: String): BaseData {
    val baseData = BaseData()
    baseData.id = id
    baseData.action = action
    return baseData
}