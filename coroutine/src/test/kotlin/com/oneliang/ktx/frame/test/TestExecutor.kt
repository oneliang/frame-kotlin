package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.coroutine.Coroutine

fun main() {
    val coroutine = Coroutine()
    coroutine.runBlocking {
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
}