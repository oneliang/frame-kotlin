package com.oneliang.ktx.frame.ai.base

import com.aparapi.device.Device
import com.aparapi.device.OpenCLDevice
import com.oneliang.ktx.util.logging.LoggerManager

object AparapiUtil {
    private val logger = LoggerManager.getLogger(AparapiUtil::class)

    fun getDevice(deviceType: Device.TYPE): OpenCLDevice? {
        var device: OpenCLDevice? = null
        // We do not test for EXECUTION_MODE.JTP because JTP is non-OpenCL
        if (deviceType == Device.TYPE.CPU) {
            device = Device.firstCPU() as OpenCLDevice?
            if (device == null) {
                logger.warning("OpenCLDevice.CPU is NULL...OpenCL is unavailable. Setting to JTP mode.")
            }
        } else if (deviceType == Device.TYPE.GPU) {
            device = Device.best() as OpenCLDevice?
            if (device == null) {
                logger.warning("OpenCLDevice.GPU is NULL...OpenCL is unavailable. Setting to JTP mode.")
            }
        }
        return device
    }
}