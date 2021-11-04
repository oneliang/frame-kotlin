package com.oneliang.ktx.frame.socket

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.toHexString
import com.oneliang.ktx.util.concurrent.LoopThread
import com.oneliang.ktx.util.concurrent.ThreadPool
import com.oneliang.ktx.util.logging.LoggerManager
import java.net.DatagramPacket
import java.net.DatagramSocket

class UDPSocketServer(private val port: Int) : LoopThread() {
    companion object {
        private val logger = LoggerManager.getLogger(UDPSocketServer::class)
    }

    private val threadCount = Runtime.getRuntime().availableProcessors()
    private val threadPool = ThreadPool()
    private lateinit var datagramSocket: DatagramSocket
    lateinit var processor: Processor

    override fun run() {
        this.datagramSocket = DatagramSocket(this.port)
        super.run()
    }

    @Throws(Throwable::class)
    override fun looping() {
        if (!this::datagramSocket.isInitialized) {
            return
        }
        this.threadPool.addThreadTask {
            val byteArray = ByteArray(Constants.Capacity.BYTES_PER_KB)
            val datagramPacket = DatagramPacket(byteArray, byteArray.size)
            this.datagramSocket.receive(datagramPacket)
            val totalLength = datagramPacket.length
            val receiveData = datagramPacket.data
            logger.info("UDP socket address:%s, total length:%s, size:%s, data hex:%s", datagramPacket.address, totalLength, receiveData.size, receiveData.toHexString())
            var data = ByteArray(0)
            when {
                receiveData.size > totalLength -> {
                    data = ByteArray(totalLength)
                    receiveData.copyInto(data, 0, 0, totalLength)
                }
                receiveData.size == totalLength -> {
                    data = receiveData
                }
                else -> {
                }
            }
            try {
                logger.info("UDP socket address:%s ,processing...", datagramPacket.address)
                this.processor.process(data)
            } catch (e: Throwable) {
                logger.error("UDP socket processor exception", e)
                throw e
            }
        }
    }

    @Synchronized
    override fun start() {
        super.start()
        //thread pool
        this.threadPool.minThreads = 1
        this.threadPool.maxThreads = threadCount
        this.threadPool.start()
    }

    interface Processor {

        @Throws(Throwable::class)
        fun process(data: ByteArray)
    }
}