package com.mohamedrejeb.calf.ui.web.jsbridge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import com.mohamedrejeb.calf.ui.web.WebViewNavigator
import com.mohamedrejeb.calf.ui.web.WebViewState

/**
 * A bridge that can be used to communicate between native and web.
 */
@Immutable
open class WebViewJsBridge(
    val navigator: WebViewNavigator? = null, 
    val jsBridgeName: String = "calfJsBridge"
) {
    private val jsMessageDispatcher = JsMessageDispatcher()
    var webViewState: WebViewState? = null

    fun register(handler: IJsMessageHandler) {
        jsMessageDispatcher.registerJSHandler(handler)
    }

    fun unregister(handler: IJsMessageHandler) {
        jsMessageDispatcher.unregisterJSHandler(handler)
    }

    fun clear() {
        jsMessageDispatcher.clear()
    }

    fun dispatch(message: JsMessage) {
        jsMessageDispatcher.dispatch(message, navigator) {
            onCallback(it, message.callbackId)
        }
    }

    private fun onCallback(
        data: String,
        callbackId: Int,
    ) {
        webViewState?.evaluateJavascript("window.$jsBridgeName.onCallback($callbackId, '$data')")
    }
}

/**
 * Create a [WebViewJsBridge] that is remembered across Compositions.
 */
@Composable
fun rememberWebViewJsBridge(navigator: WebViewNavigator? = null, jsBridgeName: String = "calfJsBridge"): WebViewJsBridge =
    remember { WebViewJsBridge(navigator, jsBridgeName) }