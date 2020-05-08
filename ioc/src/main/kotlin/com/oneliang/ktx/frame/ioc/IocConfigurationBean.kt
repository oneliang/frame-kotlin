package com.oneliang.ktx.frame.ioc

class IocConfigurationBean {
    //	static final String INJECT_TYPE_MANUAL="manual"
    companion object {

        const val TAG_CONFIGURATION = "configuration"
        internal const val INJECT_TYPE_AUTO_BY_TYPE = IocBean.INJECT_TYPE_AUTO_BY_TYPE
        internal const val INJECT_TYPE_AUTO_BY_ID = IocBean.INJECT_TYPE_AUTO_BY_ID
    }
    /**
     * @return the objectInjectType
     */
    /**
     * @param objectInjectType the objectInjectType to set
     */
    var objectInjectType = INJECT_TYPE_AUTO_BY_ID
}
