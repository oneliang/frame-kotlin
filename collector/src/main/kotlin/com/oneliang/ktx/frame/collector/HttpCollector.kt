package com.oneliang.ktx.frame.collector

import com.oneliang.ktx.Constants

abstract class HttpCollector<FROM,DATA> : Collector<FROM, DATA> {

    /**
     * replace all blank
     *
     * @param text
     * @return String
     */
    protected fun replaceAllBlank(text: String): String {
        return text.replace(Constants.String.SPACE, Constants.String.BLANK)
                .replace(Constants.String.CR, Constants.String.BLANK)
                .replace(Constants.String.LF, Constants.String.BLANK)
                .replace(Constants.String.TAB, Constants.String.BLANK)
                .replace(Constants.String.CRLF, Constants.String.BLANK)
                .replace("&nbsp;", Constants.String.BLANK)
    }
}