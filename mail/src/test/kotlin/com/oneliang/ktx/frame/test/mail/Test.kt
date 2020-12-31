package com.oneliang.ktx.frame.test.mail

import com.oneliang.ktx.frame.mail.Mail
import com.oneliang.ktx.frame.mail.ReceiveMailInformation
import com.oneliang.ktx.frame.mail.SendMailInformation
import com.oneliang.ktx.frame.mail.ToAddress
import com.oneliang.ktx.util.file.FileUtil

fun main() {
    val sendMailInformation = SendMailInformation()
    sendMailInformation.host = "smtp.exmail.qq.com"
    sendMailInformation.port = 465
    sendMailInformation.user = "server@findsteel.cn"
    sendMailInformation.password = "owabHiQvxCCk4yDg"
    sendMailInformation.ssl = true
    sendMailInformation.subject = "test"
    sendMailInformation.content = "test"
    sendMailInformation.fromAddress = "server@findsteel.cn"
    sendMailInformation.toAddressList = listOf(ToAddress(address = "582199098@qq.com"))
    Mail.send(sendMailInformation)
    //receive
    val receiveMailInformation = ReceiveMailInformation()
    receiveMailInformation.host = "imap.exmail.qq.com"
    receiveMailInformation.port = 993
    receiveMailInformation.user = "server@findsteel.cn"
    receiveMailInformation.password = "owabHiQvxCCk4yDg"
    receiveMailInformation.ssl = true
    receiveMailInformation.protocol = Mail.IMAP
    val mailMessageList = Mail.receive(receiveMailInformation)
    val path = "/C:/temp";
    FileUtil.createDirectory(path);
    for (mailMessage in mailMessageList) {
        println(mailMessage.fromAddress);
        mailMessage.saveAccessories(path);
    }
}