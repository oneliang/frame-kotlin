package com.oneliang.ktx.frame.mail

import com.oneliang.ktx.Constants
import java.util.concurrent.CopyOnWriteArrayList

class SendMailInformation {
    var host = Constants.String.BLANK
    var port = 25
    var user = Constants.String.BLANK
    var password = Constants.String.BLANK
    var ssl = false
    var protocol = Mail.SMTP
    var fromAddress = Constants.String.BLANK
    var toAddressList = emptyList<ToAddress>()
    var subject = Constants.String.BLANK
    var content: String = Constants.String.BLANK
    var accessoryPathList = emptyList<String>()
}