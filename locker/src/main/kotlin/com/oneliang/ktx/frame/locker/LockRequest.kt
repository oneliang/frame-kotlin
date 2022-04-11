package com.oneliang.ktx.frame.locker

import com.oneliang.ktx.Constants

class LockRequest {
    companion object

    var action = Constants.String.BLANK
    var lockKey = Constants.String.BLANK
    var id = Constants.String.BLANK
}

fun LockRequest.Companion.build(action: String, lockKey: String, id: String): LockRequest {
    val lockRequest = LockRequest()
    lockRequest.action = action
    lockRequest.lockKey = lockKey
    lockRequest.id = id
    return lockRequest
}

fun LockRequest.Companion.buildTryLockRequest(lockKey: String, id: String): LockRequest {
    return build(ConstantsLock.Action.TRY_LOCK, lockKey, id)
}

fun LockRequest.Companion.buildReleaseLockRequest(lockKey: String, id: String): LockRequest {
    return build(ConstantsLock.Action.RELEASE_LOCK, lockKey, id)
}