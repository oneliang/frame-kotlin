package com.oneliang.ktx.frame.mail

import com.oneliang.ktx.Constants

class ReceiveMailInformation {
    var host = Constants.String.BLANK
    var port = 110
    var user = Constants.String.BLANK
    var password = Constants.String.BLANK
    var ssl = false
    var protocol= Mail.POP3
}