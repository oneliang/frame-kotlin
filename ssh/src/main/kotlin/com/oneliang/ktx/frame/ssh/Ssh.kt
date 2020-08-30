package com.oneliang.ktx.frame.ssh

import com.jcraft.jsch.*
import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.perform
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayOutputStream
import java.io.InputStream

object Ssh {
    private val logger = LoggerManager.getLogger(Ssh::class)

    object Configuration {
        const val USERAUTH_GSSAPI_WITH_MIC = "userauth.gssapi-with-mic"
        const val STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking"
    }

    fun connect(host: String, port: Int = 22, user: String, password: String, configurationMap: Map<String, String> = emptyMap(), connectTimeout: Int = 0, afterSessionConnect: (session: Session) -> Unit): Session {
        val jSch = JSch()
        val session = jSch.getSession(user, host, port)
        session.setPassword(password)
        configurationMap.forEach { (key, value) ->
            session.setConfig(key, value);
        }
        perform({
            session.connect(connectTimeout)
            afterSessionConnect(session)
        }, failure = {
            logger.error(Constants.Base.EXCEPTION, it)
        })
        return session
    }

    fun shell(session: Session) {
        val channelShell = session.openChannel("shell")
        channelShell as ChannelShell
        channelShell.inputStream = System.`in`
        channelShell.outputStream = System.out
        channelShell.connect()
//        channelShell.disconnect()
    }

    fun exec(session: Session, command: String, afterChannelConnect: (channelExec: ChannelExec) -> Unit = {}) {
        val channelExec = session.openChannel("exec")
        logger.info("exec command:[%s]", command)
        (channelExec as ChannelExec).setCommand(command)
        channelExec.setInputStream(null)
        channelExec.setErrStream(System.err)
        perform({
            channelExec.connect()
            afterChannelConnect(channelExec)
        }, failure = {
            logger.error(decodeInputStream(channelExec.inputStream), it)
        })
        channelExec.disconnect()
    }

    fun sftp(session: Session, afterChannelConnect: (channelSftp: ChannelSftp) -> Unit = {}) {
        val channelSftp = session.openChannel("sftp")
        channelSftp as ChannelSftp
        perform({
            channelSftp.connect()
            afterChannelConnect(channelSftp)
        }, failure = {
            logger.error(decodeInputStream(channelSftp.inputStream), it)
        })
        channelSftp.disconnect()
    }

    fun decodeInputStream(inputStream: InputStream): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        inputStream.copyTo(byteArrayOutputStream)
        return String(byteArrayOutputStream.toByteArray()).trim()
    }
}