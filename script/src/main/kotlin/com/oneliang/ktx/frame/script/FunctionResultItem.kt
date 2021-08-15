package com.oneliang.ktx.frame.script

import com.oneliang.ktx.Constants

class FunctionResultItem(
    val name: String = Constants.String.BLANK,
    val code: String = Constants.String.BLANK,
    val originalCode: String = code,
    val inputJson: String = Constants.String.BLANK,
    var value: String = Constants.String.BLANK,
    var codeType: String = Constants.String.BLANK
)
