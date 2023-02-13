package com.oneliang.ktx.frame.tokenization

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.isChinese

open class DefaultCategorizationStrategy(private val mixNumberAndEnglishLetter: Boolean = true) : Dictionary.CategorizationStrategy {

    /**
     * check the same category
     * @param currentChar
     * @param nextChar
     * @return Boolean
     */
    override fun checkTheSameCategory(currentChar: Char, nextChar: Char): Boolean {
        return if (this.mixNumberAndEnglishLetter) {
            if ((currentChar.isDigit() && nextChar.isDigit())
                    || (currentChar.isDigit() && nextChar.isLowerCase())
                    || (currentChar.isDigit() && nextChar.isUpperCase())
                    || (currentChar.isLowerCase() && nextChar.isDigit())
                    || (currentChar.isLowerCase() && nextChar.isLowerCase())
                    || (currentChar.isLowerCase() && nextChar.isUpperCase())
                    || (currentChar.isUpperCase() && nextChar.isDigit())
                    || (currentChar.isUpperCase() && nextChar.isLowerCase())
                    || (currentChar.isUpperCase() && nextChar.isUpperCase())) {
                true
            } else if ((currentChar.isDigit() && nextChar == Constants.Ascii.DOT)
                    || (currentChar == Constants.Symbol.DOT_CHAR && nextChar.isDigit())) {
                true
            } else {
                currentChar.isChinese() && nextChar.isChinese()
            }
        } else {
            if (currentChar.isDigit() && nextChar.isDigit()) {
                true
            } else if ((currentChar.isLowerCase() && nextChar.isLowerCase())
                    || (currentChar.isLowerCase() && nextChar.isUpperCase())
                    || (currentChar.isUpperCase() && nextChar.isLowerCase())
                    || (currentChar.isUpperCase() && nextChar.isUpperCase())) {
                true
            } else currentChar.isChinese() && nextChar.isChinese()
        }
    }
}