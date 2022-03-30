package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.coroutine.Coroutine

suspend fun main() {
    val coroutine = Coroutine()
    coroutine.async {
        println(1)
        sync {
            println(2)
            sync {
                println(3)
                sync { println(4) }
            }
        }
    }.join()
}