package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants

class ActionInterceptorBean {

    companion object {
        const val TAG_INTERCEPTOR = "interceptor"
    }

    object Mode {
        const val BEFORE = "before"
        const val AFTER = "after"
    }

    /**
     * @return the id
     */
    /**
     * @param id the id to set
     */
    var id: String = Constants.String.BLANK
    /**
     * @return the mode
     */
    /**
     * @param mode the mode to set
     */
    var mode: String = Mode.BEFORE
    /**
     * @return the interceptorInstance
     */
    /**
     * @param interceptorInstance the interceptorInstance to set
     */
    lateinit var interceptorInstance: InterceptorInterface
}
