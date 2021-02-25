package com.oneliang.ktx.frame.mail

import com.oneliang.ktx.Constants
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
     * @param sendMailConfiguration
     * @throws Exception
     */
    @Throws(Exception::class)
    fun send(sendMailConfiguration: SendMailConfiguration, sendMailMessageList: List<SendMailMessage>, sessionPropertyMap: Map<String, Any> = emptyMap()) {
        val user = sendMailConfiguration.user
        val host = sendMailConfiguration.host
        val port = sendMailConfiguration.port
        val protocol = sendMailConfiguration.protocol
        val properties = Properties()
        // set host
        properties[MAIL_PROTOCOL_HOST.format(protocol)] = host
        // set port
        properties[MAIL_PROTOCOL_PORT.format(protocol)] = port
        // set authenticator true
        properties[MAIL_PROTOCOL_AUTH.format(protocol)] = true
        if (sendMailConfiguration.ssl) {
            properties[MAIL_PROTOCOL_SOCKETFACTORY_CLASS.format(protocol)] = SSL_SOCKETFACTORY_CLASS
        }
        sessionPropertyMap.forEach { (key, value) ->
            properties[key] = value
        }
        logger.info("send mail, session properties:%s", properties)
        val messageList = mutableListOf<MimeMessage>()
        val session: Session = Session.getInstance(properties)
        // debug mode
        session.debug = DEBUG
        System.setProperty("mail.mime.splitlongparameters", "false")
        sendMailMessageList.forEach { sendMailMessage ->
            // 向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
            val multipart: Multipart = MimeMultipart()
            // set text content
            val bodyPart: BodyPart = MimeBodyPart()
            //		bodyPart.setText(sendMailInformation.getText());
            bodyPart.setContent(sendMailMessage.content, "text/html;charset=UTF-8")
            multipart.addBodyPart(bodyPart)
            val accessoryPathList = sendMailMessage.accessoryPathList
            for (accessoryPath in accessoryPathList) {
                if (accessoryPath.isNotBlank()) { // add body part
                    val messageBodyPart = MimeBodyPart()
                    val source: DataSource = FileDataSource(accessoryPath)
                    // set accessories file
                    messageBodyPart.dataHandler = DataHandler(source)
                    // set accessories name
                    messageBodyPart.fileName = MimeUtility.encodeWord(source.name)
                    multipart.addBodyPart(messageBodyPart)
                }
            }
            val sentDate = Date()
            if (sendMailMessage.separateToAddress) {
                val toAddressList = sendMailMessage.toAddressList
                toAddressList.forEach { toAddress ->
                    if (toAddress.address.isNotBlank()) {
                        val message = MimeMessage(session)
                        message.setData(sendMailMessage.fromAddress, listOf(toAddress), sendMailMessage.subject, multipart, sentDate)
                        // save mail
                        message.saveChanges()
                        messageList += message
                    }
                }
            } else {
                val message = MimeMessage(session)
                message.setData(sendMailMessage.fromAddress, sendMailMessage.toAddressList, sendMailMessage.subject, multipart, sentDate)
                // save mail
                message.saveChanges()
                messageList += message
            }
        }
        if (messageList.isEmpty()) {
            logger.warning("mail message list is empty, please check it.")
            return
        }
        // get transport
        val transport: Transport = session.getTransport(protocol)
        // connection
        transport.connect(host, port, user, sendMailConfiguration.password)
        // send mail
        val commandMap = CommandMap.getDefaultCommandMap() as MailcapCommandMap
        commandMap.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
        commandMap.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
        commandMap.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
        commandMap.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
        commandMap.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822")
        CommandMap.setDefaultCommandMap(commandMap)
        messageList.forEach { mimeMessage ->
            transport.sendMessage(mimeMessage, mimeMessage.allRecipients)
        }
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

    class SendMailConfiguration {
        var host = Constants.String.BLANK
        var port = 25
        var user = Constants.String.BLANK
        var password = Constants.String.BLANK
        var ssl = false
        var protocol = SMTP
    }
}