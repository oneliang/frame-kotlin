package com.oneliang.ktx.frame.test.storage

import com.oneliang.ktx.frame.storage.ContentStorage
import com.oneliang.ktx.util.common.toBase64String
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.common.toHexString
import java.io.ByteArrayOutputStream


fun main() {
    println(System.currentTimeMillis().toString().toBase64String())
    return
    val directory = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/storage/src/test/kotlin/content"
//    println((directory + "/segment_0.ds").toFile().readBytes().toHexString())
//    return
    val contentStorage = ContentStorage(directory, false)

    val byteArrayOutputStream = ByteArrayOutputStream()
    byteArrayOutputStream.write(1.toByteArray())
    byteArrayOutputStream.write(1.toByteArray())
    byteArrayOutputStream.write(1.toByteArray())
    byteArrayOutputStream.write(1.toByteArray())
    val byteArray = byteArrayOutputStream.toByteArray()
    contentStorage.addContent(byteArray)


    val byteArrayOutputStream2 = ByteArrayOutputStream()
    byteArrayOutputStream2.write(2.toByteArray())
    byteArrayOutputStream2.write(2.toByteArray())
    byteArrayOutputStream2.write(2.toByteArray())
    byteArrayOutputStream2.write(2.toByteArray())
    val byteArray2 = byteArrayOutputStream2.toByteArray()
    contentStorage.addContent(byteArray2)

    val byteArrayOutputStream3 = ByteArrayOutputStream()
    byteArrayOutputStream3.write(3.toByteArray())
    byteArrayOutputStream3.write(3.toByteArray())
    byteArrayOutputStream3.write(3.toByteArray())
    byteArrayOutputStream3.write(3.toByteArray())
    val byteArray3 = byteArrayOutputStream3.toByteArray()
    contentStorage.addContent(byteArray3)

    val byteArrayOutputStream4 = ByteArrayOutputStream()
    byteArrayOutputStream4.write(4.toByteArray())
    byteArrayOutputStream4.write(4.toByteArray())
    byteArrayOutputStream4.write(4.toByteArray())
    byteArrayOutputStream4.write(4.toByteArray())
    val byteArray4 = byteArrayOutputStream4.toByteArray()
    contentStorage.replaceContent(1, byteArray4)
}