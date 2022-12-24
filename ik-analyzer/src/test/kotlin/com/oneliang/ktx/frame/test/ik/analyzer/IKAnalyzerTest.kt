/**
 * IK 中文分词  版本 5.0.1
 * IK Analyzer release 5.0.1
 *
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * 源代码由林良益(linliangyi2005@gmail.com)提供
 * 版权声明 2012，乌龙茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 */
package com.oneliang.ktx.frame.test.ik.analyzer

import com.oneliang.ktx.frame.ik.analyzer.core.IKSegmenter
import com.oneliang.ktx.frame.ik.analyzer.core.Lexeme
import java.io.IOException
import java.io.StringReader

fun main(args: Array<String>) {
    val content = "求购201宏旺JIM1 0.5卷一个"
    // 智能切分
    segText(content, true)
    // 细粒度划分
    segText(content, false)
}

private fun segText(text: String, useSmart: Boolean) {
    val ik = IKSegmenter(StringReader(text), useSmart)
    try {
        var word: Lexeme? = null
        println("$text:")
        while (ik.next().also { word = it } != null) {
            println(word!!.lexemeText + " " + word!!.lexemeTypeString)
        }
    } catch (ex: IOException) {
        throw RuntimeException(ex)
    }
}