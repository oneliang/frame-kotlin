package com.oneliang.ktx.frame.updater.tomcat

import com.jcraft.jsch.Session
import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ssh.Ssh
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.json.jsonToObject
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File

class TomcatAutoUpdater(private val configuration: Configuration) {
    companion object {
        private val logger = LoggerManager.getLogger(TomcatAutoUpdater::class)
        private fun killTomcatProcess(session: Session, war: Configuration.War) {
            var tomcatPid = 0
            Ssh.exec(session, "ps -ef|grep ${war.remoteTomcatDirectory}") {
                it.inputStream.readContentIgnoreLine { line ->
                    val found = line.finds(war.remoteTomcatDirectory)
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
            logger.info("tomcat:[%s], pid:[%s]", war.remoteTomcatDirectory, tomcatPid)
            if (tomcatPid > 0) {
                logger.info("kill the tomcat process, tomcat:[%s], pid:[%s]", war.remoteTomcatDirectory, tomcatPid)
                Ssh.exec(session, "kill -9 $tomcatPid")
            }
        }

        private fun uploadWar(session: Session, war: Configuration.War) {
            val localWarFile = File(war.localWarFile)
            val localWarFullFilename = localWarFile.absolutePath
            logger.info("upload local war:[%s], to remote war:[%s]", localWarFullFilename, war.remoteWarFullFilename)
            if (!localWarFile.exists()) {
                logger.error("file not exists, file:${localWarFullFilename} ")
                return
            }
            Ssh.sftp(session) { channelSftp ->
                val warDirectory = war.remoteWarDirectory
                perform({
                    channelSftp.cd(warDirectory)
                }, failure = { e ->
                    channelSftp.inputStream?.also {
                        logger.error(Ssh.decodeInputStream(it), e)
                    }
                    channelSftp.extInputStream?.also {
                        logger.info(Ssh.decodeInputStream(it), e)
                    }
                    channelSftp.mkdir(warDirectory)
                })
                channelSftp.put(localWarFullFilename, war.remoteWarFullFilename)
            }
        }

        private fun unzipWar(session: Session, war: Configuration.War) {
            Ssh.exec(session, "unzip -d ${war.remoteWarDirectory} ${war.remoteWarFullFilename}") {
                logger.info(Ssh.decodeInputStream(it.inputStream))
            }
        }

        private fun startupTomcat(session: Session, war: Configuration.War) {
            logger.info("startup the tomcat process, tomcat:[%s]", war.remoteTomcatDirectory)
            Ssh.exec(session, war.remoteTomcatStartup) {
                logger.info(Ssh.decodeInputStream(it.inputStream))
            }
        }
    }

    class Configuration {
        companion object {
            fun fromJson(json: String): Configuration {
                return json.jsonToObject(Configuration::class)
            }
        }

        var host = Constants.String.BLANK
        var port = 22
        var user = Constants.String.BLANK
        var password = Constants.String.BLANK
        var warArray = emptyArray<War>()

        class War {
            var localWarFile = Constants.String.BLANK
            var remoteTomcatDirectory = Constants.String.BLANK
            val remoteTomcatStartup: String
                get() = "$remoteTomcatDirectory/bin/startup.sh"
            val remoteWarDirectory: String
                get() = "$remoteTomcatDirectory/webapps"
            var remoteWarName = Constants.String.BLANK
            val remoteWarFullFilename: String
                get() = "$remoteWarDirectory/$remoteWarName"
        }
    }

    fun update() {
        val begin = System.currentTimeMillis()
        Ssh.connect(host = this.configuration.host,
                user = this.configuration.user,
                port = this.configuration.port,
                password = this.configuration.password,
                configurationMap = mapOf(Ssh.Configuration.USERAUTH_GSSAPI_WITH_MIC to "no", Ssh.Configuration.STRICT_HOST_KEY_CHECKING to "no"), afterSessionConnect = { session ->
            this.configuration.warArray.forEach {
                killTomcatProcess(session, it)
                uploadWar(session, it)
//                unzipWar(session, it)
                startupTomcat(session, it)
            }
            session.disconnect()
        })
        logger.info("update cost:%s", (System.currentTimeMillis() - begin))
    }
}