package com.oneliang.ktx.frame.test.ai.aparapi.matrix

import com.aparapi.Kernel.EXECUTION_MODE
import com.aparapi.Range
import com.aparapi.device.Device
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.json.toJson

private fun testIntArray() {
    val aMatrix = IntArray(10) { 1 }
    val bMatrix = IntArray(10) { 1 }
    val begin = System.currentTimeMillis()
    val resultMatrix1D = IntArray(aMatrix.size) { 0 }
    val kernel = IMatMul1D(aMatrix, bMatrix, resultMatrix1D, aMatrix.size)
    kernel.addExecutionModes(EXECUTION_MODE.GPU, EXECUTION_MODE.CPU, EXECUTION_MODE.JTP)
    val device = AparapiUtil.getDevice(Device.TYPE.GPU)
    println("device:$device")
    val range = Range.create(device, aMatrix.size)
    kernel.execute(range)
    kernel.dispose()
    println("cost:" + (System.currentTimeMillis() - begin))
    println(kernel.C.toJson())
}

private fun testFloatArray() {
    val aMatrix = FloatArray(10) { 1.0f }
    val bMatrix = FloatArray(10) { 1.0f }
    val begin = System.currentTimeMillis()
    val resultMatrix1D = FloatArray(aMatrix.size) { 0.0f }
    val kernel = FMatMul1D(aMatrix, bMatrix, resultMatrix1D, aMatrix.size)
    kernel.addExecutionModes(EXECUTION_MODE.GPU, EXECUTION_MODE.CPU, EXECUTION_MODE.JTP)
    val device = AparapiUtil.getDevice(Device.TYPE.GPU)
    println("device:$device")
    val range = Range.create(device, aMatrix.size)
    kernel.execute(range)
    kernel.dispose()
    println("cost:" + (System.currentTimeMillis() - begin))
    println(kernel.C.toJson())
}

private fun testFloatArray2D() {
    val aMatrix = Array(10) { FloatArray(10) { 1.0f } }
    val bMatrix = Array(10) { FloatArray(10) { 1.0f } }
    val resultMatrix = Array(aMatrix.size) { FloatArray(bMatrix[0].size) { 0.0f } }
    val begin = System.currentTimeMillis()
    val kernel = FloatMatrixMultiply2D(aMatrix, bMatrix, resultMatrix, aMatrix.size)
    kernel.addExecutionModes(EXECUTION_MODE.GPU, EXECUTION_MODE.CPU, EXECUTION_MODE.JTP)
    val device = AparapiUtil.getDevice(Device.TYPE.GPU)
    println("device:$device")
    val range = Range.create2D(device, aMatrix.size, bMatrix[0].size)
    kernel.execute(range)
    kernel.dispose()
    println("cost:" + (System.currentTimeMillis() - begin))
    println(kernel.C.toJson())
}

private fun testFloatArray3D() {
    val aMatrix = Array(10) { Array(10) { FloatArray(10) { 1.0f } } }
    val bMatrix = Array(10) { Array(10) { FloatArray(10) { 1.0f } } }
    val resultMatrix = Array(aMatrix.size) { Array(10) { FloatArray(bMatrix[0].size) { 0.0f } } }
    val begin = System.currentTimeMillis()
    val kernel = FMatMul3D(aMatrix, bMatrix, resultMatrix, aMatrix.size)
    kernel.addExecutionModes(EXECUTION_MODE.GPU, EXECUTION_MODE.CPU, EXECUTION_MODE.JTP)
    val device = AparapiUtil.getDevice(Device.TYPE.GPU)
    println("device:$device")
    val range = Range.create(device, resultMatrix.size * resultMatrix[0].size * resultMatrix[0][0].size)
    kernel.execute(range)
    kernel.dispose()
    println("cost:" + (System.currentTimeMillis() - begin))
    println(kernel.cMatrix.toJson())
}


private fun testDoubleArray2D() {
    val aMatrix = Array(10) { DoubleArray(10) { 1.0 } }
    val bMatrix = Array(10) { DoubleArray(10) { 1.0 } }
    val resultMatrix = Array(aMatrix.size) { DoubleArray(bMatrix[0].size) { 0.0 } }
    val begin = System.currentTimeMillis()
    val kernel = DMatMul2D(aMatrix, bMatrix, resultMatrix, aMatrix.size)
    kernel.addExecutionModes(EXECUTION_MODE.GPU, EXECUTION_MODE.CPU, EXECUTION_MODE.JTP)
    val device = AparapiUtil.getDevice(Device.TYPE.GPU)
    println("device:$device")
    val range = Range.create(device, aMatrix.size)
    kernel.execute(range)
    kernel.dispose()
    println("cost:" + (System.currentTimeMillis() - begin))
    println(kernel.C.toJson())
}


private fun testLongArray2D() {
    val aMatrix = Array(10) { Array(10) { 1L } }.toNewArray { it.toLongArray() }
    val bMatrix = Array(10) { Array(10) { 1L } }.toNewArray { it.toLongArray() }
    val resultMatrix = Array(aMatrix.size) { LongArray(bMatrix[0].size) { 0L } }
    val begin = System.currentTimeMillis()
    val kernel = LMatMul2D(aMatrix, bMatrix, resultMatrix, aMatrix.size)
    kernel.addExecutionModes(EXECUTION_MODE.GPU, EXECUTION_MODE.CPU, EXECUTION_MODE.JTP)
    val device = AparapiUtil.getDevice(Device.TYPE.GPU)
    println("device:$device")
    val range = Range.create(device, aMatrix.size)
    kernel.execute(range)
    kernel.dispose()
    println("cost:" + (System.currentTimeMillis() - begin))
    println(kernel.C.toJson())
}

private fun testCorrMatrix() {
    val scale = 100000000000000000L
//    val scale = 1L
    val aMatrix = Array(10) { Array(10) { (2.0 * scale).toLong() } }.toNewArray { it.toLongArray() }
    val bMatrix = Array(10) { Array(10) { (2.0 * scale).toLong() } }.toNewArray { it.toLongArray() }
    println(aMatrix.toJson())
    println(bMatrix.toJson())
    println(aMatrix[0][0] * bMatrix[0][0] / scale.toDouble())
    val begin = System.currentTimeMillis()
    val gpuResultMatrix = CorrMatrixHost.intersectionMatrix(aMatrix, bMatrix, Device.TYPE.GPU)
    println(gpuResultMatrix.toJson())
    val results = gpuResultMatrix.toNewArray { it -> it.toTypedArray().mapToNewArray { (it.toDouble() / scale) } }
    println(results.toJson())
    println("cost:" + (System.currentTimeMillis() - begin))
}

fun main() {
//    val a = (-2.0).toRawLongBits()
//    val b = 2.0.toRawLongBits()
//    println(a.toByteArray().toHexString())
//    println(b.toByteArray().toHexString())
//    testIntArray()
//    testFloatArray()
    testFloatArray2D()
//    testFloatArray3D()
//    testDoubleArray2D()
//    testLongArray2D()
//    testCorrMatrix()
    return
    val matrixA = Array(2000) { LongArray(2000) { 1L } }
    val matrixB = Array(2000) { LongArray(2000) { 1L } }
    val matrixC = Array(2000) { IntArray(2000) { 2 } }
    val matrixD = Array(2000) { IntArray(2000) { 2 } }
//    matrixC.multiply(matrixD)
//    println("cost:" + (System.currentTimeMillis() - begin))
    val gpuResultMatrix = CorrMatrixHost.intersectionMatrix(matrixA, matrixB, Device.TYPE.GPU)
//    println(gpuResultMatrix.toJson())
//    val aMatrix = arrayOf(arrayOf(1.0), arrayOf(2.0))
//    val bMatrix = arrayOf(arrayOf(1.0, 2.0, 3.0))
//    val resultMatrix = Array(matrixC.size) { DoubleArray(matrixD[0].size) { 0.0 } }
//    val resultMatrix = Array(matrixC.size) { IntArray(matrixD[0].size) { 1 } }
//    val kernel = MatrixMultiplyKernel(matrixC, matrixD, resultMatrix)
//    val resultMatrix = Array(matrixC.size) { DoubleArray(matrixD[0].size) { 0.0 } }

//    val kernel = DMatMul2D(matrixC, matrixD, resultMatrix, matrixC.size)
//    val kernel = DMatMul1D(matrixE, matrixF, resultMatrix1D, matrixE.size)
//    val kernel = MultiplyKernel(matrixE, matrixF, resultMatrix1D)

//    kernel.isExplicit = true//for memory operate

//    println(aMatrix.size * bMatrix[0].size)
//    val range = Range.create2D(aMatrix.size, bMatrix[0].size)
//    println("device:$device")
//    val range = Range.create2D(device, matrixC.size, matrixD[0].size)
//    val range = Range.create(device, matrixC.size * matrixD[0].size)
//    kernel.execute(range)
//    println(aMatrix.multiply(bMatrix).toJson())
}