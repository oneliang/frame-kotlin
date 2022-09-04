package com.oneliang.ktx.frame.container

internal class SlaveUnregisterRequest : BaseData() {
    companion object

}

internal fun SlaveUnregisterRequest.Companion.build(id: String): SlaveUnregisterRequest {
    val slaveUnregisterRequest = SlaveUnregisterRequest()
    slaveUnregisterRequest.action = ConstantsContainer.Action.SLAVE_UNREGISTER
    slaveUnregisterRequest.id = id
    return slaveUnregisterRequest
}