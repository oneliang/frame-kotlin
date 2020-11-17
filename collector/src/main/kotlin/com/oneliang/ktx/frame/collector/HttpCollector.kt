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
                .replace(Constants.String.CR_STRING, Constants.String.BLANK)
                .replace(Constants.String.LF_STRING, Constants.String.BLANK)
                .replace(Constants.String.TAB_STRING, Constants.String.BLANK)
                .replace(Constants.String.CRLF_STRING, Constants.String.BLANK)
                .replace("&nbsp;", Constants.String.BLANK)
    }
}