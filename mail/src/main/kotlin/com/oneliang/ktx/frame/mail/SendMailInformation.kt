package com.oneliang.ktx.frame.mail

import com.oneliang.ktx.Constants
import java.util.concurrent.CopyOnWriteArrayList

class SendMailInformation {
    /**
     * @return the host
     */
    /**
     * @param host the host to set
     */
    var host: String? = null
    /**
     * @return the fromAddress
     */
    /**
     * @param fromAddress the fromAddress to set
     */
    var fromAddress: String? = null
    /**
     * @return the user
     */
    /**
     * @param user the user to set
     */
    var user: String? = null
    /**
     * @return the password
     */
    /**
     * @param password the password to set
     */
    var password: String? = null
    val toAddressList: MutableList<ToAddress> = CopyOnWriteArrayList()
    /**
     * @return the subject
     */
    /**
     * @param subject the subject to set
     */
    var subject: String? = null
    /**
     * @return the content
     */
    /**
     * @param content the content to set
     */
    var content: String = Constants.String.BLANK
    val accessoryPathList: MutableList<String> = CopyOnWriteArrayList()

    /**
     * add to address
     * @param toAddress
     * @return boolean
     */
    fun addToAddress(toAddress: ToAddress): Boolean {
        return toAddressList.add(toAddress)
    }

    /**
     * @param accessoryPath
     * @return boolean
     */
    fun addAccessoryPath(accessoryPath: String): Boolean {
        return accessoryPathList.add(accessoryPath)
    }
}