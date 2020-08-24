package com.oneliang.ktx.frame.test.util

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.test.function.Complex
import com.oneliang.ktx.frame.test.function.INVALID_COMPLEX

class Point {
    var variable = Constants.String.BLANK
    var doubleValue = 0.0
    var complexValue = INVALID_COMPLEX
    var stringValue = Constants.String.BLANK
    var isComplex = false

    constructor() {
    }

    constructor(variable: String, doubleValue: Double) {
        this.variable = variable
        this.doubleValue = doubleValue
        this.isComplex = false
    }

    constructor(variable: String, complexValue: Complex) {
        this.variable = variable
        this.complexValue = complexValue
        this.isComplex = true
    }

    constructor(variable: String, stringValue: String) {
        this.variable = variable
        this.stringValue = stringValue
        this.isComplex = false
    }
}