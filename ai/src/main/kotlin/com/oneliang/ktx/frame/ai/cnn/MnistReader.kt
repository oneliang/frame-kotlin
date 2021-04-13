package com.oneliang.ktx.frame.ai.cnn

import java.io.FileInputStream
import java.lang.Exception
import java.nio.ByteBuffer
import java.util.*

class MnistReader(val labelFullFilename: String, val imageFullFilename: String) {

    private lateinit var labelIO: FileInputStream
    private lateinit var imageIO: FileInputStream
    private var labelSize = 0
    private var imageSize = 0
    var sizeX = 0
    var sizeY = 0

    init {
        try {
            labelIO = FileInputStream(labelFullFilename)
            imageIO = FileInputStream(imageFullFilename)
            if (readInt(labelIO) != 2049) error("Label file header missing")
            if (readInt(imageIO) != 2051) error("Image file header missing")
            labelSize = readInt(labelIO)
            imageSize = readInt(imageIO)
            if (labelSize != imageSize) throw Exception("Labels and images don't match in number.")
            sizeY = readInt(imageIO)
            sizeX = readInt(imageIO)
            println("label size:%s, image size:%s, size y:%s, size x:%s".format(labelSize, imageSize, sizeY, sizeX))
        } catch (e: Exception) {
            e.printStackTrace()
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
            labelIO.close()
            imageIO.close()
            labelIO = FileInputStream(labelFullFilename)
            imageIO = FileInputStream(imageFullFilename)
            readInt(labelIO)
            readInt(labelIO)
            readInt(imageIO)
            readInt(imageIO)
            readInt(imageIO)
            readInt(imageIO)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readNextLabel(): Int {
        try {
            return labelIO.read()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }

    fun readNextImage(): Array<Double> {
        val size = sizeX * sizeY
        val imageArray = ByteArray(size)
        Arrays.fill(imageArray, 0.toByte())
        imageIO.read(imageArray, 0, size)
        val arrays = Array(size) { 0.0 }
        for (i in 0 until size) {
            arrays[i] = imageArray[i].toDouble()
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