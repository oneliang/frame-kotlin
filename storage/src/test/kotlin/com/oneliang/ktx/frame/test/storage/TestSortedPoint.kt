package com.oneliang.ktx.frame.test.storage

import com.oneliang.ktx.frame.storage.SortedPoint
import com.oneliang.ktx.util.common.toHexString
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.File


fun main() {
    val directory = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/storage/src/test/kotlin/new_point"

    val file = File(directory, "route.ds")
    println(file.readBytes().toHexString())
    return

    val sortedPoint = SortedPoint(directory, false)
    sortedPoint.add(1, 1, 1.0)
    sortedPoint.add(1, 2, 2.0)
    sortedPoint.add(1, 3, 3.0)
    return

//    val dataByteArrayOutputStream = ByteArrayOutputStream()
//    dataByteArrayOutputStream.write(3.toByteArray())
//    dataByteArrayOutputStream.write(0.3.toLongBits().toByteArray())
//    dataByteArrayOutputStream.write(2.toByteArray())
//    dataByteArrayOutputStream.write(0.2.toLongBits().toByteArray())
//    dataByteArrayOutputStream.write(1.toByteArray())
//    dataByteArrayOutputStream.write(0.1.toLongBits().toByteArray())
//    val dataByteArray = dataByteArrayOutputStream.toByteArray()
//    newPoint.addContent(dataByteArray)
//    return


    val contentByteArray = sortedPoint.collectContent(1)
    println(contentByteArray.toHexString())
    val contentInputStream = DataInputStream(ByteArrayInputStream(contentByteArray))
    val contentSize = contentByteArray.size / 12
    val list = mutableListOf<Pair<Int, Double>>()
    for (i in 0 until contentSize) {
        list += contentInputStream.readInt() to contentInputStream.readDouble()
    }
    println(list)
    val sortedList = list.sortedBy { it.second }
    println(sortedList)
}