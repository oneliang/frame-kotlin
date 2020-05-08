package com.oneliang.ktx.frame.collector

import com.oneliang.ktx.Constants

abstract class HttpCollector<T> : Collector<String, T> {
    /**
     * replace all blank
     *
     * @param text
     * @return String
     */
    protected fun replaceAllBlank(text: String): String {
        return text.replace(Constants.String.SPACE, Constants.String.BLANK)
                .replace(Constants.String.CR.toString(), Constants.String.BLANK)
                .replace(Constants.String.LF.toString(), Constants.String.BLANK)
                .replace("\t", Constants.String.BLANK)
                .replace(Constants.String.CRLF_STRING, Constants.String.BLANK)
                .replace("&nbsp;", Constants.String.BLANK)
    }
}