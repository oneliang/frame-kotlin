package com.oneliang.ktx.frame.mail

class ToAddress {
    /**
     * @return the type
     */
    /**
     * @param type the type to set
     */
    var type = Type.TO
    /**
     * @return the address
     */
    /**
     * @param address the address to set
     */
    var address: String? = null

    constructor(address: String?) {
        this.address = address
    }

    constructor(type: Type, address: String?) {
        this.type = type
        this.address = address
    }

    enum class Type {
        TO, BCC, CC
    }
}