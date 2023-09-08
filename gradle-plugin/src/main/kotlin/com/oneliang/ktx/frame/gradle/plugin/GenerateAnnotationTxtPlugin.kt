package com.oneliang.ktx.frame.gradle.plugin

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.context.AnnotationContextUtil
import com.oneliang.ktx.gradle.generateJarName
import com.oneliang.ktx.util.common.toBooleanSafely
import com.oneliang.ktx.util.file.createFileIncludeDirectory
import com.oneliang.ktx.util.file.write
import com.oneliang.ktx.util.json.toJson
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

class GenerateAnnotationTxtPlugin : Plugin<Project> {
    companion object {
        private const val EXTENSION_NAME = "annotationTxt"
        private const val GENERATE_ANNOTATION_TXT_TASK_NAME = "generateAnnotationTxt"
        private fun isWindows(): Boolean {
            return System.getProperty("os.name").toLowerCase().contains("windows")
        }

        private fun getAllProject(project: Project, showProjectDependencies: Boolean): Set<Project> {
            val projectNameHashSet = HashSet<String>()
            return getAllProject(project, 0, projectNameHashSet, showProjectDependencies)
        }

        private fun getAllProject(project: Project, depth: Int, projectNameHashSet: MutableSet<String>, showProjectDependencies: Boolean): Set<Project> {
            val projectSet = HashSet<Project>()
            val projectName = project.generateJarName(true)
//            if (projectNameHashSet.contains(projectName)) {
//                return projectSet
//            }
            projectNameHashSet += projectName

            val dependencyProjectSet = HashSet<Project>()
            project.configurations.forEach { configuration ->
                val dependencySet = configuration.allDependencies
                dependencySet.forEach { dependency ->
                    if (dependency is ProjectDependency) {
                        dependencyProjectSet += dependency.dependencyProject
                    }
                }
            }
            projectSet += project
            val string = StringBuilder()
            for (i in 0 until depth) {
                string.append("|\t")
            }
            val nextDepth = depth + 1
            if (showProjectDependencies) {
                println("$string|project:$projectName, dependency size:${dependencyProjectSet.size}")
            }
            dependencyProjectSet.forEach {
                projectSet.addAll(getAllProject(it, nextDepth, projectNameHashSet, showProjectDependencies))
            }
            return projectSet
        }
    }


    override fun apply(project: Project) {
        project.extensions.create(EXTENSION_NAME, AnnotationTxt::class.java)
        val selfBuildForAnnotationTxt = project.rootProject.findProperty("self.build.for.annotation.txt").toString().toBooleanSafely()
        println("----------:self.build.for.annotation.txt:$selfBuildForAnnotationTxt")
        if (!selfBuildForAnnotationTxt) {
            return
        }
        project.afterEvaluate {
            try {
                project.extensions.findByName(EXTENSION_NAME)
            } catch (t: Throwable) {
                t.printStackTrace()
                return@afterEvaluate
            }
            val annotationTxt = project.extensions.findByName(EXTENSION_NAME) as AnnotationTxt
            val fileSuffixArray = annotationTxt.fileSuffixArray
            val annotationClassNameArray = annotationTxt.annotationClassNameArray
            val showProjectDependencies = annotationTxt.showProjectDependencies
            println("fileSuffixArray:${fileSuffixArray.toJson()}, annotationClassNameArray:${annotationClassNameArray.toJson()}, showProjectDependencies:${showProjectDependencies}")
            val task = project.tasks.create(GENERATE_ANNOTATION_TXT_TASK_NAME)
            task.doLast {
                val projectSet = getAllProject(project, showProjectDependencies)
                val directoryList = mutableListOf<String>()
                projectSet.forEach { oneProject ->
                    directoryList += oneProject.file("src/main/kotlin").absolutePath
                }
                val map = AnnotationContextUtil.findMatchAnnotationClassList(directoryList as List<String>, fileSuffixArray, annotationClassNameArray)
                map.forEach { (key, classNameList) ->
                    val classNameMutableList = classNameList.toMutableList()
                    //all project white list file
                    projectSet.forEach { oneProject ->
                        val whiteListFile = oneProject.file("src/main/resources/" + key + "_white_list.txt")
                        if (whiteListFile.exists()) {
                            val whiteListClassNameList = whiteListFile.readLines()
                            whiteListClassNameList.forEach { className ->
                                if (className.isNotBlank()) {
                                    classNameMutableList += className
                                }
                            }
                        }
                    }
                    val isWindows = isWindows()
                    val file = project.file("src/main/resources/$key.txt")
                    if (classNameMutableList.isNotEmpty()) {
                        classNameMutableList.sort()
                        val content = StringBuilder()
                        file.createFileIncludeDirectory()
                        classNameMutableList.forEach { line ->
                            content.append(line + Constants.String.NEW_LINE)
                        }
                        file.write(content.toString().toByteArray())
                    } else {
                        file.delete()
                    }
                }
            }
            val classesTask = project.tasks.findByName("classes")
            classesTask?.dependsOn(GENERATE_ANNOTATION_TXT_TASK_NAME)
        }
    }

}