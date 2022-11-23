package com.oneliang.ktx.frame.test.container

import com.oneliang.ktx.Constants

class Config {
    var slaves = emptyArray<Slave>()

    class Slave {
        var hostAddress: String = Constants.String.BLANK
        var sources = emptyArray<String>()
    }
}