package com.oneliang.ktx.frame.locker

import com.oneliang.ktx.Constants

class LockResponse {
    companion object

    var action = Constants.String.BLANK
    var id = Constants.String.BLANK
    var success = false
}

fun LockResponse.Companion.build(action: String, id: String, success: Boolean): LockResponse {
    val lockResponse = LockResponse()
    lockResponse.action = action
    lockResponse.id = id
    lockResponse.success = success
    return lockResponse
}

fun LockResponse.Companion.buildTryLockResponse(id: String, success: Boolean): LockResponse {
    return build(ConstantsLock.Action.TRY_LOCK, id, success)
}

fun LockResponse.Companion.buildReleaseLockResponse(id: String, success: Boolean): LockResponse {
    return build(ConstantsLock.Action.RELEASE_LOCK, id, success)
}