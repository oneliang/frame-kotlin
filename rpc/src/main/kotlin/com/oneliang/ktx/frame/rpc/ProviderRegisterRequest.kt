package com.oneliang.ktx.frame.rpc

import com.oneliang.ktx.Constants

internal class ProviderRegisterRequest : BaseData() {
    companion object

    var clusterKey = Constants.String.BLANK
    var host = Constants.String.BLANK
    var port = 0
}

internal fun ProviderRegisterRequest.Companion.build(id: String, clusterKey: String, host: String, port: Int): ProviderRegisterRequest {
    val providerRegisterRequest = ProviderRegisterRequest()
    providerRegisterRequest.action = ConstantsRemoteProcessCall.Action.REGISTER
    providerRegisterRequest.id = id
    providerRegisterRequest.clusterKey = clusterKey
    providerRegisterRequest.host = host
    providerRegisterRequest.port = port
    return providerRegisterRequest
}