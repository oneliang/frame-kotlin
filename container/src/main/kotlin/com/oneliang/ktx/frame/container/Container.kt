package com.oneliang.ktx.frame.container

interface Container {

    /**
     * start
     */
    fun start()

    /**
     * stop
     */
    fun stop()

    /**
     * restart, default stop first, start second
     */
    fun restart()
}