package com.oneliang.ktx.frame.rpc

import com.oneliang.ktx.Constants

internal class LookupProviderRequest : BaseData() {
    companion object

    var clusterKey = Constants.String.BLANK
}

internal fun LookupProviderRequest.Companion.build(id: String, clusterKey: String): LookupProviderRequest {
    val lookupProviderRequest = LookupProviderRequest()
    lookupProviderRequest.action = ConstantsRemoteProcessCall.Action.LOOKUP_PROVIDER
    lookupProviderRequest.id = id
    lookupProviderRequest.clusterKey = clusterKey
    return lookupProviderRequest
}