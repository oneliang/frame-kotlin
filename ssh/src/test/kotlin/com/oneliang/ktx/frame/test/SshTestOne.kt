package com.oneliang.ktx.frame.test

import javax.swing.JOptionPane

fun main() {
    val configuration = TomcatAutoUpdater.Configuration().apply {
        this.host = "122.51.110.167"
        this.port = 22
        this.user = "root"
        this.password = JOptionPane.showInputDialog("Enter password")
        this.tomcatDirectory = "/home/wwwroot/apache-tomcat-backend"
        this.localWarFullFilename = "/D:/settings.zip"
        this.remoteWarName = "a.zip"
    }
    val tomcatAutoUpdater = TomcatAutoUpdater()
    tomcatAutoUpdater.update(configuration)
}