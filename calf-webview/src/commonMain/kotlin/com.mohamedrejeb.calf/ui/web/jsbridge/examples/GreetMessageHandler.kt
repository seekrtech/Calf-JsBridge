package com.mohamedrejeb.calf.ui.web.jsbridge.examples

import com.mohamedrejeb.calf.ui.web.WebViewNavigator
import com.mohamedrejeb.calf.ui.web.jsbridge.IJsMessageHandler
import com.mohamedrejeb.calf.ui.web.jsbridge.JsMessage
import com.mohamedrejeb.calf.ui.web.jsbridge.dataToJsonString
import com.mohamedrejeb.calf.ui.web.jsbridge.processParams
import kotlinx.serialization.Serializable

/**
 * Example data model for the greet message handler
 */
@Serializable
data class GreetModel(val message: String)

/**
 * Example message handler that responds to "Greet" messages from JavaScript
 */
class GreetMessageHandler : IJsMessageHandler {
    override fun methodName(): String {
        return "Greet"
    }

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    ) {
        try {
            // Parse the incoming parameters
            val param = processParams<GreetModel>(message)
            
            // Process the message and create a response
            val response = GreetModel("Calf WebView received: ${param.message}")
            
            // Send the response back to JavaScript
            callback(dataToJsonString(response))
            
            // Optionally use the navigator to control the WebView
            // navigator?.loadUrl("https://example.com")
            
        } catch (e: Exception) {
            // Handle errors gracefully
            val errorResponse = GreetModel("Error processing message: ${e.message}")
            callback(dataToJsonString(errorResponse))
        }
    }
} 