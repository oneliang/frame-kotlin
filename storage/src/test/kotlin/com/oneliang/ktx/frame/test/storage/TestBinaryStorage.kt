package com.oneliang.ktx.frame.test.storage

import com.oneliang.ktx.frame.storage.BinaryStorage
import com.oneliang.ktx.util.common.toByteArray
import java.io.ByteArrayOutputStream

fun main() {

    val binaryStorage = BinaryStorage("/Users/oneliang/Java/githubWorkspace/frame-kotlin/storage/src/test/kotlin/segment.dat")
    val byteArrayOutputStream = ByteArrayOutputStream()
    byteArrayOutputStream.write(1L.toByteArray())
    byteArrayOutputStream.write(2.toShort().toByteArray())
    byteArrayOutputStream.write(0L.toByteArray())
    byteArrayOutputStream.write(20L.toByteArray())
    val data = byteArrayOutputStream.toByteArray()
    val (start1, end1) = binaryStorage.write(data)
    println("start:%s, end:%s".format(start1, end1))
    val (start2, end2) = binaryStorage.write(data)
    println("start:%s, end:%s".format(start2, end2))
}