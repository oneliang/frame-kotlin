package com.oneliang.ktx.frame.ioc

import com.oneliang.ktx.Constants

class IocPropertyBean {

    companion object {
        const val TAG_PROPERTY = "property"
    }

    /**
     * @return the name
     */
    /**
     * @param name the name to set
     */
    var name: String = Constants.String.BLANK
    /**
     * @return the reference
     */
    /**
     * @param reference the reference to set
     */
    var reference: String = Constants.String.BLANK
}
