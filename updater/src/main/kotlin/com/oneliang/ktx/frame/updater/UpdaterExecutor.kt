package com.oneliang.ktx.frame.updater

import com.oneliang.ktx.frame.updater.tomcat.TomcatAutoUpdater
import com.oneliang.ktx.util.concurrent.ThreadPool

class UpdaterExecutor(private val revert: Boolean = false) {
    private val threadPool = ThreadPool().also { it.start() }

    fun addTomcatAutoUpdater(tomcatAutoUpdater: TomcatAutoUpdater, taskFinished: () -> Unit = {}) {
        this.threadPool.addThreadTask({
            if (this.revert) {
                tomcatAutoUpdater.revert()
            } else {
                tomcatAutoUpdater.update()
            }
        }, finally = taskFinished)
    }

    fun stop() {
        this.threadPool.interrupt()
    }
}