package com.oneliang.ktx.frame.test.knowledge

import com.oneliang.ktx.frame.knowledge.Paper
import com.oneliang.ktx.frame.knowledge.PaperGenerator
import com.oneliang.ktx.util.generate.Template

fun main() {
    val knowledgeFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/knowledge/src/test/resources/grade_1/knowledge_point.json"
    val unitConvertFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/knowledge/src/test/resources/grade_1/unit_convert_point.json"
    val timeFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/knowledge/src/test/resources/grade_1/time_point.json"
    val knowledgeFullFilenameList = listOf(
        knowledgeFullFilename,
        unitConvertFullFilename,
        timeFullFilename
    )

    println("----------测试----------")
    val questionFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/knowledge/src/test/resources/paper/question.json"
    val paperGenerator = PaperGenerator(knowledgeFullFilenameList)
    var paper = paperGenerator.generate(questionFullFilename)
    printPager(paper)

    println("----------纯乘法----------")
    val multiplyQuestionFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/knowledge/src/test/resources/paper/multiply_paper.json"
    paper = paperGenerator.generate(multiplyQuestionFullFilename)
    printPager(paper)
    outputTxtPaper("multiply",paper)

    println("----------纯时间----------")
    val timeQuestionFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/knowledge/src/test/resources/paper/time_paper.json"
    paper = paperGenerator.generate(timeQuestionFullFilename)
    printPager(paper)
    outputTxtPaper("time",paper)

}

private fun printPager(paper: Paper) {
    paper.questionList.forEachIndexed { index, content ->
        print("题目 ${index + 1}:\t")
        println(content)
    }
}

private fun outputHtmlPaper(outputName: String, paper: Paper) {
    val htmlTemplateFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/knowledge/src/test/resources/template.html"
    val toFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/knowledge/src/test/resources/$outputName.html"
    Template.generate(htmlTemplateFullFilename, toFullFilename, Template.Option().apply {
        this.instance = paper
    })
}

private fun outputTxtPaper(outputName: String, paper: Paper) {
    val htmlTemplateFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/knowledge/src/test/resources/template.txt"
    val toFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/knowledge/src/test/resources/$outputName.txt"
    Template.generate(htmlTemplateFullFilename, toFullFilename, Template.Option().apply {
        this.instance = paper
    })
}