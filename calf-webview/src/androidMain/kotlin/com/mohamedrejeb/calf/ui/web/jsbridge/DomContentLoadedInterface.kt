package com.mohamedrejeb.calf.ui.web.jsbridge

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import com.mohamedrejeb.calf.ui.web.WebViewState

internal class DomContentLoadedInterface(
    private val webViewState: WebViewState,
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onDomContentLoaded() {
        // @JavascriptInterface is invoked on the WebView's JavaBridge thread;
        // hop to the main thread before writing Compose snapshot state.
        mainHandler.post { webViewState.domContentLoaded = true }
    }
}
