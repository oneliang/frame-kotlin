package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.updater.UpdaterExecutor
import com.oneliang.ktx.frame.updater.tomcat.TomcatAutoUpdater
import com.oneliang.ktx.util.file.readContentIgnoreLine
import com.oneliang.ktx.util.json.jsonToObjectList
import java.io.File
import java.util.concurrent.CountDownLatch

fun main() {
//    val configuration = TomcatAutoUpdater.Configuration().apply {
//        this.host = ""
//        this.port = 0
//        this.user = "root"
//        this.password = JOptionPane.showInputDialog("Enter password:${this.host}")
//        this.warArray = arrayOf(TomcatAutoUpdater.Configuration.War().apply {
//            this.remoteTomcatDirectory = "/home/wwwroot/apache-tomcat-backend"
//            this.localWarFile = "/D:/settings.zip"
//            this.remoteWarName = "a.zip"
//        })
//    }
//    Ssh.connect(host = configuration.host,
//            user = configuration.user,
//            port = configuration.port,
//            password = configuration.password,
//            configurationMap = mapOf(Ssh.Configuration.USERAUTH_GSSAPI_WITH_MIC to "no", Ssh.Configuration.STRICT_HOST_KEY_CHECKING to "no"), afterSessionConnect = { session ->
//        Ssh.exec(session, "java -version") {
//            it.inputStream.readContentEachLine { line ->
//                println(line)
//                true
//            }
//        }
//    })
//    return
    val configurationFile = File("D:\\Dandelion\\java\\githubWorkspace\\frame-kotlin\\updater\\src\\test\\kotlin\\com\\oneliang\\ktx\\frame\\test\\configuration.json")
    val json = configurationFile.readContentIgnoreLine()
    val configurationList = json.jsonToObjectList(TomcatAutoUpdater.Configuration::class)
    val updaterExecutor = UpdaterExecutor()
    val countDownLatch = CountDownLatch(configurationList.size)
    configurationList.forEach {
//        it.password = try {
//            JOptionPane.showInputDialog("(${it.host}) Please Enter password")
//        } catch (e: Throwable) {
//            e.printStackTrace()
//            it.password
//        }
        val tomcatAutoUpdater = TomcatAutoUpdater(it)
        updaterExecutor.addTomcatAutoUpdater(tomcatAutoUpdater) {
            countDownLatch.countDown()
        }
    }
    countDownLatch.await()
    updaterExecutor.stop()
}