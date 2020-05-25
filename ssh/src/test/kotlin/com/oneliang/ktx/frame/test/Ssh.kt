package com.oneliang.ktx.frame.test

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

class Ssh {
    fun connect(host: String, port: Int = 22, user: String, password: String, configMap: Map<String, String> = emptyMap(), connectTimeout: Int = 0, afterSessionConnect: (session: Session) -> Unit): Session {
        val jSch = JSch()
        val session = jSch.getSession(user, host, port)
        session.setPassword(password)
        configMap.forEach { (key, value) ->
            session.setConfig(key, value);
        }
        session.connect(connectTimeout)
        afterSessionConnect(session)
        return session
    }
}