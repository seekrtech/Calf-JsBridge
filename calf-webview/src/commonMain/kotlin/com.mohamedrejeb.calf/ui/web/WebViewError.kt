package com.mohamedrejeb.calf.ui.web

import androidx.compose.runtime.Immutable

/**
 * A wrapper class to hold errors from the WebView.
 */
@Immutable
data class WebViewError(
    /**
     * The request the error came from.
     */
    val code: Int,
    /**
     * The error that was reported.
     */
    val description: String,
    /**
     * Is the error related to a request from the main frame?
     */
    val isFromMainFrame: Boolean,
)
