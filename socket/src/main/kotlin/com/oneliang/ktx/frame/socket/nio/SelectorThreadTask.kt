package com.oneliang.ktx.frame.socket.nio

import com.oneliang.ktx.util.common.MD5String
import com.oneliang.ktx.util.concurrent.ThreadTask
import com.oneliang.ktx.util.logging.LoggerManager
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.*

internal class SelectorThreadTask(val selector: Selector, private val disconnectCallback: (socketChannelHashCode: Int) -> Unit) : ThreadTask {
    companion object {
        private val logger = LoggerManager.getLogger(SelectorThreadTask::class)
    }

    lateinit var selectorProcessor: SelectorProcessor

    override fun runTask() {
        while (true) {
            this.selector.select()//blocking
            logger.verbose("selector:%s", this.selector)
            val keysIterator = this.selector.selectedKeys().iterator()
            logger.verbose("before selected key size:%s", this.selector.selectedKeys().size)
            while (keysIterator.hasNext()) {
                val key = keysIterator.next()
                keysIterator.remove()
                logger.debug("task:%s, isReadable:%s, isWritable:%s, key:%s", this, key.isReadable, key.isWritable, key)
                val socketChannel = key.channel() as SocketChannel
                val socketChannelHashCode = socketChannel.hashCode()
                val address = socketChannel.remoteAddress as InetSocketAddress
                try {
                    when {
                        key.isReadable -> {
                            logger.debug("isReadable, time:%s, host:%s, port:%s", Date(), address.hostString, address.port)
                            val byteArray = socketChannel.readByteArray()
                            val responseByteArray = this.selectorProcessor.process(byteArray, socketChannelHashCode)
                            key.interestOps(key.interestOps() or SelectionKey.OP_WRITE)
                            key.attach(responseByteArray)
//                            socketChannel.register(this.selector, key.interestOps() or SelectionKey.OP_WRITE, responseByteArray)
                            logger.debug("read byte array md5:%s, byte array size:%s", byteArray.MD5String(), byteArray.size)
                        }
                        key.isWritable -> {
                            logger.debug("isWritable, time:%s, host:%s, port:%s", Date(), address.hostString, address.port)
                            val attachment = key.attachment()
                            if (attachment == null) {//push
                                val pushByteArray = this.selectorProcessor.notify(socketChannelHashCode)
                                socketChannel.write(ByteBuffer.wrap(pushByteArray))
                                logger.debug("push, socketChannelHashCode:%s, byte array md5:%s, byte array size:%s", socketChannelHashCode, pushByteArray.MD5String(), pushByteArray.size)
                            } else {//write after read or notify
                                val byteArray = key.attachment() as ByteArray
                                socketChannel.write(ByteBuffer.wrap(byteArray))
                                logger.debug("write byte array md5:%s, byte array size:%s", byteArray.MD5String(), byteArray.size)
                            }
                            key.interestOps(key.interestOps() and SelectionKey.OP_WRITE.inv())//remove the write key from interest ops
                        }
                    }
                } catch (e: Throwable) {
                    logger.error("disconnect, socket channel:%s", e, socketChannel)
                    key.cancel()
                    socketChannel.close()
                    this.disconnectCallback(socketChannelHashCode)
                }
            }
            logger.verbose("after selected key size:%s", this.selector.selectedKeys().size)
        }
    }
}