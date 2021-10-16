package com.oneliang.ktx.frame.mail

import com.oneliang.ktx.Constants

class SendMailMessage {
    var fromAddress = Constants.String.BLANK
    var toAddressList = emptyList<ToAddress>()
    var separateToAddress = false
    var subject = Constants.String.BLANK
    var content: String = Constants.String.BLANK
    var accessoryPathList = emptyList<String>()
}