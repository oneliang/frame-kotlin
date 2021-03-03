package com.oneliang.ktx.frame.mail

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.toFormatString
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.*
import java.util.*
import javax.mail.BodyPart
import javax.mail.Flags
import javax.mail.Multipart
import javax.mail.Part
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeUtility

class MailMessage(private val part: Part) {
    companion object {
        private val logger = LoggerManager.getLogger(MailMessage::class)
        private const val TYPE_TEXT_PLAIN = "text/plain"
        private const val TYPE_TEXT_HTML = "text/html"
        private const val TYPE_MULTIPART = "multipart/*"
        private const val TYPE_MESSAGE_RFC822 = "message/rfc822"
        private const val HEADER_DISPOSITION_NOTIFICATION_TO = "Disposition-Notification-TO"
    }

    /**
     * get from address
     * @return String
     * @throws Exception
     */
    @Suppress("UNCHECKED_CAST")
    @get:Throws(Exception::class)
    val fromAddress: String
        get() {
            val mimeMessage: MimeMessage = part as MimeMessage
            val address: Array<InternetAddress> = mimeMessage.from as Array<InternetAddress>
            val from: String = (address[0].address)
            val personal: String = (address[0].personal)
            return "$personal<$from>"
        }

    /**
     * get subject
     * @return String
     * @throws Exception
     */
    @get:Throws(Exception::class)
    val subject: String
        get() {
            val mimeMessage: MimeMessage = part as MimeMessage
            return MimeUtility.decodeText(mimeMessage.subject)
        }

    /**
     * send date
     * @return String
     * @throws Exception
     */
    @get:Throws(Exception::class)
    val sendDate: String
        get() {
            val mimeMessage: MimeMessage = part as MimeMessage
            val sendDate: Date = mimeMessage.sentDate
            return sendDate.toFormatString(Constants.Time.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        }

    /**
     * get mail body text
     * @return String
     * @throws Exception
     */
    @get:Throws(Exception::class)
    val mailBodyText: String
        get() = getMailBodyText(part)

    /**
     * get mail body text
     * @param part
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun getMailBodyText(part: Part): String {
        val bodyText = StringBuilder()
        val contentType: String = part.contentType
        val nameIndex = contentType.indexOf("name")
        var nameSign = false
        if (nameIndex != -1) {
            nameSign = true
        }
        if (part.isMimeType(TYPE_TEXT_PLAIN) && !nameSign) {
            bodyText.append(part.content as String)
        } else if (part.isMimeType(TYPE_TEXT_HTML) && !nameSign) {
            bodyText.append(part.content as String)
        } else if (part.isMimeType(TYPE_MULTIPART)) {
            val multipart: Multipart = part.content as Multipart
            val count: Int = multipart.count
            for (i in 0 until count) {
                bodyText.append(getMailBodyText(multipart.getBodyPart(i)))
            }
        } else if (part.isMimeType(TYPE_MESSAGE_RFC822)) {
            bodyText.append(getMailBodyText(part.content as Part))
        }
        return bodyText.toString()
    }

    /**
     * isNeedReply
     * @return boolean
     * @throws Exception
     */
    @get:Throws(Exception::class)
    val isNeedReply: Boolean
        get() {
            var isNeedReply = false
            val needReply: Array<String> = part.getHeader(HEADER_DISPOSITION_NOTIFICATION_TO)
            if (needReply.isNotEmpty()) {
                isNeedReply = true
            }
            return isNeedReply
        }

    /**
     * isNew
     * @return is new
     * @throws Exception
     */
    val isNew: Boolean
        @Throws(Exception::class)
        get() {
            var isNew = false
            val flags: Flags = (part as MimeMessage).flags
            val flag: Array<Flags.Flag> = flags.systemFlags
            for (i in flag.indices) {
                if (flag[i] == Flags.Flag.SEEN) {
                    isNew = true
                    break
                }
            }
            return isNew
        }

    /**
     * isContainAccessories
     * @return boolean
     * @throws Exception
     */
    val isContainAccessories: Boolean
        @Throws(Exception::class)
        get() = isContainAccessories(part)

    /**
     * isContainAccessories
     * @param part
     * @return boolean
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun isContainAccessories(part: Part): Boolean {
        var isContainAccessories = false
        if (part.isMimeType(TYPE_MULTIPART)) {
            val multipart: Multipart = part.content as Multipart
            val count: Int = multipart.count
            for (i in 0 until count) {
                val bodyPart: BodyPart = multipart.getBodyPart(i)
                val dispostion: String = bodyPart.disposition
                if ((dispostion == Part.ATTACHMENT || (dispostion
                                == Part.INLINE))) {
                    isContainAccessories = true
                } else if (bodyPart.isMimeType(TYPE_MULTIPART)) {
                    isContainAccessories = isContainAccessories(bodyPart)
                } else {
                    val contentType: String = bodyPart.contentType
                    if (contentType.toLowerCase().indexOf("application") != -1) {
                        isContainAccessories = true
                    }
                    if (contentType.toLowerCase().indexOf("name") != -1) {
                        isContainAccessories = true
                    }
                }
            }
        } else if (part.isMimeType(TYPE_MESSAGE_RFC822)) {
            isContainAccessories = isContainAccessories(part.content as Part)
        }
        return isContainAccessories
    }

    /**
     * saveAccessories
     * @throws Exception
     */
    @Throws(Exception::class)
    fun saveAccessories(path: String) {
        this.saveAccessories(part, path)
    }

    /**
     * saveAccessories
     * @param part
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun saveAccessories(part: Part, path: String) {
        if (part.isMimeType(TYPE_MULTIPART)) {
            val multipart: Multipart = part.content as Multipart
            for (i in 0 until multipart.count) {
                val bodyPart: BodyPart = multipart.getBodyPart(i)
                val dispostion = bodyPart.disposition ?: Constants.String.BLANK
                when {
                    dispostion == Part.ATTACHMENT || dispostion == Part.INLINE -> {
                        var filename = bodyPart.fileName
                        if (filename != null && filename.toLowerCase().indexOf(Constants.Encoding.GB2312.toLowerCase()) != -1) {
                            filename = MimeUtility.decodeText(filename)
                            saveFile(path, filename, bodyPart.inputStream)
                        }
                    }
                    bodyPart.isMimeType(TYPE_MULTIPART) -> {
                        this.saveAccessories(bodyPart, path)
                    }
                    else -> {
                        var filename = bodyPart.fileName
                        if (filename != null && filename.toLowerCase().indexOf(Constants.Encoding.GB2312.toLowerCase()) != -1) {
                            filename = MimeUtility.decodeText(filename)
                            saveFile(path, filename, bodyPart.inputStream)
                        }
                    }
                }
            }
        } else if (part.isMimeType(TYPE_MESSAGE_RFC822)) {
            this.saveAccessories(part.content as Part, path)
        }
    }

    /**
     * saveFile
     * @param filename
     * @param inputStream
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun saveFile(path: String, filename: String, inputStream: InputStream) {
        var tempFilename = filename
        if (tempFilename.isNotBlank()) {
            val lastIndex = tempFilename.lastIndexOf(File.separator)
            if (lastIndex > -1) {
                tempFilename = tempFilename.substring(lastIndex)
            }
            val file = File(path + File.separator + tempFilename)
            var bufferedOutputStream: BufferedOutputStream? = null
            var bufferedInputStream: BufferedInputStream? = null
            try {
                bufferedOutputStream = BufferedOutputStream(FileOutputStream(file))
                bufferedInputStream = BufferedInputStream(inputStream)
                val buffer = ByteArray(Constants.Capacity.BYTES_PER_KB)
                var length = -1
                while (bufferedInputStream.read(buffer, 0, buffer.size).also { length = it } != -1) {
                    bufferedOutputStream.write(buffer, 0, length)
                    bufferedOutputStream.flush()
                }
            } catch (e: Exception) {
                logger.error(Constants.String.EXCEPTION, e)
            } finally {
                bufferedInputStream?.close()
                bufferedOutputStream?.close()
            }
        }
    }
}