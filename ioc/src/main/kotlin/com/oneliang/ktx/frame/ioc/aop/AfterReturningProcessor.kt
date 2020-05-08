package com.oneliang.ktx.frame.ioc.aop

import java.lang.reflect.Method

interface AfterReturningProcessor {

    /**
     * after returning
     * @param instance
     * @param method
     * @param args
     * @param returnValue
     * @throws Throwable
     */
    @Throws(Throwable::class)
    fun afterReturning(instance: Any, method: Method, args: Array<Any>, returnValue: Any?)
}
