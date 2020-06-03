package com.oneliang.ktx.frame.mail

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
    private const val MAIL_SMTP_HOST = "mail.smtp.host"
    private const val MAIL_SMTP_AUTH = "mail.smtp.auth"
    private const val SMTP = "smtp"
    private const val POP3 = "pop3"
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
    fun send(sendMailInformation: SendMailInformation) {
        val from = sendMailInformation.fromAddress
        val user = sendMailInformation.user
        val host = sendMailInformation.host
        val properties = Properties()
        // set host
        properties[MAIL_SMTP_HOST] = host
        // set authenticator true
        properties[MAIL_SMTP_AUTH] = true
        val session: Session = Session.getDefaultInstance(properties)
        // debug mode
        session.setDebug(DEBUG)
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
        message.setSubject(sendMailInformation.subject)
        // send date
        message.setSentDate(Date())
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
                messageBodyPart.setDataHandler(DataHandler(source))
                // set accessories name
                messageBodyPart.setFileName(MimeUtility.encodeText(source.name))
                multipart.addBodyPart(messageBodyPart)
            }
        }
        // set content
        message.setContent(multipart)
        // save mail
        message.saveChanges()
        // get transport
        val transport: Transport = session.getTransport(SMTP)
        // connection
        transport.connect(host, user, sendMailInformation.password)
        // send mail
        val commandMap = CommandMap.getDefaultCommandMap() as MailcapCommandMap
        commandMap.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
        commandMap.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
        commandMap.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
        commandMap.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
        commandMap.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822")
        CommandMap.setDefaultCommandMap(commandMap)
        transport.sendMessage(message, message.getAllRecipients())
        transport.close()
    }

    /**
     * receive mail
     * @param receiveMailInformation
     * @return List<MailMessage>
     * @throws Exception
    </MailMessage> */
    @Throws(Exception::class)
    fun receive(receiveMailInformation: ReceiveMailInformation): List<MailMessage> {
        val mailMessageList: MutableList<MailMessage> = ArrayList()
        val user = receiveMailInformation.user
        val host = receiveMailInformation.host
        val properties = Properties()
        properties[MAIL_SMTP_HOST] = host
        properties[MAIL_SMTP_AUTH] = true
        val session: Session = Session.getDefaultInstance(properties)
        val urlName = URLName(POP3, host, 110, null, user, receiveMailInformation.password)
        val store: Store = session.getStore(urlName)
        store.connect()
        val folder: Folder = store.getFolder(FOLDER_INBOX)
        folder.open(Folder.READ_ONLY)
        val messages: Array<Message> = folder.getMessages()
        for (message in messages) {
            mailMessageList.add(MailMessage(message))
        }
        return mailMessageList
    }

    /**
     * test
     * @param args
     * @throws Exception
     */
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) { //send
        val mailBean = SendMailInformation()
        mailBean.fromAddress = "13422349169@163.com"
        mailBean.host = "smtp.163.com"
        mailBean.user = "13422349169"
        mailBean.password = "borqs654321"
        mailBean.subject = "test"
        mailBean.content = "test"
        mailBean.addToAddress(ToAddress("stephen8558@gmail.com"))
        send(mailBean)
        //receive
//		ReceiveMailInformation receiveMailInformation=new ReceiveMailInformation();
//		receiveMailInformation.setHost("mail.kidgrow.cn");
//		receiveMailInformation.setUser("noreply@kidgrow.cn");
//		receiveMailInformation.setPassword("t6g4f3");
//		List<MailMessage> mailMessageList=Mail.receive(receiveMailInformation);
//		String path="C:\\temp";
//		FileUtil.createDirectory(path);
//		for(MailMessage mailMessage:mailMessageList){
//			System.out.println(mailMessage.getFromAddress());
//			mailMessage.saveAccessories(path);
//		}
    }
}