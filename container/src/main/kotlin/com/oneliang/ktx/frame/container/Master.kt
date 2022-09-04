package com.oneliang.ktx.frame.container

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.socket.nio.SelectorProcessor
import com.oneliang.ktx.frame.socket.nio.Server
import com.oneliang.ktx.util.common.HOST_ADDRESS
import com.oneliang.ktx.util.common.isEntity
import com.oneliang.ktx.util.common.toInt
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
    private val server = Server(HOST_ADDRESS, port)
    private val slaveMap = mutableMapOf<String, Pair<String, Int>>()

    private var classLoader: ClassLoader? = null

    internal var localTest = false
    var jarFullFilename: String = Constants.String.BLANK
    var containerRunnableClassName = Constants.String.BLANK
    private var containerRunnable: ContainerRunnable? = null

    override fun process(byteArray: ByteArray, socketChannelHashCode: Int): ByteArray {
        val tlvPacket = tlvPacketProcessor.receiveTlvPacket(ByteArrayInputStream(byteArray))
        val type = tlvPacket.type.toInt()
        val requestString = String(tlvPacket.body)
        logger.debug("server read:%s", requestString)
        return when (type) {
            ConstantsContainer.TlvPackageType.SLAVE_REGISTER.toInt() -> {
                val slaveRegisterRequest = requestString.jsonToObject(SlaveRegisterRequest::class)
                this.slaveMap[slaveRegisterRequest.id] = slaveRegisterRequest.id to socketChannelHashCode
                logger.info("slave register, slave id:%s, socket channel hash code:%s", slaveRegisterRequest.id, socketChannelHashCode)
                val slaveRegisterResponse = SlaveRegisterResponse.build(slaveRegisterRequest.id, true)
                val slaveRegisterResponseJson = slaveRegisterResponse.toJson()
                TlvPacket(ConstantsContainer.TlvPackageType.SLAVE_REGISTER, slaveRegisterResponseJson.toByteArray()).toByteArray()
            }
            ConstantsContainer.TlvPackageType.SLAVE_UNREGISTER.toInt() -> {
                val slaveUnregisterRequest = requestString.jsonToObject(SlaveUnregisterRequest::class)
                this.slaveMap.remove(slaveUnregisterRequest.id)
                logger.info("slave unregister, slave id:%s, socket channel hash code:%s", slaveUnregisterRequest.id, socketChannelHashCode)
                val slaveUnregisterResponse = SlaveUnregisterResponse.build(slaveUnregisterRequest.id, true)
                val slaveUnregisterResponseJson = slaveUnregisterResponse.toJson()
                TlvPacket(ConstantsContainer.TlvPackageType.SLAVE_UNREGISTER, slaveUnregisterResponseJson.toByteArray()).toByteArray()
            }
            ConstantsContainer.TlvPackageType.SLAVE_DATA.toInt() -> {
                val slaveDataRequest = SlaveDataRequest.fromByteArray(tlvPacket.body)
                val slaveData = String(slaveDataRequest.data)
                logger.info("slave data, slave id:%s, slave data:%s, socket channel hash code:%s", slaveDataRequest.id, slaveData, socketChannelHashCode)
                val slaveDataResponse = SlaveDataResponse.build(slaveDataRequest.id, true)
                val slaveDataResponseJson = slaveDataResponse.toJson()
                TlvPacket(ConstantsContainer.TlvPackageType.SLAVE_DATA, slaveDataResponseJson.toByteArray()).toByteArray()
            }
            else -> {
                val responseJson = BaseData.build(Constants.String.BLANK, ConstantsContainer.Action.NONE).toJson()
                TlvPacket(ConstantsContainer.TlvPackageType.NONE, responseJson.toByteArray()).toByteArray()
            }
        }
    }

    private fun loadContainerRunnable() {
        if (!this.localTest && this.jarFullFilename.isBlank()) {
            error("jar full filename is blank")
        }
        if (this.containerRunnableClassName.isBlank()) {
            error("container runnable class name is blank")
        }
        this.classLoader = if (this.localTest) {
            Thread.currentThread().contextClassLoader
        } else {
            DynamicJarManager.loadJar(this.jarFullFilename)
        }
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
            this.server.selectorProcessor = this
            this.server.start()
            val containerRunnable = this.containerRunnable
            if (containerRunnable != null) {
                containerRunnable.communicable = this
                containerRunnable.running()
            }
        }
    }

    override fun stop() {
        this.operationLock.operate {
            this.server.stop()
        }
    }

    override fun restart() {
        this.operationLock.operate {
            this.start()
            this.stop()
        }
    }

    override fun sendData(byteArray: ByteArray) {
//        this.server.notify()
    }

    override fun receive() {
    }
}