package com.mohamedrejeb.calf.ui.web.request

/**
 * Represents the result of intercepting a web request.
 */
sealed interface WebRequestInterceptResult {
    /**
     * Allow the request to proceed normally.
     */
    data object Allow : WebRequestInterceptResult

    /**
     * Reject the request (block it from proceeding).
     */
    data object Reject : WebRequestInterceptResult

    /**
     * Modify the request before allowing it to proceed.
     */
    class Modify(val request: WebRequest) : WebRequestInterceptResult
} 