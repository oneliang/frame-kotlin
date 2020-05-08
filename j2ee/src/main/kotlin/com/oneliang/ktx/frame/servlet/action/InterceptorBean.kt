package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants


class InterceptorBean {

    companion object {
        const val TAG_INTERCEPTOR = "interceptor"
    }
    /**
     * @return the id
     */
    /**
     * @param id the id to set
     */
    var id: String = Constants.String.BLANK
    /**
     * @return the type
     */
    /**
     * @param type the type to set
     */
    var type: String? = null
    /**
     * @return the interceptorInstance
     */
    /**
     * @param interceptorInstance the interceptorInstance to set
     */
    var interceptorInstance: InterceptorInterface? = null
}
