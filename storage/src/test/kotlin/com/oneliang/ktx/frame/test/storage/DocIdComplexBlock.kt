package com.oneliang.ktx.frame.test.storage

import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.common.toLong
import com.oneliang.ktx.util.common.toShort
import com.oneliang.ktx.util.section.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

class DocIdComplexBlock : ComplexBlock() {
    enum class Position(val value: Int) {
        DOC_ID(0), SEGMENT_NO(1), CONTENT_BEGIN(2), CONTENT_END(3)
    }

    override val parseBlockWrapperQueue: Queue<BlockWrapper>
        get() {
            val linkedList = LinkedList<BlockWrapper>()
            linkedList += BlockWrapper(Position.DOC_ID.value, UnitBlock(initialSize = 4))
            linkedList += BlockWrapper(Position.DOC_ID.value, UnitBlock(initialSize = 2))
            linkedList += BlockWrapper(Position.DOC_ID.value, UnitBlock(initialSize = 8))
            linkedList += BlockWrapper(Position.DOC_ID.value, UnitBlock(initialSize = 8))
            return linkedList
        }

    override fun afterRead(currentIndex: Int, currentId: Int, currentBlock: Block) {
        super.afterRead(currentIndex, currentId, currentBlock)
        println("currentIndex:%s, currentId:%s".format(currentIndex, currentId))
    }
}

class DocIdLoopBlock : LoopBlock() {
    enum class Position(val value: Int) {
        DOC_ID(0), SEGMENT_NO(1), CONTENT_BEGIN(2), CONTENT_END(3)
    }

    class DocId(docId: Long, segmentNo: Short, contentBegin: Long, contentEnd: Long)

    override val parseBlockWrapperList: List<BlockWrapper>
        get() {
            val linkedList = mutableListOf<BlockWrapper>()
            linkedList += BlockWrapper(Position.DOC_ID.value, UnitBlock(initialSize = 8))
            linkedList += BlockWrapper(Position.SEGMENT_NO.value, UnitBlock(initialSize = 2))
            linkedList += BlockWrapper(Position.CONTENT_BEGIN.value, UnitBlock(initialSize = 8))
            linkedList += BlockWrapper(Position.CONTENT_END.value, UnitBlock(initialSize = 8))
            return linkedList
        }

    override fun afterRead(currentIndex: Int, currentId: Int, currentBlock: Block) {
        super.afterRead(currentIndex, currentId, currentBlock)
        println("currentIndex:%s, currentId:%s".format(currentIndex, currentId))
        when (currentId) {
            Position.DOC_ID.value -> {
                println(currentBlock.value.toInt())
            }

            Position.SEGMENT_NO.value -> {
                println(currentBlock.value.toShort())
            }

            Position.CONTENT_BEGIN.value -> {
                println(currentBlock.value.toLong())
            }

            Position.CONTENT_END.value -> {
                println(currentBlock.value.toLong())
            }
        }
    }

    fun write(content:ByteArray){

    }
}

fun main() {
//    val docIdComplexBlock = DocIdComplexBlock()
//    val byteArrayOutputStream = ByteArrayOutputStream()
//    byteArrayOutputStream.write(1.toByteArray())
//    byteArrayOutputStream.write(0.toShort().toByteArray())
//    byteArrayOutputStream.write(0L.toByteArray())
//    byteArrayOutputStream.write(20L.toByteArray())
//    val bytes = byteArrayOutputStream.toByteArray()
//    println(bytes.size)
//    val byteArrayInputStream = ByteArrayInputStream(bytes)
//    docIdComplexBlock.parse(byteArrayInputStream)

    val docIdLoopBlock = DocIdLoopBlock()
    val byteArrayOutputStream = ByteArrayOutputStream()
    byteArrayOutputStream.write(1L.toByteArray())
    byteArrayOutputStream.write(2.toShort().toByteArray())
    byteArrayOutputStream.write(0L.toByteArray())
    byteArrayOutputStream.write(20L.toByteArray())

    byteArrayOutputStream.write(2L.toByteArray())
    byteArrayOutputStream.write(2.toShort().toByteArray())
    byteArrayOutputStream.write(20L.toByteArray())
    byteArrayOutputStream.write(40L.toByteArray())

    val bytes = byteArrayOutputStream.toByteArray()
    println(bytes.size)
    val byteArrayInputStream = ByteArrayInputStream(bytes)
    docIdLoopBlock.parse(byteArrayInputStream)
    println(docIdLoopBlock.totalSize)
}