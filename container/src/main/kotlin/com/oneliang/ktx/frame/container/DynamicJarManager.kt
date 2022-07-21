package com.oneliang.ktx.frame.container

import com.oneliang.ktx.util.common.toFileProtocolURL
import com.oneliang.ktx.util.jar.JarClassLoader

object DynamicJarManager {

    fun loadJar(jarFullFilename: String): ClassLoader {
        return JarClassLoader(Thread.currentThread().contextClassLoader).also {
            it.addURL(jarFullFilename.toFileProtocolURL())
        }
    }
}
