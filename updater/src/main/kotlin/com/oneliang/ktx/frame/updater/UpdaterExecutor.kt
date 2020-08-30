package com.oneliang.ktx.frame.updater

import com.oneliang.ktx.frame.updater.tomcat.TomcatAutoUpdater
import com.oneliang.ktx.util.concurrent.ThreadPool

class UpdaterExecutor {
    private val threadPool = ThreadPool().also { it.start() }

    fun addTomcatAutoUpdater(tomcatAutoUpdater: TomcatAutoUpdater){
        threadPool.addThreadTask {
            tomcatAutoUpdater.update()
        }
    }
}