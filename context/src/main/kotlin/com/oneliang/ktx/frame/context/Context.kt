package com.oneliang.ktx.frame.context

interface Context {

    /**
     * Method:context initialize
     * @param parameters
     */
    fun initialize(parameters: String)

    /**
     * Method:destroy
     * @throws Exception
     */
    fun destroy()
}
