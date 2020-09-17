package com.oneliang.ktx.frame.mail

import com.oneliang.ktx.Constants

class ToAddress {
    var type = Type.TO
    var address = Constants.String.BLANK

    constructor(address: String) {
        this.address = address
    }

    constructor() {

    }

    constructor(type: Type, address: String) {
        this.type = type
        this.address = address
    }

    enum class Type {
        TO, BCC, CC
    }
}