package com.oneliang.ktx.frame.container

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.socket.nio.SelectorProcessor
import com.oneliang.ktx.frame.socket.nio.Server
import com.oneliang.ktx.util.common.Generator
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
import java.util.concurrent.ConcurrentHashMap

class Master(port: Int) : Container, Communicable, SelectorProcessor {

    companion object {
        private val logger = LoggerManager.getLogger(Master::class)
        private val tlvPacketProcessor = TlvPacketProcessor()
    }

    private val operationLock = OperationLock()
    private val server = Server(HOST_ADDRESS, port, statusCallback = object : Server.StatusCallback {
        /**
         * on connect
         * @param socketChannelHashCode
         */
        override fun onConnect(socketChannelHashCode: Int) {
            if (this@Master::communicationCallback.isInitialized) {
                this@Master.communicationCallback.onConnect(socketChannelHashCode)
            }
        }

        /**
         * on disconnect
         * @param socketChannelHashCode
         */
        override fun onDisconnect(socketChannelHashCode: Int) {
            this@Master.removeSlaveBySocketChannelHashCode(socketChannelHashCode)
            if (this@Master::communicationCallback.isInitialized) {
                this@Master.communicationCallback.onDisconnect(socketChannelHashCode)
            }
        }
    })
    private val slaveMap = ConcurrentHashMap<String, Pair<String, Int>>()
    private val socketChannelSlaveMap = ConcurrentHashMap<Int, String>()

    private var classLoader: ClassLoader? = null

    internal var localTest = false
    var jarFullFilename: String = Constants.String.BLANK
    var containerExecutorClassName = Constants.String.BLANK
    private var containerExecutor: ContainerExecutor? = null
    private lateinit var communicationCallback: Communicable.CommunicationCallback

    private lateinit var privateId: String
    override val id: String
        get() = "Master-$privateId"

    override fun process(byteArray: ByteArray, socketChannelHashCode: Int): ByteArray {
        val tlvPacket = tlvPacketProcessor.receiveTlvPacket(ByteArrayInputStream(byteArray))
        val type = tlvPacket.type.toInt()
        val requestString = String(tlvPacket.body)
        logger.debug("server read:%s", requestString)
        return when (type) {
            ConstantsContainer.TlvPackageType.SLAVE_REGISTER.toInt() -> {
                val slaveRegisterRequest = requestString.jsonToObject(SlaveRegisterRequest::class)
                val slaveId = slaveRegisterRequest.id
                this.addSlaveToMap(socketChannelHashCode, slaveId)
                if (this::communicationCallback.isInitialized) {
                    try {
                        this.communicationCallback.onRegister(slaveId)
                    } catch (e: Throwable) {
                        logger.error("slave on register callback exception, slave register request id:%s", e, slaveId)
                    }
                }
                logger.info("slave register, slave id:%s, socket channel hash code:%s", slaveId, socketChannelHashCode)
                val slaveRegisterResponse = SlaveRegisterResponse.build(slaveId, true)
                val slaveRegisterResponseJson = slaveRegisterResponse.toJson()
                TlvPacket(ConstantsContainer.TlvPackageType.SLAVE_REGISTER, slaveRegisterResponseJson.toByteArray()).toByteArray()
            }
            ConstantsContainer.TlvPackageType.SLAVE_UNREGISTER.toInt() -> {
                val slaveUnregisterRequest = requestString.jsonToObject(SlaveUnregisterRequest::class)
                val slaveId = slaveUnregisterRequest.id
                this.removeSlaveById(slaveId)
                if (this::communicationCallback.isInitialized) {
                    try {
                        this.communicationCallback.onUnregister(slaveId)
                    } catch (e: Throwable) {
                        logger.error("slave on unregister callback exception, slave unregister request id:%s", e, slaveId)
                    }
                }
                logger.info("slave unregister, slave id:%s, socket channel hash code:%s", slaveId, socketChannelHashCode)
                val slaveUnregisterResponse = SlaveUnregisterResponse.build(slaveId, true)
                val slaveUnregisterResponseJson = slaveUnregisterResponse.toJson()
                TlvPacket(ConstantsContainer.TlvPackageType.SLAVE_UNREGISTER, slaveUnregisterResponseJson.toByteArray()).toByteArray()
            }
            ConstantsContainer.TlvPackageType.SLAVE_DATA.toInt() -> {
                val slaveDataRequest = SlaveDataRequest.fromByteArray(tlvPacket.body)
                val slaveData = String(slaveDataRequest.data)
                logger.info("slave data, slave id:%s, slave data:%s, socket channel hash code:%s", slaveDataRequest.id, slaveData, socketChannelHashCode)
                if (this::communicationCallback.isInitialized) {
                    try {
                        this.communicationCallback.onReceiveData(slaveDataRequest.id, slaveDataRequest.data)
                    } catch (e: Throwable) {
                        logger.error("slave data receive callback exception, slave data request id:%s", e, slaveDataRequest.id)
                    }
                }
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

    /**
     * add slave to map
     * @param socketChannelHashCode
     * @param slaveId
     */
    private fun addSlaveToMap(socketChannelHashCode: Int, slaveId: String) {
        this.slaveMap[slaveId] = slaveId to socketChannelHashCode
        this.socketChannelSlaveMap[socketChannelHashCode] = slaveId
    }

    /**
     * remove slave by socket channel hash code
     * @param socketChannelHashCode
     * @return String?
     */
    private fun removeSlaveBySocketChannelHashCode(socketChannelHashCode: Int): String? {
        val slaveId = this.socketChannelSlaveMap.remove(socketChannelHashCode)
        if (slaveId != null) {
            this.slaveMap.remove(slaveId)
        }
        return slaveId
    }

    /**
     * remove slave by id
     * @param slaveId
     */
    private fun removeSlaveById(slaveId: String) {
        val slaveIdPair = this.slaveMap.remove(slaveId)
        if (slaveIdPair != null) {
            val (_, socketChannelHashCode) = slaveIdPair
            this.socketChannelSlaveMap.remove(socketChannelHashCode)
        }
    }

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

    override fun start() {
        this.operationLock.operate {
            //initialize
            if (!this::privateId.isInitialized) {
                this.privateId = Generator.generateGlobalThreadId()
            }
            loadContainerExecutor()
            this.server.selectorProcessor = this
            //start and execute
            this.server.start()
            this.containerExecutor?.execute()
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

    /**
     * send data
     * @param byteArray
     */
    override fun sendData(byteArray: ByteArray) {
        this.socketChannelSlaveMap.forEach { (socketChannelHashCode, _) ->
            val masterNotifyConfigChanged = MasterNotifyConfigChanged.build(this.id, byteArray)
            val tlvPacketByteArray = TlvPacket(ConstantsContainer.TlvPackageType.MASTER_NOTIFY_CONFIG_CHANGED, masterNotifyConfigChanged.toByteArray()).toByteArray()
            this.server.notify(socketChannelHashCode, tlvPacketByteArray)
        }
    }

    /**
     * set communication callback
     * @param communicationCallback
     */
    override fun setCommunicationCallback(communicationCallback: Communicable.CommunicationCallback) {
        this.communicationCallback = communicationCallback
    }
}