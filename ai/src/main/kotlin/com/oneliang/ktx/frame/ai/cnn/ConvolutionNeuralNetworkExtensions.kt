package com.oneliang.ktx.frame.ai.cnn

import com.oneliang.ktx.util.common.doubleIteration
import com.oneliang.ktx.util.common.singleIteration
import kotlin.math.floor

fun Array<Double>.printToMatrix(rowSize: Int) {
    for (j in this.indices) {
        print((this[j].toInt() and 0xFF).toString() + "\t")
        if ((j + 1) % rowSize == 0) println()
    }
}

fun Array<Array<Double>>.printToMatrix() {
    singleIteration(this.size) { row ->
        singleIteration(this[row].size) { column ->
            print(this[row][column].toString() + "\t")
        }
        println()
    }
}

fun calculateOutSize(inSize: Int, padding: Int, size: Int, stride: Int = 1): Int {
    return floor(((inSize + padding * 2 - size) / stride + 1).toDouble()).toInt()
}

fun Array<Double>.toTripleDimensionArray(depth: Int, rows: Int, columns: Int): Array<Array<Array<Double>>> {
    val maps = Array(depth) { Array(rows) { Array(columns) { 0.0 } } } //输出图的内容值
    var k = 0
    singleIteration(depth) { depthIndex ->
        doubleIteration(rows, columns) { row, column ->
            maps[depthIndex][row][column] = this[k++]
        }
    }
    return maps
}

fun Array<Float>.toTripleDimensionArray(depth: Int, rows: Int, columns: Int): Array<Array<Array<Float>>> {
    val maps = Array(depth) { Array(rows) { Array(columns) { 0.0f } } } //输出图的内容值
    var k = 0
    singleIteration(depth) { depthIndex ->
        doubleIteration(rows, columns) { row, column ->
            maps[depthIndex][row][column] = this[k++]
        }
    }
    return maps
}