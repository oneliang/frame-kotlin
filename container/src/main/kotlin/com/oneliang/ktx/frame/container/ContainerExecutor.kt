package com.oneliang.ktx.frame.container

interface ContainerExecutor {

    var communicable: Communicable

    /**
     * initialize
     */
    fun initialize()

    /**
     * execute
     */
    fun execute()

    /**
     * destroy
     */
    fun destroy()
}