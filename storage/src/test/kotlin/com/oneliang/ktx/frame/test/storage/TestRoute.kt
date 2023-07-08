package com.oneliang.ktx.frame.test.storage

import com.oneliang.ktx.frame.storage.Route

fun main() {
    val begin = Runtime.getRuntime().totalMemory()
    println(begin)
    val fullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/storage/src/test/kotlin/route.dat"
    val route = Route(fullFilename)
    val end = Runtime.getRuntime().totalMemory()
    println(end)
    println("memory:%s".format(end - begin))
//    for (i in 0 until 1000000) {
//        route.write(i.toShort(), 0L, 20L)
//    }
}