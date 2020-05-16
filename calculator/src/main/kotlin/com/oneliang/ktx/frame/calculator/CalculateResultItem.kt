package com.oneliang.ktx.frame.calculator

import com.oneliang.ktx.Constants

class CalculateResultItem(val name: String = Constants.String.BLANK,
                          val code: String = Constants.String.BLANK,
                          val inputJson: String = Constants.String.BLANK,
                          var value: String = Constants.String.ZERO,
                          var codeType: String = Constants.String.BLANK)
