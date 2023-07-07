package com.oneliang.ktx.frame.storage

object Scorer {

    /**
     * @param subLength
     * @param totalLength
     * @param multiple
     * @return Double
     */
    fun score(subLength: Int, totalLength: Int, multiple: Double = 1.0): Double {
        if (subLength < 0) error("parameter[subLength] must be >= 0")
        if (totalLength <= 0) error("parameter[totalLength] must be > 0")
        if (multiple <= 0) error("parameter[multiple] must be > 0")

        return subLength.toDouble() / totalLength * multiple
    }
}