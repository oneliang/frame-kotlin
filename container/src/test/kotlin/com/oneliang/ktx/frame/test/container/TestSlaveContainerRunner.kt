package com.oneliang.ktx.frame.test.container

import com.oneliang.ktx.frame.container.Communicable
import com.oneliang.ktx.frame.container.ContainerExecutor
import com.oneliang.ktx.util.common.HOST_ADDRESS
import com.oneliang.ktx.util.json.jsonToObject
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager

class TestSlaveContainerRunner : ContainerExecutor, Communicable.CommunicationCallback {
    companion object {
        private val logger = LoggerManager.getLogger(TestSlaveContainerRunner::class)
    }

    override lateinit var communicable: Communicable
    private lateinit var slave: Config.Slave

    override fun initialize() {
        this.communicable.setCommunicationCallback(this)
    }

    override fun execute() {
        logger.debug("communicable:%s", this.communicable)
        logger.debug("this:%s", this)
//        Thread.sleep(2000)
//        this.communicable.sendData("abcdefg".toByteArray())
    }

    override fun destroy() {}

    /**
     * on receive data
     * @param id
     * @param byteArray
     */
    override fun onReceiveData(id: String, byteArray: ByteArray) {
        val config = String(byteArray).jsonToObject(Config::class)
        val slaves = config.slaves
        run looping@{
            slaves.forEach {
                if (it.hostAddress.contains(HOST_ADDRESS)) {
                    this.slave = it
                    logger.debug("i am in host address:%s, sources:%s", it.hostAddress, it.sources.toJson())
                    return@looping//break
                }
            }
        }
    }
}