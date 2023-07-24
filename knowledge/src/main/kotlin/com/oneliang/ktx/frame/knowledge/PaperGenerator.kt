package com.oneliang.ktx.frame.knowledge

import com.oneliang.ktx.util.common.matches
import com.oneliang.ktx.util.common.toMap
import com.oneliang.ktx.util.generate.Template

class PaperGenerator(knowledgeFullFilenameList: List<String>) {

    private val knowledgeList = mutableListOf<Knowledge>()

    init {
        this.addKnowledgeFromFileList(knowledgeFullFilenameList)
    }

    /**
     * add knowledge from file list
     * @param knowledgeFullFilenameList
     */
    fun addKnowledgeFromFileList(knowledgeFullFilenameList: List<String>) {
        this.knowledgeList += Knowledge.getKnowledgeList(knowledgeFullFilenameList)
    }

    /**
     * add knowledge from file
     * @param knowledgeFullFilename
     */
    fun addKnowledgeFromFile(knowledgeFullFilename: String) {
        this.knowledgeList += Knowledge.getKnowledgeList(listOf(knowledgeFullFilename))
    }

    /**
     * add knowledge
     * @param knowledge
     */
    fun addKnowledge(knowledge: Knowledge) {
        this.knowledgeList += knowledge
    }

    /**
     * generate
     * @param questionFullFilename
     */
    fun generate(questionFullFilename: String): Paper {
        val paper = Paper()
        val paperConfig = PaperConfig.getPaperConfig(questionFullFilename)

        val questionList = paperConfig.questions

        val knowledgeMap = this.knowledgeList.toMap { it.key to it }

        for (question in questionList) {
            val knowledge = knowledgeMap[question.key]
            if (knowledge == null) {
                println("question key:%s not exists in knowledge, please check the knowledge".format(question.key))
                continue
            }

            val knowledgeKey = knowledge.key
            val template = knowledge.template
            val parameters = knowledge.parameters
            val config = question.config
            val operator = question.operator
            val repeat = question.repeat

            if (!parameters.matches(config.keys.toTypedArray())) {
                error("knowledge parameter can not match question config, knowledge key:%s".format(knowledgeKey))
            }

            for (i in 1..repeat) {
                val parameterMap = config.toMap(mutableMapOf()) { key, value ->
                    if (value.isEmpty()) {
                        error("config value is empty, knowledge key:%s, config key:%s".format(knowledgeKey, key))
                    }
                    val parameterValue = Configer.getValueByConfigType(value, paperConfig.global)

                    key to parameterValue
                }

                operator.forEach { (key, valueArray) ->
                    if (valueArray.isEmpty()) {
                        error("operator value is empty, knowledge key:%s, config key:%s".format(knowledgeKey, key))
                    }
                    valueArray.forEach { value ->
                        parameterMap[key] = Operator.updateParameterValueByOperator(key, value, parameterMap)
                    }
                }

                val content = Template.generate(template, Template.Option().apply {
                    this.instance = parameterMap
                })

                paper.questionList += content

            }
        }
        return paper
    }
}