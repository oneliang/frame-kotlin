package com.oneliang.ktx.frame.container

import com.oneliang.ktx.util.common.toByteArray

object ConstantsContainer {
    object Action {
        const val NONE = "none"
        const val SLAVE_REGISTER = "slaveRegister"
        const val SLAVE_UNREGISTER = "slaveUnregister"
        const val SLAVE_DATA = "slaveData"
        const val MASTER_NOTIFY_CONFIG_CHANGED = "masterNotifyConfigChanged"
    }

    object TlvPackageType {
        val NONE = 0.toByteArray()
        val SLAVE_REGISTER = 1.toByteArray()
        val SLAVE_UNREGISTER = 2.toByteArray()
        val SLAVE_DATA = 3.toByteArray()
        val MASTER_NOTIFY_CONFIG_CHANGED = 4.toByteArray()
    }
}