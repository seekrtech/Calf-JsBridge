package com.mohamedrejeb.calf.ui.web.jsbridge

import com.mohamedrejeb.calf.ui.web.WebViewState

/**
 * Helper class for injecting JavaScript bridge code into a WebView
 */
object JsBridgeInjector {

    /**
     * The script that installs `window.<jsBridgeName>` (callNative + callback plumbing).
     *
     * Idempotent: the first installation wins, so re-running it (document-start user
     * script followed by the DOM-ready / page-finished fallback paths) never replaces
     * an existing bridge object — replacing it would orphan callbacks page code has
     * already registered.
     */
    fun bridgeInitScript(jsBridgeName: String): String = """
        if (!window.$jsBridgeName) {
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
        }
    """.trimIndent()

    /**
     * Complete bridge script — init plus the platform `postMessage` wiring — suitable
     * for document-start installation (WKUserScript / addDocumentStartJavaScript) so
     * page scripts that run before DOMContentLoaded can already reach native.
     *
     * @param platformPostMessageBody JS statement(s) forwarding `message` to the
     * platform message handler, e.g.
     * `window.webkit.messageHandlers.iosJsBridge.postMessage(message);`
     */
    fun documentStartBridgeScript(jsBridgeName: String, platformPostMessageBody: String): String = """
        ${bridgeInitScript(jsBridgeName)}
        if (!window.$jsBridgeName.postMessage) {
            window.$jsBridgeName.postMessage = function (message) {
                $platformPostMessageBody
            };
        }
    """.trimIndent()

    /**
     * Injects the JavaScript bridge code into the WebView
     */
    fun injectJsBridge(webViewState: WebViewState, webViewJsBridge: WebViewJsBridge) {
        webViewState.evaluateJavascript(bridgeInitScript(webViewJsBridge.jsBridgeName))

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
