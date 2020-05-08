package com.oneliang.ktx.frame.context

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.FileLoadException
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.replaceAllLines
import com.oneliang.ktx.util.common.replaceAllSpace
import com.oneliang.ktx.util.file.FileUtil
import com.oneliang.ktx.util.file.findMatchFile
import com.oneliang.ktx.util.jar.JarClassLoader
import com.oneliang.ktx.util.jar.JarUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KClass

object AnnotationContextUtil {

    private val logger = LoggerManager.getLogger(AnnotationContextUtil::class)

    private const val PARAMETER_TYPE = "-T="
    private const val PARAMETER_PACKAGE = "-P="
    private const val PARAMETER_PATH = "-PATH="

    private object Type {
        const val JAR = Constants.File.JAR
        const val CLASSES_DIRECTORY = "classes_directory"
    }

    private val classCacheMap = ConcurrentHashMap<String, List<KClass<*>>>()

    /**
     * Method:use for AnnotationActionContext,AnnotationIocContext,AnnotationInterceptorContext,AnnotationMappingContext
     * @param parameters
     * @param classLoader
     * @param classesRealPath
     * @param jarClassLoader
     * @param annotationClass
     * @return List<Class></Class>>
     * @throws ClassNotFoundException
     * @throws FileLoadException
     */
    @Throws(ClassNotFoundException::class, FileLoadException::class)
    fun parseAnnotationContextParameterAndSearchClass(parameters: String, classLoader: ClassLoader, classesRealPath: String, jarClassLoader: JarClassLoader, annotationClass: KClass<out Annotation>): List<KClass<*>> {
        val parameterArray = parameters.replaceAllSpace().replaceAllLines().split(Constants.Symbol.COMMA)
        val fixedClassesRealPath = if (classesRealPath.isBlank()) {
            classLoader.getResource(Constants.String.BLANK)?.path.nullToBlank()
        } else {
            classesRealPath
        }
        return if (parameterArray.size == 1) {
            val path = File(fixedClassesRealPath, parameters).absolutePath
            logger.debug("search class path:$path")
            searchClassList(fixedClassesRealPath, path, annotationClass)
        } else {
            var type: String = Constants.String.BLANK
            var packageName: String = Constants.String.BLANK
            var path: String = Constants.String.BLANK
            parameterArray.forEach {
                val parameter = it.trim()
                when {
                    parameter.startsWith(PARAMETER_TYPE) -> type = parameter.replaceFirst(PARAMETER_TYPE, Constants.String.BLANK)
                    parameter.startsWith(PARAMETER_PACKAGE) -> packageName = parameter.replaceFirst(PARAMETER_PACKAGE, Constants.String.BLANK)
                    parameter.startsWith(PARAMETER_PATH) -> path = parameter.replaceFirst(PARAMETER_PATH, Constants.String.BLANK)
                    else -> logger.error("Maybe parameter error, parameter:%s", parameter)
                }
            }
            val filePathList = path.split(Constants.Symbol.COLON)
            val searchClassList = mutableListOf<KClass<*>>()
            if (type.equals(Type.JAR, ignoreCase = true)) {
                filePathList.forEach {
                    val filePath = it.trim()
                    val jarFileRealPath = File(fixedClassesRealPath, filePath).absolutePath
                    logger.debug("search jar file real path:$jarFileRealPath")
                    searchClassList.addAll(JarUtil.searchClassList(jarClassLoader, jarFileRealPath, packageName, annotationClass))
                }
            } else if (type.equals(Type.CLASSES_DIRECTORY, ignoreCase = true)) {
                val packageToPath = packageName.replace(Constants.Symbol.DOT, Constants.Symbol.SLASH_LEFT)
                filePathList.forEach {
                    val filePath = it.trim()
                    val otherClassesRealPathFile = File(fixedClassesRealPath, filePath)
                    val otherClassesRealPath = otherClassesRealPathFile.absolutePath
                    val searchClassPathFile = File(otherClassesRealPath, Constants.Symbol.SLASH_LEFT + packageToPath)
                    val searchClassPath = searchClassPathFile.absolutePath
                    logger.debug("search classes real path:%s, exists:%s, search class path:%s, exists:%s", otherClassesRealPath, otherClassesRealPathFile.exists(), searchClassPath, searchClassPathFile.exists())
                    searchClassList.addAll(searchClassList(otherClassesRealPath, searchClassPath, annotationClass))
                }
            }
            searchClassList
        }
    }

    /**
     * search all class list
     * @param classesRealPath
     * @param searchClassPath
     * @return List<Class></Class>>
     * @throws ClassNotFoundException
     */
    @Throws(ClassNotFoundException::class)
    private fun searchAllClassList(classesRealPath: String, searchClassPath: String): List<KClass<*>> {
        val classCacheKey = generateClassCacheKey(classesRealPath, searchClassPath)
        if (this.classCacheMap.containsKey(classCacheKey)) {
            val classList = classCacheMap[classCacheKey]
            if (classList != null) {
                return classList
            }
        }
        val classList = mutableListOf<KClass<*>>()
        val classesRealPathFile = File(classesRealPath)
        val searchClassPathFile = File(searchClassPath)
        searchClassPathFile.findMatchFile(FileUtil.MatchOption().also {
            it.fileSuffix = Constants.Symbol.DOT + Constants.File.CLASS
        }) {
            val filePath = it.absolutePath
            val className = filePath.substring(classesRealPathFile.absolutePath.length + 1, filePath.length - (Constants.Symbol.DOT + Constants.File.CLASS).length).replace(File.separator, Constants.Symbol.DOT)
            val clazz = Thread.currentThread().contextClassLoader.loadClass(className)
            classList.add(clazz.kotlin)
            filePath
        }
        this.classCacheMap[classCacheKey] = classList
        return classList
    }

    /**
     * search class list
     * @param classesRealPath
     * @param searchClassPath
     * @param annotationClass
     * @return List<Class></Class>>
     * @throws ClassNotFoundException
     */
    @Throws(ClassNotFoundException::class)
    fun searchClassList(classesRealPath: String, searchClassPath: String, annotationClass: KClass<out Annotation>): List<KClass<*>> {
        val kClassList = mutableListOf<KClass<*>>()
        val allKClassList = searchAllClassList(classesRealPath, searchClassPath)
        for (kClass in allKClassList) {
            if (kClass.java.isAnnotationPresent(annotationClass.java)) {
                kClassList.add(kClass)
            }
        }
        return kClassList
    }

    private fun generateClassCacheKey(classesRealPath: String, searchClassPath: String): String {
        return classesRealPath + Constants.Symbol.COMMA + searchClassPath
    }
}
