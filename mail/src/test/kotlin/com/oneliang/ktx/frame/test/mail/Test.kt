package com.oneliang.ktx.frame.test.mail

import com.oneliang.ktx.frame.mail.Mail
import com.oneliang.ktx.frame.mail.ReceiveMailInformation
import com.oneliang.ktx.frame.mail.SendMailMessage
import com.oneliang.ktx.frame.mail.ToAddress
import com.oneliang.ktx.util.file.FileUtil

fun main() {
    val sendMailConfiguration = Mail.SendMailConfiguration()
    sendMailConfiguration.host = "smtp.exmail.qq.com"
    sendMailConfiguration.port = 465
    sendMailConfiguration.user = "server@findsteel.cn"
    sendMailConfiguration.password = "********"
    sendMailConfiguration.ssl = true
    val sendMailMessage = SendMailMessage()
    sendMailMessage.subject = "test"
    sendMailMessage.content = "test"
    sendMailMessage.fromAddress = "server@findsteel.cn"
    sendMailMessage.toAddressList = listOf(ToAddress("********@qq.com"))
    sendMailMessage.accessoryPathList = listOf("C:\\Users\\Administrator\\Desktop\\微钢云最新库存通知_20210301.xlsx")
    Mail.send(sendMailConfiguration, listOf(sendMailMessage))
    return
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