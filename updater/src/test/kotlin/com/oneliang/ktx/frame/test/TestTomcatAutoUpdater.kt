package com.oneliang.ktx.frame.test

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.ssh.Ssh
import com.oneliang.ktx.frame.updater.UpdaterExecutor
import com.oneliang.ktx.frame.updater.tomcat.TomcatAutoUpdater
import com.oneliang.ktx.util.common.perform
import com.oneliang.ktx.util.common.readContentIgnoreLine
import com.oneliang.ktx.util.file.readContentIgnoreLine
import com.oneliang.ktx.util.json.jsonToObjectList
import java.io.File
import java.util.concurrent.CountDownLatch
import javax.swing.JOptionPane

fun main() {
    val configuration = TomcatAutoUpdater.Configuration().apply {
        this.host = "42.192.93.32"
        this.port = 3222
        this.user = "root"
        this.password = JOptionPane.showInputDialog("Enter password:${this.host}")
        this.warArray = arrayOf(TomcatAutoUpdater.Configuration.War().apply {
            this.remoteTomcatDirectory = "/home/wwwroot/apache-tomcat-backend"
            this.localWarFile = "/D:/settings.zip"
            this.remoteWarName = "a.zip"
        })
    }
//    Ssh.connect(host = configuration.host,
//            user = configuration.user,
//            port = configuration.port,
//            password = configuration.password,
//            configurationMap = mapOf(Ssh.Configuration.USERAUTH_GSSAPI_WITH_MIC to "no", Ssh.Configuration.STRICT_HOST_KEY_CHECKING to "no"), afterSessionConnect = { session ->
//        Ssh.exec(session, "java -version") {
//            it.inputStream.readContentIgnoreLine { line ->
//                println(line)
//                true
//            }
//        }
//    })
//    return
    val configurationFile = File(Constants.String.BLANK)
    val json = configurationFile.readContentIgnoreLine()
    val configurationList = json.jsonToObjectList(TomcatAutoUpdater.Configuration::class)
    val updaterExecutor = UpdaterExecutor()
    val countDownLatch = CountDownLatch(configurationList.size)
    configurationList.forEach {
        it.password = perform({
            JOptionPane.showInputDialog("(${it.host}) Please Enter password")
        }, failure = { e ->
            e.printStackTrace()
            it.password
        })
        val tomcatAutoUpdater = TomcatAutoUpdater(it)
        updaterExecutor.addTomcatAutoUpdater(tomcatAutoUpdater) {
            countDownLatch.countDown()
        }
    }
    countDownLatch.await()
    updaterExecutor.stop()
}