package com.mohamedrejeb.calf.ui.web.request

import com.mohamedrejeb.calf.ui.web.WebViewNavigator

/**
 * Interface for intercepting requests in WebView.
 */
interface RequestInterceptor {
    fun onInterceptUrlRequest(
        request: WebRequest,
        navigator: WebViewNavigator,
    ): WebRequestInterceptResult
} 