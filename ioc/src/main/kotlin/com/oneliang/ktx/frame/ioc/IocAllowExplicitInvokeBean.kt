package com.oneliang.ktx.frame.ioc

import com.oneliang.ktx.Constants
import java.lang.reflect.Method

internal class IocAllowExplicitInvokeBean {
    var id: String = Constants.String.BLANK
    var proxyMethod: Method? = null
    var proxyInstance: Any? = null
}
