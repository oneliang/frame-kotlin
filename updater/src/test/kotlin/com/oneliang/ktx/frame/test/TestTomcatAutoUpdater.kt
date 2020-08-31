package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.updater.UpdaterExecutor
import com.oneliang.ktx.frame.updater.tomcat.TomcatAutoUpdater
import com.oneliang.ktx.util.file.readContentIgnoreLine
import com.oneliang.ktx.util.json.jsonToObjectList
import com.oneliang.ktx.util.json.toJson
import java.io.File
import javax.swing.JOptionPane

fun main() {
    val configuration = TomcatAutoUpdater.Configuration().apply {
        this.host = "122.51.110.167"
        this.port = 22
        this.user = "root"
        this.password = JOptionPane.showInputDialog("Enter password:${this.host}")
        this.warArray = arrayOf(TomcatAutoUpdater.Configuration.War().apply {
            this.remoteTomcatDirectory = "/home/wwwroot/apache-tomcat-backend"
            this.localWarFullFilename = "/D:/settings.zip"
            this.remoteWarName = "a.zip"
        })
    }
    val configurationFile = File("")
    val json = configurationFile.readContentIgnoreLine()
    val configurationList = json.jsonToObjectList(TomcatAutoUpdater.Configuration::class)
    val updaterExecutor = UpdaterExecutor()
    configurationList.forEach {
        it.password = JOptionPane.showInputDialog("Please Enter password:${it.host}")
        val tomcatAutoUpdater = TomcatAutoUpdater(it)
        updaterExecutor.addTomcatAutoUpdater(tomcatAutoUpdater)
    }
}