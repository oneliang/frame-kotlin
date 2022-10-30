package com.oneliang.ktx.frame.container

interface Communicable {

    val id: String

    /**
     * only for slave data tlv package
     * @param byteArray
     */
    fun sendData(byteArray: ByteArray)

    /**
     * set communication callback
     * @param communicationCallback
     */
    fun setCommunicationCallback(communicationCallback: CommunicationCallback)

    interface CommunicationCallback {
        /**
         * on register
         * @param id
         */
        fun onRegister(id: String) {}

        /**
         * on unregister
         * @param id
         */
        fun onUnregister(id: String) {}

        /**
         * on connect
         * @param socketChannelHashCode
         */
        fun onConnect(socketChannelHashCode: Int) {}

        /**
         * on disconnect
         * @param socketChannelHashCode
         */
        fun onDisconnect(socketChannelHashCode: Int) {}

        /**
         * on receive data
         * @param id
         * @param byteArray
         */
        fun onReceiveData(id: String, byteArray: ByteArray)
    }
}