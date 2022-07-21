package com.oneliang.ktx.frame.container

internal class SlaveRegisterRequest : BaseData() {
    companion object

}

internal fun SlaveRegisterRequest.Companion.build(id: String): SlaveRegisterRequest {
    val slaveRegisterRequest = SlaveRegisterRequest()
    slaveRegisterRequest.action = ConstantsContainer.Action.SLAVE_REGISTER
    slaveRegisterRequest.id = id
    return slaveRegisterRequest
}