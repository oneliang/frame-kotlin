package com.oneliang.ktx.frame.container

import com.oneliang.ktx.util.common.toByteArray

object ConstantsContainer {
    object Action {
        const val NONE = "none"
        const val SLAVE_REGISTER = "slaveRegister"
    }

    object TlvPackageType {
        val NONE = 0.toByteArray()
        val SLAVE_REGISTER = 1.toByteArray()
    }
}