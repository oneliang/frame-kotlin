package com.oneliang.ktx.frame.expression

import com.oneliang.ktx.util.common.perform

fun String.eval(): Any = perform({
    Expression.eval(this) ?: false
}, failure = {
    false
})