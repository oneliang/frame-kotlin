package com.oneliang.ktx.frame.test.configuration.dsl

import com.oneliang.ktx.frame.configuration.dsl.configuration
import com.oneliang.ktx.frame.configuration.dsl.configurationContext


fun main(){

    configurationContext {
        configuration("can not blank"){

        }

    }
}