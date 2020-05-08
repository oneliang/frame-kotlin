package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.socket.nio.ClientManager

fun main() {
    ClientManager("localhost", 9999).also { clientManager ->
        clientManager.readProcessor = {
            println(String(it))
        }
        clientManager.start()
        Thread.sleep(1000)
        clientManager.send(ByteArray(0))
    }
//    Client("localhost", 9999).start()
}