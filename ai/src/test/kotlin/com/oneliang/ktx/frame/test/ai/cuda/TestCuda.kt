package com.oneliang.ktx.frame.test.ai.cuda

//import jcuda.Pointer
//import jcuda.runtime.JCuda
//
//import jcuda.runtime.cudaDeviceProp
//import jcuda.driver.JCudaDriver.cuMemFree
//import jcuda.Sizeof
//import jcuda.driver.JCudaDriver.cuMemcpyDtoH
//import jcuda.driver.JCudaDriver.cuMemAlloc
//import jcuda.driver.CUdeviceptr
//import jcuda.driver.JCudaDriver.cuMemcpyHtoD
//import jcuda.driver.JCudaDriver.cuCtxCreate
//import jcuda.driver.CUcontext
//import jcuda.driver.JCudaDriver.cuDeviceGet
//import jcuda.driver.CUdevice
//import jcuda.driver.JCudaDriver.cuInit
//import jcuda.driver.JCudaDriver

//private fun vecDoubleSample(){
//    // Enable exceptions and omit all subsequent error checks
//    // Enable exceptions and omit all subsequent error checks
//    JCudaDriver.setExceptionsEnabled(true)
//
//    // Initialize the driver and create a context for the first device.
//
//    // Initialize the driver and create a context for the first device.
//    cuInit(0)
//    val device = CUdevice()
//    cuDeviceGet(device, 0)
//    val context = CUcontext()
//    cuCtxCreate(context, 0, device)
//
//    // Afterwards, initialize the vector library, which will
//    // attach to the current context
//
//    // Afterwards, initialize the vector library, which will
//    // attach to the current context
////    VecDouble.init()
//
//    // Allocate and fill the host input data
//
//    // Allocate and fill the host input data
//    val n = 50000
//    val hostX = DoubleArray(n)
//    val hostY = DoubleArray(n)
//    for (i in 0 until n) {
//        hostX[i] = i.toDouble()
//        hostY[i] = i.toDouble()
//    }
//
//    // Allocate the device pointers, and copy the
//    // host input data to the device
//
//    // Allocate the device pointers, and copy the
//    // host input data to the device
//    val deviceX = CUdeviceptr()
//    cuMemAlloc(deviceX, (n * Sizeof.DOUBLE).toLong())
//    cuMemcpyHtoD(deviceX, Pointer.to(hostX), (n * Sizeof.DOUBLE).toLong())
//
//    val deviceY = CUdeviceptr()
//    cuMemAlloc(deviceY, (n * Sizeof.DOUBLE).toLong())
//    cuMemcpyHtoD(deviceY, Pointer.to(hostY), (n * Sizeof.DOUBLE).toLong())
//
//    val deviceResult = CUdeviceptr()
//    cuMemAlloc(deviceResult, (n * Sizeof.DOUBLE).toLong())
//
//    // Perform the vector operations
//
//    // Perform the vector operations
////    VecDouble.cos(n, deviceX, deviceX) // x = cos(x)
//
////    VecDouble.mul(n, deviceX, deviceX, deviceX) // x = x*x
//
////    VecDouble.sin(n, deviceY, deviceY) // y = sin(y)
//
////    VecDouble.mul(n, deviceY, deviceY, deviceY) // y = y*y
//
////    VecDouble.add(n, deviceResult, deviceX, deviceY) // result = x+y
//
//
//    // Allocate host output memory and copy the device output
//    // to the host.
//
//    // Allocate host output memory and copy the device output
//    // to the host.
//    val hostResult = DoubleArray(n)
//    cuMemcpyDtoH(Pointer.to(hostResult), deviceResult, (n * Sizeof.DOUBLE).toLong())
//
//    // Verify the result
//
//    // Verify the result
//    var passed = true
//    for (i in 0 until n) {
//        val expected = Math.cos(hostX[i]) * Math.cos(hostX[i]) +
//                Math.sin(hostY[i]) * Math.sin(hostY[i])
//        if (Math.abs(hostResult[i] - expected) > 1e-14) {
//            println(
//                "At index " + i + " found " + hostResult[i] +
//                        " but expected " + expected
//            )
//            passed = false
//            break
//        }
//    }
//    println("Test " + (if (passed) "PASSED" else "FAILED"))
//
//    // Clean up.
//
//    // Clean up.
//    cuMemFree(deviceX)
//    cuMemFree(deviceY)
//    cuMemFree(deviceResult)
////    VecDouble.shutdown()
//}
//
//private fun jcudaPrintDeviceInfo() {
//    JCuda.setExceptionsEnabled(true)
//    val deviceCount = intArrayOf(0)
//    JCuda.cudaGetDeviceCount(deviceCount)
//    println("Found " + deviceCount[0] + " devices")
//    for (device in 0 until deviceCount[0]) {
//        println("Properties of device $device:")
//        val deviceProperties = cudaDeviceProp()
//        JCuda.cudaGetDeviceProperties(deviceProperties, device)
//        println(deviceProperties.toFormattedString())
//    }
//}
//
//fun main() {
////    jcudaPrintDeviceInfo()
//    val pointer = Pointer()
//    JCuda.cudaHostAllocMapped
//    JCuda.cudaMalloc(pointer, 4)
//    println("Pointer: $pointer")
//    JCuda.cudaFree(pointer)
//}