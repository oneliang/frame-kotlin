package com.oneliang.ktx.frame.test.container

import com.oneliang.ktx.frame.container.Communicable
import com.oneliang.ktx.frame.container.ContainerRunnable
import com.oneliang.ktx.util.logging.LoggerManager

class TestSlaveContainerRunner : ContainerRunnable {
    companion object {
        private val logger = LoggerManager.getLogger(TestSlaveContainerRunner::class)
    }

    override lateinit var communicable: Communicable

    override fun running() {
        logger.debug("communicable:%s", communicable)
        logger.debug("this:%s", this)
    }
}