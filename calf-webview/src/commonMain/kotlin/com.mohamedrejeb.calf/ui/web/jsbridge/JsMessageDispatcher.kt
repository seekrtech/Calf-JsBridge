package com.mohamedrejeb.calf.ui.web.jsbridge

import androidx.compose.runtime.Immutable
import com.mohamedrejeb.calf.ui.web.WebViewNavigator

/**
 * A message dispatcher that routes JS messages to registered handlers.
 */
@Immutable
internal class JsMessageDispatcher {
    private val jsHandlerMap = mutableMapOf<String, IJsMessageHandler>()

    fun registerJSHandler(handler: IJsMessageHandler) {
        jsHandlerMap[handler.methodName()] = handler
    }

    fun dispatch(
        message: JsMessage,
        navigator: WebViewNavigator? = null,
        callback: (String) -> Unit,
    ) {
        jsHandlerMap[message.methodName]?.handle(message, navigator, callback)
    }

    fun canHandle(id: String) = jsHandlerMap.containsKey(id)

    fun unregisterJSHandler(handler: IJsMessageHandler) {
        jsHandlerMap.remove(handler.methodName())
    }

    fun clear() {
        jsHandlerMap.clear()
    }
} 