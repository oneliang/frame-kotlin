package com.oneliang.ktx.frame.test.container

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.container.Communicable
import com.oneliang.ktx.frame.container.ContainerExecutor
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.file.FileDetector
import com.oneliang.ktx.util.file.readContentEachLine
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File

class TestMasterContainerRunner : ContainerExecutor, Communicable.CommunicationCallback {
    companion object {
        private val logger = LoggerManager.getLogger(TestMasterContainerRunner::class)
    }

    override lateinit var communicable: Communicable
    private val projectPath = File(Constants.String.BLANK).absolutePath
    private val fileDetector = FileDetector("$projectPath/container/src/test/kotlin/com/oneliang/ktx/frame/test/container", "config.json")

    init {
        fileDetector.detectProcessor = object : FileDetector.DetectProcessor {
            override fun afterUpdateFileProcess(filePath: String) {
                logger.debug("It has a file updated, file path:%s", filePath)
                val file = filePath.toFile()
                if (file.name == "config.json") {
                    val configJsonStringBuilder = StringBuilder()
                    file.readContentEachLine {
                        configJsonStringBuilder.append(it.trim())
                        true
                    }
                    val configJson = configJsonStringBuilder.toString()
                    communicable.sendData(configJson.toByteArray())
                }
            }
        }
    }

    override fun initialize() {
        this.communicable.setCommunicationCallback(this)
        this.fileDetector.start()
    }

    override fun execute() {
        logger.debug("communicable:%s", communicable)
        logger.debug("this:%s", this)
    }

    override fun destroy() {}

    /**
     * on receive data
     * @param id
     * @param byteArray
     */
    override fun onReceiveData(id: String, byteArray: ByteArray) {
        logger.debug("on receive data, slave id:%s", id)
    }

    /**
     * on connect
     * @param socketChannelHashCode
     */
    override fun onConnect(socketChannelHashCode: Int) {
        logger.debug("on connect, slave socket channel hash code:%s", socketChannelHashCode)
    }

    /**
     * on disconnect
     * @param socketChannelHashCode
     */
    override fun onDisconnect(socketChannelHashCode: Int) {
        logger.debug("on disconnect, slave socket channel hash code:%s", socketChannelHashCode)
    }

    /**
     * on register
     * @param id
     */
    override fun onRegister(id: String) {
        logger.debug("on register, slave id:%s", id)
    }

    /**
     * on unregister
     * @param id
     */
    override fun onUnregister(id: String) {
        logger.debug("on unregister, slave id:%s", id)
    }
}