package com.oneliang.ktx.frame.updater.tomcat

import com.jcraft.jsch.Session
import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ssh.Ssh
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.file.fileExists
import com.oneliang.ktx.util.json.jsonToObject
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File

class TomcatAutoUpdater(private val configuration: Configuration) {
    companion object {
        private val logger = LoggerManager.getLogger(TomcatAutoUpdater::class)

        private fun backupWar(session: Session, war: Configuration.War) {
            Ssh.exec(session, "mkdir ${war.remoteTomcatBakDirectory}")
            Ssh.exec(session, "cp ${war.remoteWarFullFilename} ${war.remoteTomcatBakDirectory}${Constants.Symbol.SLASH_LEFT}${war.remoteWarName}")
        }

        private fun revertWar(session: Session, war: Configuration.War) {
            Ssh.exec(session, "cp ${war.remoteTomcatBakDirectory}${Constants.Symbol.SLASH_LEFT}${war.remoteWarName} ${war.remoteTomcatWebAppDirectory}${Constants.Symbol.SLASH_LEFT}${war.remoteWarName}")
        }

        private fun findTomcatProcessPid(session: Session, war: Configuration.War): List<Int> {
            val tomcatPidList = mutableListOf<Int>()
            Ssh.exec(session, "ps -ef|grep ${war.remoteTomcatDirectory}") {
                it.inputStream.readContentIgnoreLine { line ->
                    val found = line.finds(war.remoteTomcatDirectory)
                    if (found) {
                        logger.info(line)
                        val stringList = line.splitForWhitespace()
                        if (stringList.size > 1) {
                            tomcatPidList += stringList[1].toIntSafely()
                        }
                    }
                    true
                }
            }
            return tomcatPidList
        }

        private fun killTomcatProcess(session: Session, war: Configuration.War, tomcatPidList: List<Int>) {
            tomcatPidList.forEach { tomcatPid ->
                logger.info("tomcat:[%s], pid:[%s]", war.remoteTomcatDirectory, tomcatPid)
                if (tomcatPid > 0) {
                    logger.info("kill the tomcat process, tomcat:[%s], pid:[%s]", war.remoteTomcatDirectory, tomcatPid)
                    Ssh.exec(session, "kill -9 $tomcatPid")
                }
            }
        }

        private fun uploadWarForRetry(session: Session, war: Configuration.War): Boolean {
            logger.info("upload local war:[%s], to remote war:[%s]", war.localWarFullFilename, war.remoteWarFullFilename)
            if (!war.localWarFullFilename.fileExists()) {
                logger.error("file not exists, file:${war.localWarFullFilename}")
                return false
            }
            var uploadResult = uploadWar(session, war)
            if (!uploadResult) {
                for (i in 1..war.uploadRetryCount) {
                    logger.info("upload retry:%s, remote war full filename:%s", i, war.remoteWarFullFilename)
                    uploadResult = uploadWar(session, war)
                    if (uploadResult) {
                        break
                    }
                }
            }
            return uploadResult
        }

        private fun uploadWar(session: Session, war: Configuration.War): Boolean {
            if (!war.localWarFullFilename.fileExists()) {
                logger.error("file not exists, file:${war.localWarFullFilename} ")
                return false
            }
            Ssh.sftp(session) { channelSftp ->
                val webAppDirectory = war.remoteTomcatWebAppDirectory
                try {
                    channelSftp.cd(webAppDirectory)
                } catch (e: Throwable) {
                    channelSftp.inputStream?.also {
                        logger.error(Ssh.decodeInputStream(it), e)
                    }
                    channelSftp.extInputStream?.also {
                        logger.info(Ssh.decodeInputStream(it), e)
                    }
                    channelSftp.mkdir(webAppDirectory)
                }
                channelSftp.put(war.localWarFullFilename, war.remoteWarFullFilename)
            }
            val remoteFileMd5 = execRemoteFileMd5(session, war.remoteWarFullFilename)
            return war.localWarFullFilename.toFile().MD5String().equals(remoteFileMd5, true)
        }

        private fun execRemoteFileMd5(session: Session, remoteFullFilename: String): String {
            var remoteFileMd5 = Constants.String.BLANK
            Ssh.exec(session, "md5sum $remoteFullFilename") {
                it.inputStream.readContentIgnoreLine { line ->
                    logger.info(line)
                    val stringList = line.splitForWhitespace()
                    if (stringList.isNotEmpty()) {
                        remoteFileMd5 = stringList[0]
                    }
                    return@readContentIgnoreLine false
                }
            }
            return remoteFileMd5
        }
    }

    private fun removeWarDirectory(session: Session, war: Configuration.War) {
        val warDirectoryName = war.remoteWarName.substring(0, war.remoteWarName.lastIndexOf(Constants.Symbol.DOT))
        val warDirectory = war.remoteTomcatWebAppDirectory + Constants.Symbol.SLASH_LEFT + warDirectoryName
        Ssh.exec(session, "rm -rf $warDirectory") {
            logger.info(Ssh.decodeInputStream(it.inputStream))
        }
    }

    private fun removeTomcatLogDirectory(session: Session, war: Configuration.War) {
        val logDirectory = war.remoteTomcatLogDirectory + Constants.Symbol.SLASH_LEFT + Constants.Symbol.WILDCARD
        Ssh.exec(session, "rm -rf $logDirectory") {
            logger.info(Ssh.decodeInputStream(it.inputStream))
        }
    }

    private fun unzipWar(session: Session, war: Configuration.War) {
        Ssh.exec(session, "unzip -d ${war.remoteTomcatWebAppDirectory} ${war.remoteWarFullFilename}") {
            logger.info(Ssh.decodeInputStream(it.inputStream))
        }
    }

    private fun startupTomcat(session: Session, war: Configuration.War) {
        logger.info("startup the tomcat process, tomcat:[%s]", war.remoteTomcatDirectory)
        Ssh.exec(session, war.remoteTomcatStartup) {
            logger.info(Ssh.decodeInputStream(it.inputStream))
        }
    }

    class Configuration {
        var host = Constants.String.BLANK
        var port = 22
        var user = Constants.String.BLANK
        var password = Constants.String.BLANK
        var warArray = emptyArray<War>()

        class War {
            var localWarFile = Constants.String.BLANK
                set(value) {
                    field = value
                    val file = File(field)
                    localWarFullFilename = file.absolutePath
                }
            var localWarFullFilename = Constants.String.BLANK
                private set
            var remoteTomcatDirectory = Constants.String.BLANK
            val remoteTomcatStartup: String
                get() = "$remoteTomcatDirectory/bin/startup.sh"
            val remoteTomcatWebAppDirectory: String
                get() = "$remoteTomcatDirectory/webapps"
            val remoteTomcatLogDirectory: String
                get() = "$remoteTomcatDirectory/logs"
            val remoteTomcatBakDirectory: String
                get() = "$remoteTomcatDirectory/bak"
            var remoteWarName = Constants.String.BLANK
            val remoteWarFullFilename: String
                get() = "$remoteTomcatWebAppDirectory/$remoteWarName"
            var uploadRetryCount = 2
        }
    }

    private fun sshConnect(afterSessionConnect: (session: Session) -> Unit) {
        val begin = System.currentTimeMillis()
        Ssh.connect(
            host = this.configuration.host,
            user = this.configuration.user,
            port = this.configuration.port,
            password = this.configuration.password,
            configurationMap = mapOf(Ssh.Configuration.USERAUTH_GSSAPI_WITH_MIC to "no", Ssh.Configuration.STRICT_HOST_KEY_CHECKING to "no"),
            afterSessionConnect = afterSessionConnect
        )
        logger.info("cost:%s", (System.currentTimeMillis() - begin))
    }

    fun update() {
        sshConnect { session ->
            this.configuration.warArray.forEach {
                backupWar(session, it)
                val tomcatPidList = findTomcatProcessPid(session, it)
                killTomcatProcess(session, it, tomcatPidList)
                val uploadResult = uploadWarForRetry(session, it)
                if (uploadResult) {
                    removeWarDirectory(session, it)
                    removeTomcatLogDirectory(session, it)
//                unzipWar(session, it)
                    startupTomcat(session, it)
                    val tomcatPidListAfterStartup = findTomcatProcessPid(session, it)
                    logger.info("after tomcat start up, tomcat pid list size:%s, tomcat:[%s]", tomcatPidListAfterStartup.size, it.remoteTomcatDirectory)
                } else {
                    startupTomcat(session, it)
                    logger.info("upload failure, just restart tomcat, tomcat:[%s]", it.remoteTomcatDirectory)
                    return@forEach
                }
            }
            session.disconnect()
        }
    }

    fun revert() {
        sshConnect { session ->
            this.configuration.warArray.forEach {
                val tomcatPidList = findTomcatProcessPid(session, it)
                killTomcatProcess(session, it, tomcatPidList)
                revertWar(session, it)
                removeWarDirectory(session, it)
                removeTomcatLogDirectory(session, it)
                startupTomcat(session, it)
                val tomcatPidListAfterStartup = findTomcatProcessPid(session, it)
                logger.info("after tomcat start up, tomcat pid list size:%s, tomcat:[%s]", tomcatPidListAfterStartup.size, it.remoteTomcatDirectory)
            }
            session.disconnect()
        }
    }
}