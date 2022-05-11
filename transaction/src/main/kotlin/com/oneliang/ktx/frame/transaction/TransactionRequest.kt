package com.oneliang.ktx.frame.transaction

import com.oneliang.ktx.Constants

class TransactionRequest {
    companion object

    var action = Constants.String.BLANK
    var masterId = Constants.String.BLANK
    var id = Constants.String.BLANK
}

fun TransactionRequest.Companion.build(action: String, masterId: String, id: String): TransactionRequest {
    val lockRequest = TransactionRequest()
    lockRequest.action = action
    lockRequest.masterId = masterId
    lockRequest.id = id
    return lockRequest
}

fun TransactionRequest.Companion.buildBeginTransactionRequest(masterId: String, id: String): TransactionRequest {
    return build(ConstantsTransaction.Action.BEGIN_TRANSACTION, masterId, id)
}

fun TransactionRequest.Companion.buildEndTransactionRequest(masterId: String, id: String): TransactionRequest {
    return build(ConstantsTransaction.Action.END_TRANSACTION, masterId, id)
}

fun TransactionRequest.Companion.buildRollbackTransactionRequest(masterId: String, id: String): TransactionRequest {
    return build(ConstantsTransaction.Action.ROLLBACK_TRANSACTION, masterId, id)
}

fun TransactionRequest.Companion.buildExecuteInTransactionRequest(masterId: String, id: String): TransactionRequest {
    return build(ConstantsTransaction.Action.EXECUTE_IN_TRANSACTION, masterId, id)
}