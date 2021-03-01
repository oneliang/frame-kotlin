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
        private val lifecycleList = CopyOnWriteArrayList<ActionListener.Lifecycle>()
        fun registerLifecycle(lifecycle: ActionListener.Lifecycle) {
            lifecycleList += lifecycle
        }

        fun unRegisterLifecycle(lifecycle: ActionListener.Lifecycle) {
            lifecycleList -= lifecycle
        }
    }

    override fun onRequest(uri: String, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod) {
        logger.info("Action lifecycle on request, uri:%s", uri)
        for (lifecycle in lifecycleList) {
            lifecycle.onRequest(uri, httpServletRequest, httpServletResponse, httpRequestMethod)
        }
    }

    override fun onResponse(uri: String, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod) {
        logger.info("Action lifecycle on response, uri:%s", uri)
        for (lifecycle in lifecycleList) {
            lifecycle.onResponse(uri, httpServletRequest, httpServletResponse, httpRequestMethod)
        }
    }
}