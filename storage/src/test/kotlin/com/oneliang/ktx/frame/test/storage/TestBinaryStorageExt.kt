package com.oneliang.ktx.frame.test.storage

import com.oneliang.ktx.frame.storage.BlockStorageExt
import com.oneliang.ktx.frame.storage.PointValueInfo
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.logging.*
import java.io.File

fun main() {
    val directory = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/storage/src/test/kotlin/point"
    val loggerList = mutableListOf<AbstractLogger>()
//    loggerList += BaseLogger(Logger.Level.DEBUG)
    loggerList += FileLogger(Logger.Level.DEBUG, directory.toFile(), "default.log")
    val complexLogger = ComplexLogger(Logger.Level.INFO, loggerList, true)
    LoggerManager.registerLogger("*", complexLogger)

    var begin = System.currentTimeMillis()
    val fullFilename = File(directory, "block_with_cache.ds").absolutePath
    val blockStorageExt = BlockStorageExt(fullFilename, dataLength = PointValueInfo.DATA_LENGTH) { _: Int, _: Long, byteArray: ByteArray ->
        PointValueInfo.fromByteArray(byteArray)
    }
    println("read cost:%s".format(System.currentTimeMillis() - begin))
    begin = System.currentTimeMillis()
    for (i in 1..10000) {
        blockStorageExt.add(PointValueInfo(i, i, i.toDouble()), true)
    }
    blockStorageExt.flush()
    println("add cost:%s".format(System.currentTimeMillis() - begin))
}