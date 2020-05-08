package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants

class GlobalForwardBean {

    companion object {
        const val TAG_GLOBAL_FORWARD = "global-forward"
    }

    /**
     * @return the name
     */
    /**
     * @param name the name to set
     */
    var name: String = Constants.String.BLANK
    /**
     * @return the path
     */
    /**
     * @param path the path to set
     */
    var path: String = Constants.String.BLANK
}
