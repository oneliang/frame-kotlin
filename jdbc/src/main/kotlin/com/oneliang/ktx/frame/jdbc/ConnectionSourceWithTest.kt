package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import java.sql.Connection

class ConnectionSourceWithTest : ConnectionSource() {
    /**
     * test
     */
    var useTest: Boolean = false
    /**
     * test url
     */
    var testUrl: String = Constants.String.BLANK
    /**
     * test user
     */
    var testUser: String = Constants.String.BLANK
    /**
     * test password
     */
    var testPassword: String = Constants.String.BLANK

    /**
     * get resource
     */
    override val resource: Connection?
        get() {
            return if (!useTest) {
                super.resource
            } else {
                getConnection(this.testUrl, this.testUser, this.testPassword)
            }
        }
}