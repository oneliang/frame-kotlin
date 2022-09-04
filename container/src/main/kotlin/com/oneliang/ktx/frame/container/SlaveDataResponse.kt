package com.oneliang.ktx.frame.container

internal class SlaveDataResponse : BaseData() {
    companion object

    var success = false
}

internal fun SlaveDataResponse.Companion.build(id: String, success: Boolean): SlaveDataResponse {
    val slaveDataResponse = SlaveDataResponse()
    slaveDataResponse.action = ConstantsContainer.Action.SLAVE_REGISTER
    slaveDataResponse.id = id
    slaveDataResponse.success = success
    return slaveDataResponse
}