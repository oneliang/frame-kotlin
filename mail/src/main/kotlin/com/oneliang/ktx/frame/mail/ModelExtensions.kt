package com.oneliang.ktx.frame.mail

import java.util.*
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

fun Message.addRecipient(toAddress: ToAddress) {
    val type = toAddress.type
    val address = InternetAddress(toAddress.address)
    when (type) {
        ToAddress.Type.TO.value -> this.addRecipient(Message.RecipientType.TO, address)
        ToAddress.Type.BCC.value -> this.addRecipient(Message.RecipientType.BCC, address)
        ToAddress.Type.CC.value -> this.addRecipient(Message.RecipientType.CC, address)
    }
}

fun MimeMessage.setData(from: String, toAddressList: List<ToAddress>, subject: String, content: Multipart, sentDate: Date = Date()) {
    // from address
    this.setFrom(InternetAddress(from))
    // set subject
    this.subject = subject
    // send date
    this.sentDate = sentDate
    // set content
    this.setContent(content)
    for (toAddress in toAddressList) {
        this.addRecipient(toAddress)
    }
}