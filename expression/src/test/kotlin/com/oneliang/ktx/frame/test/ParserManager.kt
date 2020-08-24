package com.oneliang.ktx.frame.test

/**
 * The Class ParserManager.
 */
class ParserManager
/**
 * Instantiates a new parser manager.
 */
protected constructor() {
    /**
     * isDeegre.
     *
     * @return true, if is deegre
     */
    /**
     * setDeegre.
     *
     * @param deegre the new deegre
     */
    /** The deegre.  */
    var isDeegre = false

    companion object {
        // ..... Other configuration values //
        /**
         * getInstance.
         *
         * @return single instance of ParserManager
         */
        /** The instance.  */
        var instance: ParserManager? = null
            get() {
                if (field == null) {
                    field = ParserManager()
                }
                return field
            }
            private set

    }
}