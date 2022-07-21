package com.oneliang.ktx.frame.container

internal class SlaveRegisterResponse : BaseData() {
    companion object

    var success = false
}

internal fun SlaveRegisterResponse.Companion.build(id: String, success: Boolean): SlaveRegisterResponse {
    val slaveRegisterResponse = SlaveRegisterResponse()
    slaveRegisterResponse.action = ConstantsContainer.Action.SLAVE_REGISTER
    slaveRegisterResponse.id = id
    slaveRegisterResponse.success = success
    return slaveRegisterResponse
}