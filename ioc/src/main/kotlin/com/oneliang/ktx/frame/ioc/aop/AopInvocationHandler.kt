package com.oneliang.ktx.frame.ioc.aop

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.util.concurrent.CopyOnWriteArrayList

class AopInvocationHandler<T : Any>(private val interfaceImpl: T) : InvocationHandler {

    /**
     * proxy method invoke
     */
    @Throws(Throwable::class)
    override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
        //		logger.log("proxy invocation:"+interfaceImpl.getClass().getName()+"--"+method.getName())
        val instance: Any?
        for (beforeInvokeProcessor in beforeInvokeProcessorList) {
            beforeInvokeProcessor.beforeInvoke(this.interfaceImpl, method, args ?: emptyArray())
        }
        try {
            instance = invokeProcessor.invoke(this.interfaceImpl, method, args ?: emptyArray())
        } catch (e: Throwable) {
            for (afterThrowingProcessor in afterThrowingProcessorList) {
                afterThrowingProcessor.afterThrowing(this.interfaceImpl, method, args ?: emptyArray(), e)
            }
            throw e
        }

        for (afterReturningProcessor in afterReturningProcessorList) {
            afterReturningProcessor.afterReturning(this.interfaceImpl, method, args ?: emptyArray(), instance)
        }
        return instance
    }

    companion object {
        private val beforeInvokeProcessorList = CopyOnWriteArrayList<BeforeInvokeProcessor>()
        private val afterReturningProcessorList = CopyOnWriteArrayList<AfterReturningProcessor>()
        private val afterThrowingProcessorList = CopyOnWriteArrayList<AfterThrowingProcessor>()
        private var invokeProcessor: InvokeProcessor = DefaultInvokeProcessor()

        /**
         * add BeforeInvokeProcessor
         * @param beforeInvokeProcessor
         */
        fun addBeforeInvokeProcessor(beforeInvokeProcessor: BeforeInvokeProcessor) {
            beforeInvokeProcessorList.add(beforeInvokeProcessor)
        }

        /**
         * add AfterReturningProcessor
         * @param afterReturningProcessor
         */
        fun addAfterReturningProcessor(afterReturningProcessor: AfterReturningProcessor) {
            afterReturningProcessorList.add(afterReturningProcessor)
        }

        /**
         * add AfterThrowingProcessor
         */
        fun addAfterThrowingProcessor(afterThrowingProcessor: AfterThrowingProcessor) {
            afterThrowingProcessorList.add(afterThrowingProcessor)
        }

        /**
         * @param invokeProcessor the invokeProcessor to set
         */
        fun setInvokeProcessor(invokeProcessor: InvokeProcessor) {
            AopInvocationHandler.invokeProcessor = invokeProcessor
        }
    }
}