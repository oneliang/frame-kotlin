package com.oneliang.ktx.frame.test.storage

import com.oneliang.ktx.frame.storage.Point
import com.oneliang.ktx.util.common.toHexString
import java.io.File

fun main() {
    val fullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/storage/src/test/kotlin/point"
    println(File(fullFilename).readBytes().toHexString())
    return

    val begin = Runtime.getRuntime().totalMemory()
    println(begin)
    val point = Point(fullFilename, pageValueCount = 2)
    val end = Runtime.getRuntime().totalMemory()
    println(end)
    println("memory:%s".format(end - begin))
    point.add(1, 1, 1.2)
    point.add(1, 2, 1.0)
    point.add(1, 3, 1.0)
}



