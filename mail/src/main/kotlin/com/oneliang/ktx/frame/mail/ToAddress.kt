package com.oneliang.ktx.frame.mail

class ToAddress(var address: String, var type: String = Type.TO.value) {

    enum class Type(val value: String) {
        TO("TO"), BCC("BCC"), CC("CC")
    }
}