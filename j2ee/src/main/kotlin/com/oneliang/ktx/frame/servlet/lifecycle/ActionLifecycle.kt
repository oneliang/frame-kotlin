package com.oneliang.ktx.frame.servlet.lifecycle

import com.oneliang.ktx.frame.servlet.ActionListener
import com.oneliang.ktx.frame.servlet.action.ActionInterface
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.CopyOnWriteArrayList
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ActionLifecycle : ActionListener.Lifecycle {
    companion object {
        private val logger = LoggerManager.getLogger(ActionLifecycle::class)
        val lifecycleList = CopyOnWriteArrayList<ActionListener.Lifecycle>()
    }

    override fun onRequest(uri: String, request: HttpServletRequest, response: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod) {
        logger.info("Action lifecycle on request, uri:%s", uri)
    }

    override fun onResponse(uri: String, request: HttpServletRequest, response: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod) {
        logger.info("Action lifecycle on response, uri:%s", uri)
    }
}