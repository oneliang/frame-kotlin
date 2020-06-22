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
    val toAddressList: MutableList<ToAddress> = CopyOnWriteArrayList()
    var subject = Constants.String.BLANK
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