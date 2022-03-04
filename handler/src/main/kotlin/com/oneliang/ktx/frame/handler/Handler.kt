package com.oneliang.ktx.frame.handler

interface Handler<T : Any> {
    fun execute(task: (T) -> Unit)
}