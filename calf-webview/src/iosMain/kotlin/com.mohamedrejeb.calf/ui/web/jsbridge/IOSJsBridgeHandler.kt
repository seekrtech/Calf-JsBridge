package com.mohamedrejeb.calf.ui.web.jsbridge

import kotlinx.serialization.json.Json
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.darwin.NSObject

/**
 * iOS-specific WKWebKit script message handler for the JavaScript bridge
 */
class IOSJsBridgeHandler(private val webViewJsBridge: WebViewJsBridge) :
    WKScriptMessageHandlerProtocol,
    NSObject() {
    
    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage,
    ) {
        try {
            val body = didReceiveScriptMessage.body as? String ?: return
            val message = Json.decodeFromString<JsMessage>(body)
            webViewJsBridge.dispatch(message)
        } catch (e: Exception) {
            // Handle JSON parsing errors gracefully
            e.printStackTrace()
        }
    }
} 