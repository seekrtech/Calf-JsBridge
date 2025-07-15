package com.mohamedrejeb.calf.ui.web.jsbridge

import com.mohamedrejeb.calf.ui.web.WebViewState

/**
 * Helper class for injecting JavaScript bridge code into a WebView
 */
object JsBridgeInjector {
    
    /**
     * Injects the JavaScript bridge code into the WebView
     */
    fun injectJsBridge(webViewState: WebViewState, webViewJsBridge: WebViewJsBridge) {
        val jsBridgeName = webViewJsBridge.jsBridgeName
        
        val initJs = """
            window.$jsBridgeName = {
                callbacks: {},
                callbackId: 0,
                callNative: function (methodName, params, callback) {
                    var message = {
                        methodName: methodName,
                        params: params,
                        callbackId: callback ? window.$jsBridgeName.callbackId++ : -1
                    };
                    if (callback) {
                        window.$jsBridgeName.callbacks[message.callbackId] = callback;
                        console.log('add callback: ' + message.callbackId + ', ' + callback);
                    }
                    window.$jsBridgeName.postMessage(JSON.stringify(message));
                },
                onCallback: function (callbackId, data) {
                    var callback = window.$jsBridgeName.callbacks[callbackId];
                    console.log('onCallback: ' + callbackId + ', ' + data + ', ' + callback);
                    if (callback) {
                        callback(data);
                        delete window.$jsBridgeName.callbacks[callbackId];
                    }
                }
            };
        """.trimIndent()
        
        webViewState.evaluateJavascript(initJs)
        
        // Store reference for callbacks
        webViewJsBridge.webViewState = webViewState
    }
    
    /**
     * Injects platform-specific bridge implementation
     */
    fun injectPlatformBridge(webViewState: WebViewState, webViewJsBridge: WebViewJsBridge, platformScript: String) {
        webViewState.evaluateJavascript(platformScript)
    }
} 