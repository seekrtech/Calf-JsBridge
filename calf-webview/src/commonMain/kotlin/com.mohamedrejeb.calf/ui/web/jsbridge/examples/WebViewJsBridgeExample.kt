package com.mohamedrejeb.calf.ui.web.jsbridge.examples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.web.WebView
import com.mohamedrejeb.calf.ui.web.WebViewState
import com.mohamedrejeb.calf.ui.web.WebContent
import com.mohamedrejeb.calf.ui.web.rememberWebViewNavigator
import com.mohamedrejeb.calf.ui.web.jsbridge.WebViewJsBridge
import com.mohamedrejeb.calf.ui.web.jsbridge.rememberWebViewJsBridge

/**
 * Example usage of WebViewJsBridge with Calf WebView
 */
@Composable
fun WebViewJsBridgeExample() {
    // Create navigator and bridge
    val navigator = rememberWebViewNavigator()
    val jsBridge = rememberWebViewJsBridge(navigator)
    
    // Create state with HTML content that demonstrates the bridge
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Calf WebView JsBridge Example</title>
            <style>
                body { 
                    font-family: Arial, sans-serif; 
                    padding: 20px; 
                    text-align: center;
                }
                button { 
                    padding: 10px 20px; 
                    margin: 10px; 
                    font-size: 16px;
                    background-color: #007AFF;
                    color: white;
                    border: none;
                    border-radius: 8px;
                    cursor: pointer;
                }
                button:hover {
                    background-color: #0056CC;
                }
                #response { 
                    margin-top: 20px; 
                    padding: 20px; 
                    background-color: #f0f0f0; 
                    border-radius: 8px;
                    min-height: 40px;
                }
            </style>
        </head>
        <body>
            <h1>Calf WebView JsBridge Demo</h1>
            <p>Click the button below to test the JavaScript bridge:</p>
            
            <button onclick="sendGreeting()">Send Greeting to Native</button>
            <button onclick="sendCustomMessage()">Send Custom Message</button>
            
            <div id="response">
                <em>Response will appear here...</em>
            </div>
            
            <script>
                function sendGreeting() {
                    // Check if the bridge is available
                    if (window.calfJsBridge) {
                        window.calfJsBridge.callNative(
                            "Greet", 
                            JSON.stringify({message: "Hello from JavaScript!"}),
                            function(response) {
                                document.getElementById("response").innerHTML = 
                                    "<strong>Native Response:</strong> " + response;
                            }
                        );
                    } else {
                        document.getElementById("response").innerHTML = 
                            "<strong>Error:</strong> Bridge not available";
                    }
                }
                
                function sendCustomMessage() {
                    if (window.calfJsBridge) {
                        window.calfJsBridge.callNative(
                            "Greet",
                            JSON.stringify({message: "Custom message from web!"}),
                            function(response) {
                                document.getElementById("response").innerHTML = 
                                    "<strong>Custom Response:</strong> " + response;
                            }
                        );
                    } else {
                        document.getElementById("response").innerHTML = 
                            "<strong>Error:</strong> Bridge not available";
                    }
                }
                
                // Log that the page has loaded
                console.log("WebView JsBridge example page loaded");
            </script>
        </body>
        </html>
    """.trimIndent()
    
    val webViewState = remember { WebViewState(WebContent.Data(htmlContent)) }
    var nativeResponse by remember { mutableStateOf("No response yet") }
    
    // Register the example message handler
    LaunchedEffect(jsBridge) {
        jsBridge.register(GreetMessageHandler())
    }
    
    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            // Native control section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Native Controls",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Text(
                    text = "Last response: $nativeResponse",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Button(
                    onClick = {
                        // Example of calling JavaScript from native
                        webViewState.evaluateJavascript(
                            """
                            document.getElementById("response").innerHTML = 
                                "<strong>Message from Native:</strong> Hello from Kotlin!";
                            """.trimIndent()
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send Message to WebView")
                }
            }
            
            // WebView section
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                WebView(
                    state = webViewState,
                    modifier = Modifier.fillMaxSize(),
                    navigator = navigator,
                    webViewJsBridge = jsBridge,
                    onCreated = {
                        println("WebView created with JsBridge support")
                    }
                )
            }
        }
    }
} 