package com.mohamedrejeb.calf.ui.web.jsbridge

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import com.mohamedrejeb.calf.ui.web.WebViewState

internal class DomContentLoadedInterface(
    private val webViewState: WebViewState,
    private val onDomReady: () -> Unit,
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onDomContentLoaded() {
        // @JavascriptInterface is invoked on the WebView's JavaBridge thread;
        // hop to the main thread before touching Compose state / injecting JS.
        mainHandler.post {
            // Inject the calf bridge first (so window.<jsBridgeName> exists before the DOM-ready
            // flag triggers plugin evaluation), then flip the flag.
            onDomReady()
            webViewState.domContentLoaded = true
        }
    }
}
