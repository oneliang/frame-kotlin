package com.oneliang.ktx.frame.updater

import com.oneliang.ktx.frame.updater.tomcat.TomcatAutoUpdater
import com.oneliang.ktx.util.concurrent.ThreadPool

class UpdaterExecutor {
    private val threadPool = ThreadPool().also { it.start() }

    fun addTomcatAutoUpdater(tomcatAutoUpdater: TomcatAutoUpdater, taskFinished: () -> Unit = {}) {
        this.threadPool.addThreadTask({
            tomcatAutoUpdater.update()
        }, finally = taskFinished)
    }

    fun stop() {
        this.threadPool.interrupt()
    }
}