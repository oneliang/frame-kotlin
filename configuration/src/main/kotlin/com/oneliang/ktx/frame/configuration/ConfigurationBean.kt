package com.oneliang.ktx.frame.configuration

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.context.Context

class ConfigurationBean {

    companion object {
        const val TAG_CONFIGURATION = "configuration"
    }
    /**
     * @return the id
     */
    /**
     * @param id the id to set
     */
    var id: String = Constants.String.BLANK
    /**
     * @return the contextClass
     */
    /**
     * @param contextClass the contextClass to set
     */
    var contextClass: String = Constants.String.BLANK
    /**
     * @return the parameters
     */
    /**
     * @param parameters the parameters to set
     */
    var parameters: String = Constants.String.BLANK
    /**
     * @return the contextInstance
     */
    /**
     * @param contextInstance the contextInstance to set
     */
    var contextInstance: Context? = null
}
