package com.oneliang.ktx.frame.test.ai.cuda

import jcuda.Pointer
import jcuda.runtime.JCuda

import jcuda.runtime.cudaDeviceProp


private fun jcudaPrintDeviceInfo() {
    JCuda.setExceptionsEnabled(true)
    val deviceCount = intArrayOf(0)
    JCuda.cudaGetDeviceCount(deviceCount)
    println("Found " + deviceCount[0] + " devices")
    for (device in 0 until deviceCount[0]) {
        println("Properties of device $device:")
        val deviceProperties = cudaDeviceProp()
        JCuda.cudaGetDeviceProperties(deviceProperties, device)
        println(deviceProperties.toFormattedString())
    }
}

fun main() {
//    jcudaPrintDeviceInfo()
    val pointer = Pointer()
    JCuda.cudaHostAllocMapped
    JCuda.cudaMalloc(pointer, 4)
    println("Pointer: $pointer")
    JCuda.cudaFree(pointer)
}