package com.oneliang.ktx.frame.updater.tomcat

import com.jcraft.jsch.Session
import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ssh.Ssh
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.json.jsonToObject
import com.oneliang.ktx.util.logging.LoggerManager

class TomcatAutoUpdater(private val configuration: Configuration) {
    companion object {
        private val logger = LoggerManager.getLogger(TomcatAutoUpdater::class)
        private fun killTomcatProcess(session: Session, configuration: Configuration) {
            var tomcatPid = 0
            Ssh.exec(session, "ps -ef|grep ${configuration.remoteTomcatDirectory}") {
                it.inputStream.readContentIgnoreLine { line ->
                    val found = line.finds(configuration.remoteTomcatDirectory)
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
            logger.info("tomcat:[%s], pid:[%s]", configuration.remoteTomcatDirectory, tomcatPid)
            if (tomcatPid > 0) {
                logger.info("kill the tomcat process, tomcat:[%s], pid:[%s]", configuration.remoteTomcatDirectory, tomcatPid)
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
            logger.info("startup the tomcat process, tomcat:[%s]", configuration.remoteTomcatDirectory)
            Ssh.exec(session, configuration.remoteTomcatStartup) {
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
        var localWarFullFilename = Constants.String.BLANK
        var remoteTomcatDirectory = Constants.String.BLANK
        val remoteTomcatStartup: String
            get() = "$remoteTomcatDirectory/bin/startup.sh"
        val remoteWarDirectory: String
            get() = "$remoteTomcatDirectory/webapps"
        var remoteWarName = Constants.String.BLANK
        val remoteWarFullFilename: String
            get() = "$remoteWarDirectory/$remoteWarName"
    }

    fun update() {
        val begin = System.currentTimeMillis()
        Ssh.connect(this.configuration.host,
                user = this.configuration.user,
                port = this.configuration.port,
                password = this.configuration.password,
                configurationMap = mapOf(Ssh.Configuration.USERAUTH_GSSAPI_WITH_MIC to "no", Ssh.Configuration.STRICT_HOST_KEY_CHECKING to "no"), afterSessionConnect = { session ->
            killTomcatProcess(session, this.configuration)
//            uploadWar(session, this.configuration)
//            unzipWar(session, this.configuration)
            startupTomcat(session, this.configuration)
            session.disconnect()
        })
        logger.info("update cost:%s", (System.currentTimeMillis() - begin))
    }
}