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
    var jarFullFilename: String = Constants.String.BLANK
    var containerRunnableClassName = Constants.String.BLANK
    private var containerRunnable: ContainerRunnable? = null

    private val awaitAndSignal = AwaitAndSignal<String>()

    //second way:clientManager readProcessor is lambda, so you can implement Function interface to use it
    private val readProcessor = { byteArray: ByteArray ->
        val responseTlvPacket = tlvPacketProcessor.receiveTlvPacket(byteArray)
        val responseJson = String(responseTlvPacket.body)
        logger.debug("receive, type:%s, body json:%s", responseTlvPacket.type.toInt(), responseJson)
        val slaveRegisterResponse = responseJson.jsonToObject(SlaveRegisterResponse::class)
        val action = slaveRegisterResponse.action
        val id = slaveRegisterResponse.id
        val success = slaveRegisterResponse.success
        when {
            action == ConstantsContainer.Action.SLAVE_REGISTER && success -> {
                this.awaitAndSignal.signal(id)
            }
        }
    }
    private val clientManager = ClientManager(masterHost, masterPort, 1, this.readProcessor)
    private val operationLock = OperationLock()

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

    private fun register(){
        val id = Generator.generateGlobalThreadId()
        val slaveRegisterRequest = SlaveRegisterRequest.build(id)
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

    override fun send() {
        this.clientManager.send(ByteArray(0))
    }

    override fun receive() {
    }
}