package com.oneliang.ktx.frame.plugin

interface Plugin {
    val id: String

    /**
     * initialize
     */
    fun initialize()

    /**
     * dispatch
     * @param command
     */
    fun dispatch(command: Command)

    /**
     * public action
     * @return String[]
     */
    fun publicAction(): Array<String>

    /**
     * destroy
     */
    fun destroy()

    class Command(var action: String, var data: Any? = null, var callback: Callback? = null) {
        interface Callback {
            fun callback(data: Any?)
        }
    }
}