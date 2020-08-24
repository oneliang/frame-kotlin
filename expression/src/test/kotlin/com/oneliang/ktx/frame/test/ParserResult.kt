package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.test.function.Complex
import com.oneliang.ktx.frame.test.function.INVALID_COMPLEX

/**
 * The Class ParserResult.
 */
class ParserResult {
    var value: Double = 0.0
        set(value) {
            field = value
            isComplex = false
        }
    var complexValue: Complex = INVALID_COMPLEX
        set(complexValue) {
            field = complexValue
            isComplex = true
        }
    var isComplex = false
}