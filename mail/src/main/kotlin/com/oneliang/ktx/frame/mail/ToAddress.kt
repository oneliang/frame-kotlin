package com.oneliang.ktx.frame.mail

import com.oneliang.ktx.Constants

class ToAddress(var type: Type = Type.TO, var address: String = Constants.String.BLANK) {

    enum class Type {
        TO, BCC, CC
    }
}