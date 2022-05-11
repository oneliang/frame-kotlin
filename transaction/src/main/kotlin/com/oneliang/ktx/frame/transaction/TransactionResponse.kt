package com.oneliang.ktx.frame.transaction

import com.oneliang.ktx.Constants

class TransactionResponse {
    companion object

    var action = Constants.String.BLANK
    var masterId = Constants.String.BLANK
    var id = Constants.String.BLANK
    var success = false
}

fun TransactionResponse.Companion.build(action: String, masterId: String, id: String, success: Boolean): TransactionResponse {
    val lockResponse = TransactionResponse()
    lockResponse.action = action
    lockResponse.masterId = masterId
    lockResponse.id = id
    lockResponse.success = success
    return lockResponse
}

fun TransactionResponse.Companion.buildBeginTransactionResponse(masterId: String, id: String, success: Boolean): TransactionResponse {
    return build(ConstantsTransaction.Action.BEGIN_TRANSACTION, masterId, id, success)
}

fun TransactionResponse.Companion.buildEndTransactionResponse(masterId: String, id: String, success: Boolean): TransactionResponse {
    return build(ConstantsTransaction.Action.END_TRANSACTION, masterId, id, success)
}

fun TransactionResponse.Companion.buildRollbackTransactionResponse(masterId: String, id: String, success: Boolean): TransactionResponse {
    return build(ConstantsTransaction.Action.ROLLBACK_TRANSACTION, masterId, id, success)
}

fun TransactionResponse.Companion.buildExecuteInTransactionResponse(masterId: String, id: String, success: Boolean): TransactionResponse {
    return build(ConstantsTransaction.Action.EXECUTE_IN_TRANSACTION, masterId, id, success)
}