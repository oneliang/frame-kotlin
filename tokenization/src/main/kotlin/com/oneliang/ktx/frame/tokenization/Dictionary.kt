package com.oneliang.ktx.frame.tokenization

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.concurrent.atomic.AtomicTreeSet
import com.oneliang.ktx.util.file.readContentEachLine
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.FileNotFoundException
import java.util.concurrent.ConcurrentHashMap

class Dictionary(private val categorizationStrategy: DefaultCategorizationStrategy = DefaultCategorizationStrategy()) {
    companion object {
        private val logger = LoggerManager.getLogger(Dictionary::class)
    }

    private val map = ConcurrentHashMap<String, String>()
    private val wordLengthSet = AtomicTreeSet<Int> { o1, o2 ->
        if (o1 == o2) {
            0
        } else if (o1 > o2) {
            -1
        } else {
            1
        }
    }

    /**
     * load dictionary
     * @param fullFilename
     */
    fun load(fullFilename: String) {
        val file = fullFilename.toFile()
        if (file.exists()) {
            file.readContentEachLine { line ->
                if (line.isBlank()) {
                    return@readContentEachLine true
                } else {
                    this.addKeywordToDictionary(line)
                }
                true
            }
        } else {
            throw FileNotFoundException("file path:%s not found".format(fullFilename))
        }
    }

    /**
     * add keyword set dictionary
     * @param keywordSet
     */
    fun addKeywordSetToDictionary(keywordSet: Set<String>) {
        for (keyword in keywordSet) {
            this.addKeywordToDictionary(keyword)
        }
    }

    /**
     * add keyword to dictionary
     * @param keyword
     */
    private fun addKeywordToDictionary(keyword: String) {
        val data = keyword.trim()
        this.map[data] = data
        this.wordLengthSet += data.length
    }

    /**
     * split words
     * @param content
     * @param wordLengthList
     * @param wordLengthStartIndex
     * @param realBeginIndex
     * @param wordCollector
     */
    private fun splitWords(content: String, wordLengthList: List<Int>, wordLengthStartIndex: Int, realBeginIndex: Int, wordCollector: WordCollector) {
        var wordLengthIndex = wordLengthStartIndex
        if (wordLengthIndex > wordLengthList.lastIndex) {
            return
        }
        if (content.length <= 1) {
            if (content.isNotEmpty()) {
                wordCollector.addWord(Word(content, realBeginIndex, realBeginIndex + content.length))
            } else {
                //will not empty, because decrease one by one
            }
            return
        }
        val wordLengthListSize = wordLengthList.size
        var currentContent = content
        while (wordLengthIndex < wordLengthListSize) {
            val wordLength = wordLengthList[wordLengthIndex]
            if (currentContent.length < wordLength) {
                wordLengthIndex++//next index for word length
                continue
            }
            var beginIndex = currentContent.length - wordLength
            var endIndex = currentContent.length
            var notFound = true
            while (beginIndex >= 0) {
                val word = currentContent.substring(beginIndex, endIndex)
                if (this.map.containsKey(word)) {
                    notFound = false
//                    println("$word[${realBeginIndex + beginIndex},${realBeginIndex + endIndex})")
                    wordCollector.addWord(Word(word, realBeginIndex + beginIndex, realBeginIndex + endIndex))
                    val leftWord = currentContent.substring(0, beginIndex)//not match
                    val rightWord = currentContent.substring(endIndex, currentContent.length)//not match
                    if (rightWord.isNotEmpty()) {
                        splitWords(rightWord, wordLengthList, wordLengthIndex + 1, realBeginIndex + endIndex, wordCollector)
                    }

                    currentContent = leftWord
                    //current content has be changed, reset beginIndex and endIndex
                    beginIndex = currentContent.length - wordLength
                    endIndex = currentContent.length
                } else {//check next
                    beginIndex -= 1
                    endIndex -= 1
                }
            }
            //split word when not found and last check
            if (notFound && wordLengthIndex == wordLengthListSize - 1) {
                wordCollector.addWord(Word(currentContent, realBeginIndex, realBeginIndex + currentContent.length))
//                splitWords(currentContent, wordLengthList, index + 1, realBeginIndex, recordList)
            }
            wordLengthIndex++
        }
    }

    private fun splitWords(content: String, beginOffset: Int, wordCollector: WordCollector): List<Word> {
//        val wordList = mutableListOf<Word>()
//        val wordCollector = WordCollector()
        val wordLengthList = this.wordLengthSet.toList()
        splitWords(content, wordLengthList, 0, beginOffset, wordCollector)
        if (wordCollector.wordList.isEmpty()) {
            val endIndex = beginOffset + content.length
            wordCollector.addWord(Word(content, beginOffset, endIndex))
        }
        return wordCollector.wordList
    }

    fun splitWords(content: String): WordCollector {
        val charArray = content.toCharArray()
        var beginIndex = 0
        var endIndex = beginIndex + 1
        val lastIndex = content.length - 1
//        val wordList = mutableListOf<Word>()
        val wordCollector = WordCollector()
        //[beginIndex,endIndex) left close right open
        while (endIndex <= lastIndex) {
            val currentChar = charArray[endIndex - 1]
            val nextChar = charArray[endIndex]
            var currentWord = Constants.String.BLANK//charArray.copyOfRange(beginIndex, endIndex).concatToString()

            //if same character type, update endIndex, need to concat it
            if (this.categorizationStrategy.checkTheSameCategory(currentChar, nextChar)) {
                endIndex += 1
                if (endIndex > lastIndex) {//endIndex word is the last word, the character type of endIndex word is the same with current word, endIndex+1 overflow lastIndex, null word
                    currentWord = charArray.copyOfRange(beginIndex, endIndex).concatToString()
                    logger.debug("last word:[%s], need to split, beginIndex:%s", currentWord, beginIndex)
                    this.splitWords(currentWord, beginIndex, wordCollector)
                }
            } else {//if not the same character type,update beginIndex and endIndex, separate it, stop word, endIndex is the next beginIndex
                currentWord = charArray.copyOfRange(beginIndex, endIndex).concatToString()
                logger.debug("after separate word:[%s], need to split, beginIndex:%s", currentWord, beginIndex)
                this.splitWords(currentWord, beginIndex, wordCollector)
                //update index
                beginIndex = endIndex
                endIndex = beginIndex + 1
            }
        }
        wordCollector.wordList.sortBy { it.beginIndex }
        return wordCollector
    }

    class Word(val value: String, val beginIndex: Int, val endIndex: Int)

    class WordCollector {
        val wordList = mutableListOf<Word>()
        val wordMap = mutableMapOf<String, MutableList<Word>>()
        fun addWord(word: Word) {
            this.wordList += word
            val wordValueWordList = this.wordMap.getOrPut(word.value) { mutableListOf() }
            wordValueWordList += word
        }
    }

    interface CategorizationStrategy {
        /**
         * check the same category
         * @param currentChar
         * @param nextChar
         * @return Boolean
         */
        fun checkTheSameCategory(currentChar: Char, nextChar: Char): Boolean
    }
}