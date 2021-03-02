package com.oneliang.ktx.frame.websocket

import com.oneliang.ktx.util.logging.LoggerManager
import javax.websocket.*

abstract class AbstractServerEndpoint {

    companion object {
        private val logger = LoggerManager.getLogger(AbstractServerEndpoint::class)
    }

    @Throws(Throwable::class)
    abstract fun onOpen(session: Session, endpointConfig: EndpointConfig)

    @Throws(Throwable::class)
    abstract fun onMessage(session: Session, message: String)

    @Throws(Throwable::class)
    open fun onError(e: Throwable) {
        logger.error("web socket error", e)
    }

    @Throws(Throwable::class)
    open fun onClose(session: Session, closeReason: CloseReason?) {
        logger.debug("session id:%s, close code:%s, reason:%s", session.id, closeReason?.closeCode, closeReason?.reasonPhrase)
        session.close()
    }

    fun send(session: Session, byteArray: ByteArray) {
        session.basicRemote.sendStream.write(byteArray)
    }

    fun send(session: Session, message: String) {
        send(session, message.toByteArray())
    }

    fun handle(session: Session, requestString: String, block: (requestString: String) -> String) {
        val responseString = block(requestString)
        send(session, responseString)
    }

    fun handle(session: Session, requestByteArray: ByteArray, block: (requestByteArray: ByteArray) -> ByteArray) {
        val responseByteArray = block(requestByteArray)
        send(session, responseByteArray)
    }
}