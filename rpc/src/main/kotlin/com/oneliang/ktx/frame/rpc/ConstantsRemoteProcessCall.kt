package com.oneliang.ktx.frame.rpc

import com.oneliang.ktx.util.common.toByteArray

internal object ConstantsRemoteProcessCall {
    object Action {
        const val NONE = "none"
        const val REGISTER = "register"
        const val LOOKUP_PROVIDER = "lookup_provider"
    }

    object TlvPackageType {
        val NONE = 0.toByteArray()
        val REGISTER = 1.toByteArray()
        val LOOKUP_PROVIDER = 2.toByteArray()
    }
}