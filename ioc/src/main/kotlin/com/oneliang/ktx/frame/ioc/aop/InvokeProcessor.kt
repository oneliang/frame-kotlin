package com.oneliang.ktx.frame.ioc.aop

import java.lang.reflect.Method

interface InvokeProcessor {

    /**
     * invoke
     * @param instance
     * @param method
     * @param args
     * @return Object
     */
    @Throws(Throwable::class)
    operator fun invoke(instance: Any, method: Method, args: Array<Any>): Any?
}
