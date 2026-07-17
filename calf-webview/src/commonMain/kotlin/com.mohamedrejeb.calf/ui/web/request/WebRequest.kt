package com.mohamedrejeb.calf.ui.web.request

/**
 * Represents a web request that can be intercepted by a RequestInterceptor.
 */
data class WebRequest(
    val url: String,
    val headers: MutableMap<String, String> = mutableMapOf(),
    val isForMainFrame: Boolean = false,
    val isRedirect: Boolean = false,
    val method: String = "GET",
) 