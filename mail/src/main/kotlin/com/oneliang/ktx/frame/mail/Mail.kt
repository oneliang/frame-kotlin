package com.oneliang.ktx.frame.mail

import com.oneliang.ktx.util.logging.LoggerManager
import java.util.*
import javax.activation.*
import javax.mail.BodyPart
import javax.mail.Folder
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.Store
import javax.mail.Transport
import javax.mail.URLName
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeUtility

object Mail {
    private val logger = LoggerManager.getLogger(Mail::class)
    private const val MAIL_PROTOCOL_HOST = "mail.%s.host"
    private const val MAIL_PROTOCOL_PORT = "mail.%s.port"
    private const val MAIL_PROTOCOL_AUTH = "mail.%s.auth"
    private const val MAIL_PROTOCOL_SOCKETFACTORY_CLASS = "mail.%s.socketFactory.class"
    const val MAIL_STORE_PROTOCOL = "mail.store.protocol"
    const val MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable"
    const val MAIL_SMTP_SOCKETFACTORY_PORT = "mail.smtp.socketFactory.port"
    private const val SSL_SOCKETFACTORY_CLASS = "javax.net.ssl.SSLSocketFactory"
    const val SMTP = "smtp"
    const val POP3 = "pop3"
    const val IMAP = "imap"
    private const val FOLDER_INBOX = "INBOX"
    /**
     * debug mode
     */
    var DEBUG = false

    /**
     * send mail
     * @param sendMailInformation
     * @throws Exception
     */
    @Throws(Exception::class)
    fun send(sendMailInformation: SendMailInformation, sessionPropertyMap: Map<String, Any> = emptyMap()) {
        val from = sendMailInformation.fromAddress
        val user = sendMailInformation.user
        val host = sendMailInformation.host
        val port = sendMailInformation.port
        val protocol = sendMailInformation.protocol
        val properties = Properties()
        // set host
        properties[MAIL_PROTOCOL_HOST.format(protocol)] = host
        // set port
        properties[MAIL_PROTOCOL_PORT.format(protocol)] = port
        // set authenticator true
        properties[MAIL_PROTOCOL_AUTH.format(protocol)] = true
        if (sendMailInformation.ssl) {
            properties[MAIL_PROTOCOL_SOCKETFACTORY_CLASS.format(protocol)] = SSL_SOCKETFACTORY_CLASS
        }
        sessionPropertyMap.forEach { (key, value) ->
            properties[key] = value
        }
        logger.info("send mail, session properties:%s", properties)
        val session: Session = Session.getInstance(properties)
        // debug mode
        session.debug = DEBUG
        val message = MimeMessage(session)
        // from address
        message.setFrom(InternetAddress(from))
        // to address
        val toAddressList = sendMailInformation.toAddressList
        for (to in toAddressList) {
            val type = to.type
            val address = InternetAddress(to.address)
            when (type) {
                ToAddress.Type.TO -> message.addRecipient(Message.RecipientType.TO, address)
                ToAddress.Type.BCC -> message.addRecipient(Message.RecipientType.BCC, address)
                ToAddress.Type.CC -> message.addRecipient(Message.RecipientType.CC, address)
            }
        }
        // set subject
        message.subject = sendMailInformation.subject
        // send date
        message.sentDate = Date()
        // 向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
        val multipart: Multipart = MimeMultipart()
        // set text content
        val bodyPart: BodyPart = MimeBodyPart()
        //		bodyPart.setText(sendMailInformation.getText());
        bodyPart.setContent(sendMailInformation.content, "text/html;charset=UTF-8")
        multipart.addBodyPart(bodyPart)
        val accessoryPathList = sendMailInformation.accessoryPathList
        for (accessoryPath in accessoryPathList) {
            if (accessoryPath.isNotBlank()) { // add body part
                val messageBodyPart: BodyPart = MimeBodyPart()
                val source: DataSource = FileDataSource(accessoryPath)
                // set accessories file
                messageBodyPart.dataHandler = DataHandler(source)
                // set accessories name
                messageBodyPart.fileName = MimeUtility.encodeText(source.name)
                multipart.addBodyPart(messageBodyPart)
            }
        }
        // set content
        message.setContent(multipart)
        // save mail
        message.saveChanges()
        // get transport
        val transport: Transport = session.getTransport(protocol)
        // connection
        transport.connect(host, port, user, sendMailInformation.password)
        // send mail
        val commandMap = CommandMap.getDefaultCommandMap() as MailcapCommandMap
        commandMap.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
        commandMap.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
        commandMap.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
        commandMap.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
        commandMap.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822")
        CommandMap.setDefaultCommandMap(commandMap)
        transport.sendMessage(message, message.allRecipients)
        transport.close()
    }

    /**
     * receive mail
     * @param receiveMailInformation
     * @return List<MailMessage>
     * @throws Exception
    </MailMessage> */
    @Throws(Exception::class)
    fun receive(receiveMailInformation: ReceiveMailInformation, sessionPropertyMap: Map<String, Any> = emptyMap()): List<MailMessage> {
        val mailMessageList = mutableListOf<MailMessage>()
        val user = receiveMailInformation.user
        val host = receiveMailInformation.host
        val port = receiveMailInformation.port
        val protocol = receiveMailInformation.protocol
        val properties = Properties()
        properties[MAIL_PROTOCOL_HOST.format(protocol)] = host
        properties[MAIL_PROTOCOL_PORT.format(protocol)] = port
        properties[MAIL_PROTOCOL_AUTH.format(protocol)] = true
        if (receiveMailInformation.ssl) {
            properties[MAIL_PROTOCOL_SOCKETFACTORY_CLASS.format(protocol)] = SSL_SOCKETFACTORY_CLASS
        }
        sessionPropertyMap.forEach { (key, value) ->
            properties[key] = value
        }
        logger.info("receive mail, session properties:%s", properties)
        val session: Session = Session.getInstance(properties)
        val urlName = URLName(protocol, host, port, null, user, receiveMailInformation.password)
        val store: Store = session.getStore(urlName)
        store.connect()
        val folder: Folder = store.getFolder(FOLDER_INBOX)
        folder.open(Folder.READ_ONLY)
        val messages: Array<Message> = folder.messages
        for (message in messages) {
            mailMessageList.add(MailMessage(message))
        }
        return mailMessageList
    }
}