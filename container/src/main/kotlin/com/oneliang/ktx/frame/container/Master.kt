package com.oneliang.ktx.frame.container

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.socket.nio.SelectorProcessor
import com.oneliang.ktx.frame.socket.nio.Server
import com.oneliang.ktx.util.common.HOST_ADDRESS
import com.oneliang.ktx.util.common.isEntity
import com.oneliang.ktx.util.concurrent.atomic.OperationLock
import com.oneliang.ktx.util.json.jsonToObject
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.packet.TlvPacket
import com.oneliang.ktx.util.packet.TlvPacketProcessor
import java.io.ByteArrayInputStream

class Master(port: Int) : Container, Communicable, SelectorProcessor {

    companion object {
        private val logger = LoggerManager.getLogger(Master::class)
        private val tlvPacketProcessor = TlvPacketProcessor()
    }

    private val operationLock = OperationLock()
    private val socketServer = Server(HOST_ADDRESS, port)
    private val slaveMap = mutableMapOf<String, String>()

    private var classLoader: ClassLoader? = null

    var jarFullFilename: String = Constants.String.BLANK
    var containerRunnableClassName = Constants.String.BLANK
    private var containerRunnable: ContainerRunnable? = null

    override fun process(byteArray: ByteArray, socketChannelHashCode: Int): ByteArray {
        val tlvPacket = tlvPacketProcessor.receiveTlvPacket(ByteArrayInputStream(byteArray))
        val requestString = String(tlvPacket.body)
        logger.debug("server read:%s", requestString)
        val baseData = requestString.jsonToObject(BaseData::class)
        val action = baseData.action
        val id = baseData.id
        return when (action) {
            ConstantsContainer.Action.SLAVE_REGISTER -> {
                val slaveRegisterRequest = requestString.jsonToObject(SlaveRegisterRequest::class)
                this.slaveMap[slaveRegisterRequest.id] = slaveRegisterRequest.id
                logger.info("slave register, slave id:%s", slaveRegisterRequest.id)
                val slaveRegisterResponse = SlaveRegisterResponse.build(id, true)
                val slaveRegisterResponseJson = slaveRegisterResponse.toJson()
                TlvPacket(ConstantsContainer.TlvPackageType.SLAVE_REGISTER, slaveRegisterResponseJson.toByteArray()).toByteArray()
            }
            else -> {
                val responseJson = BaseData.build(id, ConstantsContainer.Action.NONE).toJson()
                TlvPacket(ConstantsContainer.TlvPackageType.NONE, responseJson.toByteArray()).toByteArray()
            }
        }
    }

    private fun registerSlave() {

    }

    private fun loadContainerRunnable() {
        if (this.jarFullFilename.isBlank()) {
            error("jar full filename is blank")
        }
        if (this.containerRunnableClassName.isBlank()) {
            error("container runnable class name is blank")
        }
        this.classLoader = DynamicJarManager.loadJar(this.jarFullFilename)
        try {
            val containerRunnableClass = this.classLoader?.loadClass(this.containerRunnableClassName)
            if (containerRunnableClass != null) {
                val containerRunnableClassInstance = containerRunnableClass.newInstance()
                if (containerRunnableClassInstance.isEntity(ContainerRunnable::class)) {
                    this.containerRunnable = containerRunnableClassInstance as ContainerRunnable
                    this.containerRunnable?.communicable = this
                } else {
                    error("container runnable class instance is not the entity of ContainerRunnable::class")
                }
            } else {
                error("container runnable class is null, it is not a ClassNotFoundException??? It is impossible.")
            }
        } catch (e: Throwable) {
            throw e
        }
    }

    override fun start() {
        this.operationLock.operate {
            loadContainerRunnable()
            this.socketServer.start()
            val containerRunnable = this.containerRunnable
            if (containerRunnable != null) {
                containerRunnable.communicable = this
                containerRunnable.running()
            }
        }
    }

    override fun stop() {
        this.operationLock.operate {
            this.socketServer.stop()
        }
    }

    override fun restart() {
        this.operationLock.operate {
            this.start()
            this.stop()
        }
    }

    override fun send() {

    }

    override fun receive() {
    }
}