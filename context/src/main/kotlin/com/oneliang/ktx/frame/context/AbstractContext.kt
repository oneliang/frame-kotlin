package com.oneliang.ktx.frame.context

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.replaceAllLines
import com.oneliang.ktx.util.common.replaceAllSpace
import com.oneliang.ktx.util.jar.JarClassLoader
import java.io.File
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractContext : Context {
    companion object {
        val objectMap = ConcurrentHashMap<String, ObjectBean>()
        var jarClassLoader = JarClassLoader(Thread.currentThread().contextClassLoader)
    }

    class ObjectBean(val instance: Any, val type: Type) {
        enum class Type {
            REFERENCE, BE_REFERENCED, REFERENCE_BOTH
        }
    }

    protected var classLoader: ClassLoader = Thread.currentThread().contextClassLoader
        set(value) {
            field = value
            classesRealPath = field.getResource(Constants.String.BLANK)?.path.nullToBlank()
        }

    var classesRealPath: String = File(this.classLoader.getResource(Constants.String.BLANK)?.path.nullToBlank()).absolutePath
        set(value) {
            if (value.isNotBlank()) {
                field = File(value).absolutePath
            } else {
                error("classesRealPath can not be blank.")
            }
        }

    var projectRealPath: String = Constants.String.BLANK

    /**
     * find bean
     *
     * @param id
     * @return T
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> findBean(id: String): T? {
        return objectMap[id]?.instance as T?
    }

    protected fun fixParameters(parameters: String): String {
        return parameters.replaceAllSpace().replaceAllLines()
    }
}
