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
import kotlin.reflect.KClass

object AnnotationContextUtil {

    private val logger = LoggerManager.getLogger(AnnotationContextUtil::class)

    private const val PARAMETER_TYPE = "-T="
    private const val PARAMETER_PACKAGE = "-P="
    private const val PARAMETER_PATH = "-PATH="

    private object Type {
        const val TXT = Constants.File.TXT
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
                    parameter.startsWith(PARAMETER_TYPE) -> type = parameter.replaceFirst(PARAMETER_TYPE, Constants.String.BLANK).trim()
                    parameter.startsWith(PARAMETER_PACKAGE) -> packageName = parameter.replaceFirst(PARAMETER_PACKAGE, Constants.String.BLANK).trim()
                    parameter.startsWith(PARAMETER_PATH) -> path = parameter.replaceFirst(PARAMETER_PATH, Constants.String.BLANK).trim()
                    else -> logger.error("Maybe parameter error, parameter:%s", parameter)
                }
            }
            val filePathList = path.split(Constants.Symbol.COLON)
            val searchClassList = mutableListOf<KClass<*>>()
            when {
                type.equals(Type.JAR, ignoreCase = true) -> {
                    filePathList.forEach {
                        val filePath = it.trim()
                        val jarFile = File(fixedClassesRealPath, filePath)
                        val jarFileRealPath = jarFile.absolutePath
                        if (!jarFile.exists()) {
                            logger.error("jar file do not exists, real path:%s", jarFile.absolutePath)
                            return@forEach
                        }
                        logger.debug("search jar file real path:%s", jarFile.absolutePath)
                        searchClassList.addAll(JarUtil.searchClassList(jarClassLoader, jarFileRealPath, packageName, annotationClass))
                    }
                }
                type.equals(Type.CLASSES_DIRECTORY, ignoreCase = true) -> {
                    val packageToPath = packageName.replace(Constants.Symbol.DOT, Constants.Symbol.SLASH_LEFT)
                    filePathList.forEach {
                        val filePath = it.trim()
                        val classesDirectoryRealPathFile = File(fixedClassesRealPath, filePath)
                        val classesDirectoryRealPath = classesDirectoryRealPathFile.absolutePath
                        val searchClassPathFile = File(classesDirectoryRealPath, Constants.Symbol.SLASH_LEFT + packageToPath)
                        val searchClassPath = searchClassPathFile.absolutePath
                        if (!classesDirectoryRealPathFile.exists()) {
                            logger.error("classes directory real path do not exists, real path:%s", classesDirectoryRealPathFile.absolutePath)
                            return@forEach
                        }
                        if (!searchClassPathFile.exists()) {
                            logger.error("search class path do not exists, real path:%s", searchClassPathFile.absolutePath)
                            return@forEach
                        }
                        logger.debug("classes directory real path:%s, search class path:%s", classesDirectoryRealPath, searchClassPath)
                        searchClassList.addAll(searchClassList(classesDirectoryRealPath, searchClassPath, annotationClass))
                    }
                }
                type.equals(Type.TXT, ignoreCase = true) -> {
                    val txtFile = File(fixedClassesRealPath, path)
                    if (!txtFile.exists()) {
                        logger.error("txt file do not exists, real path:%s", txtFile.absolutePath)
                        return emptyList()
                    }
                    logger.debug("search txt file real path:%s", txtFile.absolutePath)
                    val classNameList = txtFile.readLines()
                    classNameList.forEach { className ->
                        val fixClassName = className.trim()
                        if (fixClassName.isBlank() || !fixClassName.startsWith(packageName)) {
                            return@forEach//continue
                        }
                        val clazz = Thread.currentThread().contextClassLoader.loadClass(fixClassName)
                        if (clazz.isAnnotationPresent(annotationClass.java)) {
                            searchClassList.add(clazz.kotlin)
                        }
                    }
                }
                else -> {
                    logger.debug("do not support type:", type)
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

    fun findMatchAnnotationClassList(directoryList: List<String>, fileSuffixArray: Array<String>, annotationClassNameArray: Array<String>): Map<String, List<String>> {
        val annotationClassNameMap = mutableMapOf<String, MutableList<String>>()
        directoryList.forEach {
            val subAnnotationClassNameMap = findMatchAnnotationClassList(it, fileSuffixArray, annotationClassNameArray)
            subAnnotationClassNameMap.forEach { (key, list) ->
                val annotationClassNameList = annotationClassNameMap.getOrPut(key) { mutableListOf() }
                annotationClassNameList += list
            }
        }
        return annotationClassNameMap
    }

    fun findMatchAnnotationClassList(directory: String, fileSuffixArray: Array<String>, annotationClassNameArray: Array<String>): Map<String, List<String>> {
        val matchOption = FileUtil.MatchOption()
        val annotationClassNameMap = mutableMapOf<String, MutableList<String>>()
        File(directory).findMatchFile(matchOption) {
            val fullFilename = it.absolutePath
            var currentFileSuffix = Constants.String.BLANK
            fileSuffixArray.forEach { fileSuffix ->
                if (!fullFilename.endsWith(fileSuffix)) {
                    return@findMatchFile it.absolutePath
                }
                currentFileSuffix = fileSuffix
            }
            var packageName = Constants.String.BLANK
            var annotationClassName = Constants.String.BLANK
            val lines = it.readLines()
            for (line in lines) {
                val trimLine = line.trim()
                if (trimLine.indexOf("package ") == 0) {
                    packageName = trimLine
                    packageName = packageName.replace("package ", Constants.String.BLANK).replace(Constants.Symbol.SEMICOLON, Constants.String.BLANK)
                } else if (trimLine.startsWith("@")) {
                    run loop@{
                        annotationClassNameArray.forEach { name ->
                            if (trimLine.startsWith(name)) {
                                annotationClassName = name
                                return@loop//break
                            }
                        }
                    }
                    if (annotationClassName.isNotBlank()) {
                        val className = packageName + Constants.Symbol.DOT + it.name.replace(currentFileSuffix, Constants.String.BLANK)
                        val classNameList = annotationClassNameMap.getOrPut(annotationClassName) { mutableListOf() }
                        classNameList += className
                    }
                    break
                }
            }
            it.absolutePath
        }
        return annotationClassNameMap
    }
}
