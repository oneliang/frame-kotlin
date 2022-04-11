package com.oneliang.ktx.frame.locker

import com.oneliang.ktx.util.common.toByteArray

object ConstantsLock {
    object Action {
        const val TRY_LOCK = "tryLock"
        const val RELEASE_LOCK = "releaseLock"
    }

    object TlvPackageType {
        val REQUEST_RESPONSE = 1.toByteArray()
        val NOTIFY = 2.toByteArray()
    }
}