package com.oneliang.ktx.frame.rpc

internal class ProviderRegisterResponse : BaseData() {
    companion object

    var success = false
}

internal fun ProviderRegisterResponse.Companion.build(id: String, success: Boolean): ProviderRegisterResponse {
    val providerRegisterResponse = ProviderRegisterResponse()
    providerRegisterResponse.action = ConstantsRemoteProcessCall.Action.REGISTER
    providerRegisterResponse.id = id
    providerRegisterResponse.success = success
    return providerRegisterResponse
}