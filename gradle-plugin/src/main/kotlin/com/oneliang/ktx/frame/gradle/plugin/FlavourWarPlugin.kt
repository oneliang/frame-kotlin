package com.oneliang.ktx.frame.gradle.plugin


import com.oneliang.ktx.util.file.ZipUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.zip.ZipEntry

class FlavourWarPlugin : Plugin<Project> {
    companion object {
        private const val EXTENSION_NAME = "flavourWar"
        private const val FLAVOUR_WAR_TASK_NAME = "%sFlavourWar"
        private const val FLAVOUR_GENERATE_TASK_NAME = "%sFlavourGenerate"
    }


    override fun apply(project: Project) {
        val flavourWarContainer = project.container(FlavourWar::class.java) { name ->
            project.objects.newInstance(FlavourWar::class.java, name)
        }
        project.extensions.add(EXTENSION_NAME, flavourWarContainer)
        project.afterEvaluate {
            if (flavourWarContainer.isEmpty()) {
                return@afterEvaluate
            }
            flavourWarContainer.forEach { flavourWar ->
                val inputWarFullFilename = flavourWar.from
                val outputWarFullFilename = flavourWar.to
                val replaceItemMap = flavourWar.replaceItems
                val dependsOnTaskName = flavourWar.dependsOnTaskName
                val deleteFrom = flavourWar.deleteFrom
                val flavourGenerateTask = project.tasks.create(String.format(FLAVOUR_GENERATE_TASK_NAME, flavourWar.name))
                val flavourWarTask = project.tasks.create(String.format(FLAVOUR_WAR_TASK_NAME, flavourWar.name))
                flavourGenerateTask.doLast {
                    flavourWar.generate.invoke()
                }
                flavourWarTask.doLast {
                    val zipEntryPathList = mutableListOf<ZipUtil.ZipEntryPath>()
                    replaceItemMap.forEach { (key, value) ->
                        println("$key->$value")
                        val zipEntry = ZipEntry(key)
                        zipEntryPathList += ZipUtil.ZipEntryPath(value, zipEntry, true)
                    }
                    ZipUtil.zip(outputWarFullFilename, inputWarFullFilename, zipEntryPathList, null)
                    if (deleteFrom) {
                        File(inputWarFullFilename).delete()
                    }
                }
                flavourWarTask.dependsOn(dependsOnTaskName)
            }
        }
    }
}