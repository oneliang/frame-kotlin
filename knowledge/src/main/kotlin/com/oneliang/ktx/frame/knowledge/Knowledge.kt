package com.oneliang.ktx.frame.knowledge

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.file.readContentIgnoreLine
import com.oneliang.ktx.util.json.jsonToObjectList
import java.io.File

class Knowledge {
    var name: String = Constants.String.BLANK
    var key: String = Constants.String.BLANK
    var template: String = Constants.String.BLANK
    var parameters: Array<String> = emptyArray()

    companion object {
        fun getKnowledgeList(knowledgeFullFilenameList: List<String>): List<Knowledge> {
            val knowledgeList = mutableListOf<Knowledge>()
            for (knowledgeFullFilename in knowledgeFullFilenameList) {
                val json = File(knowledgeFullFilename).readContentIgnoreLine()
                knowledgeList += json.jsonToObjectList(Knowledge::class)
            }
            return knowledgeList
        }
    }
}






