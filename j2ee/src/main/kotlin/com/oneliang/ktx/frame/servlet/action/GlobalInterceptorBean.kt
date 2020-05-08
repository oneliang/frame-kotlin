package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants


class GlobalInterceptorBean {

    companion object {

        const val TAG_GLOBAL_INTERCEPTOR = "global-interceptor"

        const val INTERCEPTOR_MODE_BEFORE = "before"
        const val INTERCEPTOR_MODE_AFTER = "after"
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
     * @return the mode
     */
    /**
     * @param mode the mode to set
     */
    var mode: String = INTERCEPTOR_MODE_BEFORE
    /**
     * @return the interceptorInstance
     */
    /**
     * @param interceptorInstance the interceptorInstance to set
     */
    var interceptorInstance: InterceptorInterface? = null
}
