package com.oneliang.ktx.frame.test

import com.jcraft.jsch.Session
import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ssh.Ssh
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

class TomcatAutoUpdater {
    companion object {
        private val logger = LoggerManager.getLogger(TomcatAutoUpdater::class)
    }

    class Configuration {
        var host = Constants.String.BLANK
        var port = 22
        var user = Constants.String.BLANK
        var password = Constants.String.BLANK
        var tomcatDirectory = Constants.String.BLANK
        val tomcatStartUp: String
            get() = "$tomcatDirectory/bin/startup.sh"
        var localWarFullFilename = Constants.String.BLANK
        val remoteWarDirectory: String
            get() = "$tomcatDirectory/webapps"
        var remoteWarName = Constants.String.BLANK
        val remoteWarFullFilename: String
            get() = "$remoteWarDirectory/$remoteWarName"
    }

    fun update(configuration: Configuration) {
        val begin = System.currentTimeMillis()
        Ssh.connect(configuration.host,
                user = configuration.user,
                port = configuration.port,
                password = configuration.password,
                configurationMap = mapOf(Ssh.Configuration.USERAUTH_GSSAPI_WITH_MIC to "no", Ssh.Configuration.STRICT_HOST_KEY_CHECKING to "no"), afterSessionConnect = { session ->
            this.killTomcatProcess(session, configuration)
//            this.uploadWar(session, configuration)
//            this.unzipWar(session, configuration)
            this.startupTomcat(session, configuration)
            session.disconnect()
        })
        logger.info("update cost:%s", (System.currentTimeMillis() - begin))
    }

    private fun killTomcatProcess(session: Session, configuration: Configuration) {
        var tomcatPid = 0
        Ssh.exec(session, "ps -ef|grep ${configuration.tomcatDirectory}") {
            it.inputStream.readContentIgnoreLine { line ->
                val found = line.finds(configuration.tomcatDirectory)
                if (found) {
                    logger.info(line)
                    val stringList = line.splitForWhitespace()
                    if (stringList.size > 1) {
                        tomcatPid = stringList[1].toIntSafely()
                    }
                    return@readContentIgnoreLine false
                }
                true
            }
        }
        logger.info("tomcat:[%s], pid:[%s]", configuration.tomcatDirectory, tomcatPid)
        if (tomcatPid > 0) {
            logger.info("kill the tomcat process, tomcat:[%s], pid:[%s]", configuration.tomcatDirectory, tomcatPid)
            Ssh.exec(session, "kill -9 $tomcatPid")
        }
    }

    private fun uploadWar(session: Session, configuration: Configuration) {
        logger.info("upload local war:[%s], to remote war:[%s]", configuration.localWarFullFilename, configuration.remoteWarFullFilename)
        Ssh.sftp(session) { channelSftp ->
            val warDirectory = configuration.remoteWarDirectory
            perform({
                channelSftp.cd(warDirectory)
            }, failure = {
                logger.error(Ssh.decodeInputStream(channelSftp.inputStream), it)
                channelSftp.mkdir(warDirectory)
            })
            channelSftp.put(configuration.localWarFullFilename, configuration.remoteWarFullFilename)
        }
    }

    private fun unzipWar(session: Session, configuration: Configuration) {
        Ssh.exec(session, "unzip -d ${configuration.remoteWarDirectory} ${configuration.remoteWarFullFilename}") {
            logger.info(Ssh.decodeInputStream(it.inputStream))
        }
    }

    private fun startupTomcat(session: Session, configuration: Configuration) {
        logger.info("startup the tomcat process, tomcat:[%s]", configuration.tomcatDirectory)
        Ssh.exec(session, configuration.tomcatStartUp) {
            logger.info(Ssh.decodeInputStream(it.inputStream).trim())
        }
    }
}