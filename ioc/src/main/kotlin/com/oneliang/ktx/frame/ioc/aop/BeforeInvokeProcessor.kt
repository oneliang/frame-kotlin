package com.oneliang.ktx.frame.ioc.aop

import java.lang.reflect.Method

interface BeforeInvokeProcessor {

    /**
     * before invoke
     * @param instance
     * @param method
     * @param args
     * @throws Throwable
     */
    @Throws(Throwable::class)
    fun beforeInvoke(instance: Any, method: Method, args: Array<Any>)
}
