package com.oneliang.ktx.frame.knowledge

import com.oneliang.ktx.util.file.readContentIgnoreLine
import com.oneliang.ktx.util.json.DefaultJsonKotlinClassProcessor
import com.oneliang.ktx.util.json.jsonToObject
import java.io.File

class PaperConfig {
    var global: Map<String, Array<String>> = emptyMap()
    var questions: Array<Question> = emptyArray()

    companion object {
        private const val KEY_GLOBAL = "global"

        fun getPaperConfig(questionFullFilename: String): PaperConfig {
            val json = File(questionFullFilename).readContentIgnoreLine()
            return json.jsonToObject(
                PaperConfig::class, fieldNameKClassMapping = mapOf(
                    KEY_GLOBAL to (DefaultJsonKotlinClassProcessor.Type.STRING_ARRAY_MAP to Any::class),
                    Question.KEY_CONFIG to (DefaultJsonKotlinClassProcessor.Type.STRING_MAP to Any::class),
                    Question.KEY_OPERATOR to (DefaultJsonKotlinClassProcessor.Type.STRING_ARRAY_MAP to Any::class)
                )
            )
        }
    }
}