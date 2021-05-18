package com.oneliang.ktx.frame.ai.base.matrix

import com.aparapi.Kernel
import com.aparapi.Range
import com.aparapi.device.Device
import com.oneliang.ktx.frame.ai.base.AparapiUtil
import com.oneliang.ktx.util.common.toNewArray
import com.oneliang.ktx.util.math.matrix.multiply

private fun floatArray1Dx2D(aMatrix: Array<Float>, bMatrix: Array<Array<Float>>): Array<Float> {
    val fixAMatrix = Array(1) { aMatrix }
    val resultMatrix = floatArray2Dx2D(fixAMatrix, bMatrix)
    return resultMatrix[0]
}

private fun floatArray2Dx2D(aMatrix: Array<Array<Float>>, bMatrix: Array<Array<Float>>): Array<Array<Float>> {
    val newAMatrix = aMatrix.toNewArray { it.toFloatArray() }
    val newBMatrix = bMatrix.toNewArray { it.toFloatArray() }
    val resultMatrix = Array(aMatrix.size) { FloatArray(bMatrix[0].size) { 0.0f } }
    val kernel = FloatMatrixMultiply2D(newAMatrix, newBMatrix, resultMatrix)
    kernel.addExecutionModes(Kernel.EXECUTION_MODE.GPU, Kernel.EXECUTION_MODE.CPU, Kernel.EXECUTION_MODE.JTP)
    val device = AparapiUtil.getDevice(Device.TYPE.GPU)
    val range = Range.create2D(device, aMatrix.size, bMatrix[0].size)
    kernel.execute(range)
    kernel.dispose()
    return resultMatrix.toNewArray { it.toTypedArray() }
}

fun Array<Float>.multiply(bMatrix: Array<Array<Float>>, parallel: Boolean = false, gpu: Boolean = false): Array<Float> {
    return if (gpu) {
        floatArray1Dx2D(this, bMatrix)
    } else {
        this.multiply(bMatrix, parallel)
    }
}

fun Array<Array<Float>>.multiply(bMatrix: Array<Array<Float>>, parallel: Boolean = false, gpu: Boolean = false): Array<Array<Float>> {
    return if (gpu) {
        floatArray2Dx2D(this, bMatrix)
    } else {
        this.multiply(bMatrix, parallel)
    }
}