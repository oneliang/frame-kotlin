package com.oneliang.ktx.frame.test.search

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.tokenization.DefaultCategorizationStrategy

class DimensionSupportCategorizationStrategy : DefaultCategorizationStrategy(true) {

    /**
     * check the same category
     * @param currentChar
     * @param nextChar
     * @return Boolean
     */
    override fun checkTheSameCategory(currentChar: Char, nextChar: Char): Boolean {
        return if ((currentChar.isDigit() && nextChar == Constants.Ascii.WILDCARD)
                || currentChar == Constants.Ascii.WILDCARD && nextChar.isDigit()) {
            true
        } else {
            super.checkTheSameCategory(currentChar, nextChar)
        }
    }
}