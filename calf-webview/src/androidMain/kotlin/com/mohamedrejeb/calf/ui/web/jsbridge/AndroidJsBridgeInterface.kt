package com.mohamedrejeb.calf.ui.web.jsbridge

import android.webkit.JavascriptInterface
import kotlinx.serialization.json.Json

/**
 * Android-specific JavaScript interface for handling JS messages
 */
class AndroidJsBridgeInterface(private val webViewJsBridge: WebViewJsBridge) {
    
    @JavascriptInterface
    fun call(request: String) {
        try {
            val message = Json.decodeFromString<JsMessage>(request)
            webViewJsBridge.dispatch(message)
        } catch (e: Exception) {
            // Handle JSON parsing errors gracefully
            e.printStackTrace()
        }
    }
    
    @JavascriptInterface
    fun callAndroid(
        id: Int,
        method: String,
        params: String,
    ) {
        try {
            val message = JsMessage(id, method, params)
            webViewJsBridge.dispatch(message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} 