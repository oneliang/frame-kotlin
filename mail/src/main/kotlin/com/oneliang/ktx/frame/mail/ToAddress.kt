package com.oneliang.ktx.frame.mail

class ToAddress(var address: String, var type: Type = Type.TO) {

    enum class Type {
        TO, BCC, CC
    }
}