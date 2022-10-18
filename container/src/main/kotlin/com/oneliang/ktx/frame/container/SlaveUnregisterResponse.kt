package com.oneliang.ktx.frame.container

internal class SlaveUnregisterResponse : BaseData() {
    companion object

    var success = false
}

internal fun SlaveUnregisterResponse.Companion.build(id: String, success: Boolean): SlaveUnregisterResponse {
    val slaveUnregisterResponse = SlaveUnregisterResponse()
    slaveUnregisterResponse.action = ConstantsContainer.Action.SLAVE_UNREGISTER
    slaveUnregisterResponse.id = id
    slaveUnregisterResponse.success = success
    return slaveUnregisterResponse
}