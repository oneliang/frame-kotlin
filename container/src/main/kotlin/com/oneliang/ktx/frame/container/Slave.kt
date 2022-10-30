package com.oneliang.ktx.frame.container

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.socket.nio.ClientManager
import com.oneliang.ktx.util.common.Generator
import com.oneliang.ktx.util.common.isEntity
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.concurrent.ResourceQueueThread
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
    var containerExecutorClassName = Constants.String.BLANK
    private var containerExecutor: ContainerExecutor? = null
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
                    this.awaitAndSignal.signal(this.privateId)
                }
                logger.debug("register, type:%s, body json:%s", receiveTlvPacket.type.toInt(), receiveJson)
            }
            ConstantsContainer.TlvPackageType.SLAVE_UNREGISTER.toInt() -> {
                val receiveJson = String(receiveTlvPacket.body)
                val slaveUnregisterResponse = receiveJson.jsonToObject(SlaveUnregisterResponse::class)
                if (slaveUnregisterResponse.success) {
                    this.awaitAndSignal.signal(this.privateId)
                }
                logger.debug("unregister, type:%s, body json:%s", receiveTlvPacket.type.toInt(), receiveJson)
            }
            ConstantsContainer.TlvPackageType.SLAVE_DATA.toInt() -> {
                val receiveJson = String(receiveTlvPacket.body)
                val slaveDataResponse = receiveJson.jsonToObject(SlaveDataResponse::class)
                if (slaveDataResponse.success) {
//                    this.awaitAndSignal.signal(privateId)
                }
                logger.debug("data, type:%s, body json:%s", receiveTlvPacket.type.toInt(), receiveJson)
            }
            ConstantsContainer.TlvPackageType.MASTER_NOTIFY_CONFIG_CHANGED.toInt() -> {
                val masterNotifyConfigChanged = MasterNotifyConfigChanged.fromByteArray(receiveTlvPacket.body)
                if (this::communicationCallback.isInitialized) {
                    this.communicationCallback.onReceiveData(masterNotifyConfigChanged.id, masterNotifyConfigChanged.data)
                }
                val masterData = String(masterNotifyConfigChanged.data)
                logger.debug("master notify config changed, type:%s, body json:%s", receiveTlvPacket.type.toInt(), masterData)
            }
        }
    }
    private val clientManager = ClientManager(masterHost, masterPort, 1, this.readProcessor, object : ClientManager.ClientStatusCallback {
        override fun onConnect(clientIndex: Int) {
            this@Slave.workerThread.addResource(WorkerAction.CONNECT)
            this@Slave.workerThread.addResource(WorkerAction.REGISTER)
        }

        override fun onDisconnect(clientIndex: Int) {
            this@Slave.workerThread.addResource(WorkerAction.DISCONNECT)
        }
    })
    private val operationLock = OperationLock()
    private lateinit var communicationCallback: Communicable.CommunicationCallback

    private enum class WorkerAction {
        CONNECT, DISCONNECT, REGISTER, UNREGISTER
    }

    private val workerThread = ResourceQueueThread(object : ResourceQueueThread.ResourceProcessor<WorkerAction> {
        override fun process(resource: WorkerAction) {
            logger.debug("communication action:%s", resource)
            when (resource) {
                WorkerAction.CONNECT -> {
                    this@Slave.communicationCallback.onConnect(0)
                }
                WorkerAction.DISCONNECT -> {
                    this@Slave.communicationCallback.onDisconnect(0)
                }
                WorkerAction.REGISTER -> {
                    this@Slave.register()
                    this@Slave.communicationCallback.onRegister(this@Slave.privateId)
                }
                WorkerAction.UNREGISTER -> {
                    this@Slave.communicationCallback.onUnregister(this@Slave.privateId)
                }
            }
        }
    })

    private fun loadContainerExecutor() {
        if (!this.localTest && this.jarFullFilename.isBlank()) {
            error("jar full filename is blank")
        }
        if (this.containerExecutorClassName.isBlank()) {
            error("container executor class name is blank")
        }
        this.classLoader = if (this.localTest) {
            Thread.currentThread().contextClassLoader
        } else {
            DynamicJarManager.loadJar(this.jarFullFilename)
        }
        try {
            val containerExecutorClass = this.classLoader?.loadClass(this.containerExecutorClassName)
            if (containerExecutorClass != null) {
                val containerExecutorClassInstance = containerExecutorClass.newInstance()
                if (containerExecutorClassInstance.isEntity(ContainerExecutor::class)) {
                    this.containerExecutor = containerExecutorClassInstance as ContainerExecutor
                    this.containerExecutor?.communicable = this
                    this.containerExecutor?.initialize()
                } else {
                    error("container executor class instance is not the entity of ContainerExecutor::class")
                }
            } else {
                error("container executor class is null, it is not a ClassNotFoundException??? It is impossible.")
            }
        } catch (e: Throwable) {
            throw e
        }
    }

    private fun register() {
        val slaveRegisterRequest = SlaveRegisterRequest.build(this.privateId)
        val slaveRegisterRequestJson = slaveRegisterRequest.toJson()
        val tlvPacketByteArray = TlvPacket(ConstantsContainer.TlvPackageType.SLAVE_REGISTER, slaveRegisterRequestJson.toByteArray()).toByteArray()
        this.clientManager.send(tlvPacketByteArray)
        this.awaitAndSignal.await(this.privateId)
    }

    override fun start() {
        this.operationLock.operate {
            //initialize
            if (!this::privateId.isInitialized) {
                this.privateId = Generator.generateGlobalThreadId()
            }
            loadContainerExecutor()
            //start and execute
            this.workerThread.start()
            this.clientManager.start()
            this.containerExecutor?.execute()
        }
    }

    override fun stop() {
        this.operationLock.operate {
            this.clientManager.stop()
            this.workerThread.stopNow()
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
     * @param byteArray
     */
    override fun sendData(byteArray: ByteArray) {
        val slaveDataRequest = SlaveDataRequest.build(this.privateId, byteArray)
        val tlvPacketByteArray = TlvPacket(ConstantsContainer.TlvPackageType.SLAVE_DATA, slaveDataRequest.toByteArray()).toByteArray()
        this.clientManager.send(tlvPacketByteArray)
    }

    /**
     * set communication callback
     * @param communicationCallback
     */
    override fun setCommunicationCallback(communicationCallback: Communicable.CommunicationCallback) {
        this.communicationCallback = communicationCallback
    }
}