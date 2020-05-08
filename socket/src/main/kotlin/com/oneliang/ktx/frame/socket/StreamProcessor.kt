package com.oneliang.ktx.frame.socket

import java.io.InputStream
import java.io.OutputStream

interface StreamProcessor {

    @Throws(Throwable::class)
    fun process(inputStream: InputStream, outputStream: OutputStream)
}