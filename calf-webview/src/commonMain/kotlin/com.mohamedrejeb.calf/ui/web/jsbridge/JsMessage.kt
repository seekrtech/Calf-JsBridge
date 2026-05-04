package com.mohamedrejeb.calf.ui.web.jsbridge

import kotlinx.serialization.Serializable

/**
 * A message dispatched from JS to native.
 * @param callbackId The callback id that will be used to send data back to JS.
 * @param methodName The name of the method that will be called on the native side.
 * @param params The parameters that will be passed to the native method. This should be a JSON string.
 */
@Serializable
data class JsMessage(
    val callbackId: Int,
    val methodName: String,
    val params: String,
) 