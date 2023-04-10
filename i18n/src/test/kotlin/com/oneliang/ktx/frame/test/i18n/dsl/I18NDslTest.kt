package com.oneliang.ktx.frame.test.i18n.dsl

import com.oneliang.ktx.frame.configuration.dsl.configurationContext
import com.oneliang.ktx.frame.i18n.dsl.messageContext

fun main(){
    configurationContext {
        messageContext {
            it.parameter = "-R,-P=/language/,-F=*.properties"
        }
    }
}