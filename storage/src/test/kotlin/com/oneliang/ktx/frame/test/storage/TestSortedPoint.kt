package com.oneliang.ktx.frame.test.storage

import com.oneliang.ktx.frame.storage.SortedPoint
import com.oneliang.ktx.util.common.toHexString
import com.oneliang.ktx.util.common.unGzip
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.File


fun main() {
    val directory = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/storage/src/test/kotlin/new_point"


    val sortedPoint = SortedPoint(directory, true)
//    for (pointId in 0 until 11) {
//        sortedPoint.add(pointId, 1, 1.0)
//        sortedPoint.add(pointId, 2, 1.0)
//        sortedPoint.add(pointId, 3, 1.0)
//        sortedPoint.add(pointId, 4, 1.0)
//        sortedPoint.add(pointId, 5, 1.0)
//    }
//    return
//
    val contentByteArray = sortedPoint.collectContent(0)
    println(contentByteArray.toHexString())
    val contentInputStream = DataInputStream(ByteArrayInputStream(contentByteArray))
    val contentSize = contentByteArray.size / 12
    val list = mutableListOf<Pair<Int, Double>>()
    for (i in 0 until contentSize) {
        list += contentInputStream.readInt() to contentInputStream.readDouble()
    }
    println(list)
    val file = File(directory, "segment_0.ds")
    println(file.readBytes().unGzip().toHexString())
    val routeFile = File(directory, "route.ds")
    println(routeFile.readBytes().toHexString())
    return
}