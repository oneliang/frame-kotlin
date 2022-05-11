package com.oneliang.ktx.frame.rpc

import com.oneliang.ktx.Constants

internal class LookupProviderResponse : BaseData() {
    companion object

    var success = false
    var provider: Provider? = null
}

class Provider(var host: String = Constants.String.BLANK, var port: Int = 0) {
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("provider:")
        stringBuilder.append("host[$host],")
        stringBuilder.append("port[$port]")
        return stringBuilder.toString()
    }
}

internal fun LookupProviderResponse.Companion.build(id: String, success: Boolean): LookupProviderResponse {
    val lookupProviderResponse = LookupProviderResponse()
    lookupProviderResponse.action = ConstantsRemoteProcessCall.Action.LOOKUP_PROVIDER
    lookupProviderResponse.id = id
    lookupProviderResponse.success = success
    return lookupProviderResponse
}