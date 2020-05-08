package com.oneliang.ktx.frame.ioc.aop

import java.lang.reflect.Method

open class DefaultInvokeProcessor : InvokeProcessor {

    @Throws(Throwable::class)
    override fun invoke(instance: Any, method: Method, args: Array<Any>): Any? {
        return method.invoke(instance, *args)
    }
}