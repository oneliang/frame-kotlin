package com.oneliang.ktx.frame.ai.cnn

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.FileInputStream
import java.lang.Exception
import java.nio.ByteBuffer
import java.util.*

class MnistReader(val labelFullFilename: String, val imageFullFilename: String) {

    companion object {
        private val logger = LoggerManager.getLogger(MnistReader::class)
    }

    private lateinit var labelInputStream: FileInputStream
    private lateinit var imageInputStream: FileInputStream
    private var labelSize = 0
    private var imageSize = 0
    var sizeX = 0
    var sizeY = 0

    init {
        try {
            labelInputStream = FileInputStream(labelFullFilename)
            imageInputStream = FileInputStream(imageFullFilename)
            if (readInt(labelInputStream) != 2049) error("Label file header missing")
            if (readInt(imageInputStream) != 2051) error("Image file header missing")
            labelSize = readInt(labelInputStream)
            imageSize = readInt(imageInputStream)
            if (labelSize != imageSize) throw Exception("Labels and images don't match in number.")
            sizeY = readInt(imageInputStream)
            sizeX = readInt(imageInputStream)
            logger.info("label size:%s, image size:%s, size y:%s, size x:%s".format(labelSize, imageSize, sizeY, sizeX))
        } catch (e: Throwable) {
            logger.error(Constants.String.EXCEPTION, e)
        }
    }

    private fun readInt(fileInputStream: FileInputStream): Int {
        val int32Full = ByteArray(4)
        fileInputStream.read(int32Full)
        val wrapped = ByteBuffer.wrap(int32Full)
        return wrapped.int
    }

    fun size(): Int {
        return imageSize
    }

    val maxvalue: Int
        get() = 255

    fun numOfClasses(): Int {
        return 10
    }

    fun reset() {
        try {
            labelInputStream.close()
            imageInputStream.close()
            labelInputStream = FileInputStream(labelFullFilename)
            imageInputStream = FileInputStream(imageFullFilename)
            readInt(labelInputStream)
            readInt(labelInputStream)
            readInt(imageInputStream)
            readInt(imageInputStream)
            readInt(imageInputStream)
            readInt(imageInputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readNextLabel(): Int {
        try {
            return labelInputStream.read()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }

    fun readNextImage(normalization: Boolean = true): Array<Float> {
        val size = sizeX * sizeY
        val imageArray = ByteArray(size)
        Arrays.fill(imageArray, 0.toByte())
        imageInputStream.read(imageArray, 0, size)
        val arrays = Array(size) { 0.0f }
        if (normalization) {
            for (i in 0 until size) {
                arrays[i] = (imageArray[i].toInt() and 0xFF).toFloat() / 0xFF
            }
        } else {
            for (i in 0 until size) {
                arrays[i] = imageArray[i].toFloat()
            }
        }
        return arrays
    }
}

fun main() {
    val mr = MnistReader("D:/Dandelion/data/mnist/t10k-labels-idx1-ubyte", "D:/Dandelion/data/mnist/t10k-images-idx3-ubyte")
    for (i in 0..0) {
        print(mr.readNextLabel())
    }
    print(mr.readNextLabel())
    for (i in 0..0) {
        try {
            mr.readNextImage()
        } catch (e: Exception) {
            e.printStackTrace()
            println("Crash at $i")
        }
    }
    try {
        val b = mr.readNextImage()
        for (j in b.indices) {
            if (j % 28 == 0) println()
            print((b[j].toInt() and 0xFF).toString() + "\t")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}