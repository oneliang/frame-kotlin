package com.oneliang.ktx.frame.ioc

import com.oneliang.ktx.Constants

class IocBean {
    companion object {
        const val TAG_BEAN = "bean"
        const val INJECT_TYPE_AUTO_BY_TYPE = "autoByType"
        const val INJECT_TYPE_AUTO_BY_ID = "autoById"
        const val INJECT_TYPE_MANUAL = "manual"
    }

    var id: String = Constants.String.BLANK
    var type: String = Constants.String.BLANK
    var value: String = Constants.String.BLANK
    var proxy = true
    var injectType = INJECT_TYPE_AUTO_BY_ID
    var beanClass: Class<*>? = null
    var beanInstance: Any? = null
    var proxyInstance: Any? = null
    var iocConstructorBean: IocConstructorBean? = null
    val iocPropertyBeanList = mutableListOf<IocPropertyBean>()
    val iocAfterInjectBeanList = mutableListOf<IocAfterInjectBean>()

    /**
     * @param iocPropertyBean
     * @return boolean
     */
    fun addIocPropertyBean(iocPropertyBean: IocPropertyBean): Boolean {
        return iocPropertyBeanList.add(iocPropertyBean)
    }

    /**
     * @param iocAfterInjectBean
     * @return boolean
     */
    fun addIocAfterInjectBean(iocAfterInjectBean: IocAfterInjectBean): Boolean {
        return iocAfterInjectBeanList.add(iocAfterInjectBean)
    }
}

fun IocBean.Companion.build(id: String, instance: Any): IocBean {
    val iocBean = IocBean()
    iocBean.id = id
    iocBean.injectType = INJECT_TYPE_AUTO_BY_ID
    iocBean.proxy = false
    iocBean.proxyInstance = instance
    iocBean.beanInstance = instance
    iocBean.type = instance.javaClass.name
    return iocBean
}