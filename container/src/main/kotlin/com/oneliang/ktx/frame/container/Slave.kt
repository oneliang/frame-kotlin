package com.oneliang.ktx.frame.container

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.socket.nio.ClientManager
import com.oneliang.ktx.util.common.Generator
import com.oneliang.ktx.util.common.isEntity
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.concurrent.atomic.AwaitAndSignal
import com.oneliang.ktx.util.concurrent.atomic.OperationLock
import com.oneliang.ktx.util.json.jsonToObject
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.packet.TlvPacket
import com.oneliang.ktx.util.packet.TlvPacketProcessor

class Slave(masterHost: String, masterPort: Int) : Container, Communicable {

    companion object {
        private val logger = LoggerManager.getLogger(Slave::class)
        private val tlvPacketProcessor = TlvPacketProcessor()
    }

    private var classLoader: ClassLoader? = null
    internal var localTest = false

    var jarFullFilename: String = Constants.String.BLANK
    var containerRunnableClassName = Constants.String.BLANK
    private var containerRunnable: ContainerRunnable? = null
    private val awaitAndSignal = AwaitAndSignal<String>()

    private lateinit var privateId: String
    override val id: String
        get() = privateId

    //second way:clientManager readProcessor is lambda, so you can implement Function interface to use it
    private val readProcessor = { byteArray: ByteArray ->
        val receiveTlvPacket = tlvPacketProcessor.receiveTlvPacket(byteArray)
        val type = receiveTlvPacket.type.toInt()
//        logger.debug("receive, type:%s, body json:%s", responseTlvPacket.type.toInt(), responseJson)
        when (type) {
            ConstantsContainer.TlvPackageType.SLAVE_REGISTER.toInt() -> {
                val receiveJson = String(receiveTlvPacket.body)
                val slaveRegisterResponse = receiveJson.jsonToObject(SlaveRegisterResponse::class)
                if (slaveRegisterResponse.success) {
                    this.awaitAndSignal.signal(id)
                }
            }
            ConstantsContainer.TlvPackageType.SLAVE_UNREGISTER.toInt() -> {
                val receiveJson = String(receiveTlvPacket.body)
                val slaveUnregisterResponse = receiveJson.jsonToObject(SlaveUnregisterResponse::class)
                if (slaveUnregisterResponse.success) {
                    this.awaitAndSignal.signal(id)
                }
                logger.debug("unregister, type:%s, body json:%s", receiveTlvPacket.type.toInt(), receiveJson)
            }
            ConstantsContainer.TlvPackageType.SLAVE_DATA.toInt() -> {
                val receiveJson = String(receiveTlvPacket.body)
                val slaveDataResponse = receiveJson.jsonToObject(SlaveDataResponse::class)
                if (slaveDataResponse.success) {
//                    this.awaitAndSignal.signal(id)
                }
                logger.debug("data, type:%s, body json:%s", receiveTlvPacket.type.toInt(), receiveJson)
            }
            ConstantsContainer.TlvPackageType.MASTER_NOTIFY_CONFIG_CHANGED.toInt() -> {
                val masterNotifyConfigChanged = MasterNotifyConfigChanged.fromByteArray(receiveTlvPacket.body)
                if (this::receiveCallback.isInitialized) {
                    this.receiveCallback.receive(masterNotifyConfigChanged.data)
                }
                val masterData = String(masterNotifyConfigChanged.data)
                logger.debug("master notify config changed, type:%s, body json:%s", receiveTlvPacket.type.toInt(), masterData)
            }
        }
    }
    private val clientManager = ClientManager(masterHost, masterPort, 1, this.readProcessor)
    private val operationLock = OperationLock()
    private lateinit var receiveCallback: Communicable.ReceiveCallback

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

    private fun register() {
        if (!this::privateId.isInitialized) {
            this.privateId = Generator.generateGlobalThreadId()
        }
        val slaveRegisterRequest = SlaveRegisterRequest.build(this.privateId)
        val slaveRegisterRequestJson = slaveRegisterRequest.toJson()
        val tlvPacketByteArray = TlvPacket(ConstantsContainer.TlvPackageType.SLAVE_REGISTER, slaveRegisterRequestJson.toByteArray()).toByteArray()
        this.clientManager.send(tlvPacketByteArray)
    }

    override fun start() {
        this.operationLock.operate {
            loadContainerRunnable()
            this.clientManager.start()
            this.register()
            val containerRunnable = this.containerRunnable
            if (containerRunnable != null) {
                containerRunnable.communicable = this
                containerRunnable.running()
            }
        }
    }

    override fun stop() {
        this.operationLock.operate {
            this.clientManager.stop()
        }
    }

    override fun restart() {
        this.operationLock.operate {
            this.start()
            this.stop()
        }
    }

    /**
     * only for slave data tlv package
     */
    override fun sendData(byteArray: ByteArray) {
        val slaveDataRequest = SlaveDataRequest.build(this.privateId, byteArray)
        val tlvPacketByteArray = TlvPacket(ConstantsContainer.TlvPackageType.SLAVE_DATA, slaveDataRequest.toByteArray()).toByteArray()
        this.clientManager.send(tlvPacketByteArray)
    }

    /**
     * set receive callback
     * @param receiveCallback
     */
    override fun setReceiveCallback(receiveCallback: Communicable.ReceiveCallback) {
        this.receiveCallback = receiveCallback
    }
}