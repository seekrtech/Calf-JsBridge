# Calf WebView JavaScript Bridge

This document describes the JavaScript Bridge functionality that has been added to the Calf WebView library, enabling bidirectional communication between JavaScript and native Kotlin code.

## Features

- **Bidirectional Communication**: Send messages from JavaScript to native code and vice versa
- **Async Callback Support**: Handle asynchronous responses from native code back to JavaScript
- **Multiplatform Support**: Works on Android, iOS, and Desktop (partial support for JS/WASM targets)
- **Type Safety**: Uses Kotlin serialization for type-safe message passing
- **Easy Integration**: Simple API that integrates seamlessly with existing Calf WebView usage

## Basic Usage

### 1. Create a Message Handler

First, create a message handler by implementing the `IJsMessageHandler` interface:

```kotlin
@Serializable
data class GreetModel(val message: String)

class GreetMessageHandler : IJsMessageHandler {
    override fun methodName(): String = "Greet"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    ) {
        // Parse incoming parameters
        val param = processParams<GreetModel>(message)
        
        // Create response
        val response = GreetModel("Native received: ${param.message}")
        
        // Send response back to JavaScript
        callback(dataToJsonString(response))
    }
}
```

### 2. Set up the WebView with JsBridge

```kotlin
@Composable
fun MyWebViewScreen() {
    val navigator = rememberWebViewNavigator()
    val jsBridge = rememberWebViewJsBridge(navigator)
    val webViewState = rememberWebViewState("https://example.com")
    
    // Register your message handlers
    LaunchedEffect(jsBridge) {
        jsBridge.register(GreetMessageHandler())
    }
    
    WebView(
        state = webViewState,
        modifier = Modifier.fillMaxSize(),
        navigator = navigator,
        webViewJsBridge = jsBridge  // Pass the bridge to WebView
    )
}
```

### 3. Call Native from JavaScript

In your web page JavaScript, use the bridge to call native methods:

```javascript
// Check if bridge is available
if (window.calfJsBridge) {
    // Call native method with callback
    window.calfJsBridge.callNative(
        "Greet",  // Method name
        JSON.stringify({message: "Hello from JavaScript!"}),  // Parameters
        function(response) {  // Callback for response
            console.log("Response from native:", response);
            // Handle the response in your web page
        }
    );
}
```

### 4. Call JavaScript from Native

You can also send messages from native to JavaScript:

```kotlin
// Evaluate JavaScript directly
webViewState.evaluateJavascript(
    """
    document.getElementById("status").textContent = "Message from native!";
    """.trimIndent()
) { result ->
    // Handle result if needed
    println("JavaScript execution result: $result")
}
```

## Advanced Features

### Custom Bridge Name

By default, the bridge is available as `window.calfJsBridge`. You can customize this:

```kotlin
val customBridge = WebViewJsBridge(navigator, jsBridgeName = "myCustomBridge")
```

Then in JavaScript:
```javascript
window.myCustomBridge.callNative("MyMethod", "params", callback);
```

### Multiple Message Handlers

You can register multiple handlers for different methods:

```kotlin
LaunchedEffect(jsBridge) {
    jsBridge.register(GreetMessageHandler())
    jsBridge.register(AuthenticationHandler())
    jsBridge.register(FileOperationHandler())
}
```

### Error Handling

Always include proper error handling in your message handlers:

```kotlin
class SafeMessageHandler : IJsMessageHandler {
    override fun methodName(): String = "SafeMethod"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    ) {
        try {
            val param = processParams<MyData>(message)
            // Process the message...
            val result = processData(param)
            callback(dataToJsonString(result))
        } catch (e: Exception) {
            val error = ErrorResponse("Processing failed: ${e.message}")
            callback(dataToJsonString(error))
        }
    }
}
```

### Using WebViewNavigator

The navigator allows you to control the WebView from your message handlers:

```kotlin
class NavigationHandler : IJsMessageHandler {
    override fun methodName(): String = "Navigate"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    ) {
        val request = processParams<NavigationRequest>(message)
        
        when (request.action) {
            "back" -> navigator?.navigateBack()
            "forward" -> navigator?.navigateForward()
            "reload" -> navigator?.reload()
            "loadUrl" -> navigator?.loadUrl(request.url)
        }
        
        callback(dataToJsonString(SuccessResponse("Navigation completed")))
    }
}
```

## Complete Example

Here's a complete working example:

```kotlin
@Serializable
data class CalculationRequest(val a: Int, val b: Int, val operation: String)

@Serializable
data class CalculationResponse(val result: Int, val operation: String)

class CalculatorHandler : IJsMessageHandler {
    override fun methodName(): String = "Calculate"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    ) {
        try {
            val request = processParams<CalculationRequest>(message)
            
            val result = when (request.operation) {
                "add" -> request.a + request.b
                "subtract" -> request.a - request.b
                "multiply" -> request.a * request.b
                "divide" -> if (request.b != 0) request.a / request.b else 0
                else -> 0
            }
            
            val response = CalculationResponse(result, request.operation)
            callback(dataToJsonString(response))
        } catch (e: Exception) {
            callback(dataToJsonString(mapOf("error" to e.message)))
        }
    }
}

@Composable
fun CalculatorWebView() {
    val navigator = rememberWebViewNavigator()
    val jsBridge = rememberWebViewJsBridge(navigator)
    
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Calculator Bridge Example</title>
        </head>
        <body>
            <h1>Native Calculator</h1>
            <input type="number" id="num1" placeholder="First number">
            <select id="operation">
                <option value="add">+</option>
                <option value="subtract">-</option>
                <option value="multiply">×</option>
                <option value="divide">÷</option>
            </select>
            <input type="number" id="num2" placeholder="Second number">
            <button onclick="calculate()">Calculate</button>
            <div id="result"></div>
            
            <script>
                function calculate() {
                    const a = parseInt(document.getElementById('num1').value) || 0;
                    const b = parseInt(document.getElementById('num2').value) || 0;
                    const operation = document.getElementById('operation').value;
                    
                    window.calfJsBridge.callNative(
                        'Calculate',
                        JSON.stringify({a: a, b: b, operation: operation}),
                        function(response) {
                            const data = JSON.parse(response);
                            document.getElementById('result').innerHTML = 
                                data.error ? 'Error: ' + data.error : 
                                'Result: ' + data.result;
                        }
                    );
                }
            </script>
        </body>
        </html>
    """.trimIndent()
    
    val webViewState = remember { WebViewState(WebContent.Data(htmlContent)) }
    
    LaunchedEffect(jsBridge) {
        jsBridge.register(CalculatorHandler())
    }
    
    WebView(
        state = webViewState,
        modifier = Modifier.fillMaxSize(),
        navigator = navigator,
        webViewJsBridge = jsBridge
    )
}
```

## JavaScript API Reference

### window.calfJsBridge.callNative(methodName, params, callback)

- **methodName**: String - The name of the native method to call
- **params**: String - JSON string containing the parameters
- **callback**: Function - Optional callback function to handle the response

### Example JavaScript Usage

```javascript
// Simple call without callback
window.calfJsBridge.callNative("LogMessage", JSON.stringify({level: "info", message: "Hello"}));

// Call with response handling
window.calfJsBridge.callNative(
    "GetUserData", 
    JSON.stringify({userId: 123}),
    function(response) {
        const userData = JSON.parse(response);
        // Use the data...
    }
);

// Always check if bridge is available
if (window.calfJsBridge) {
    // Make bridge calls...
} else {
    console.error("Calf JsBridge not available");
}
```

## Platform Support

- ✅ **Android**: Full support using `JavascriptInterface`
- ✅ **iOS**: Full support using `WKWebKit` message handlers
- ⚠️ **Desktop**: Partial support (requires platform-specific implementation)
- ⚠️ **JS/WASM**: Limited support (requires different architecture)

## Troubleshooting

### Bridge Not Available
If `window.calfJsBridge` is undefined:
1. Ensure you've passed the `webViewJsBridge` parameter to the WebView
2. Check that the page has finished loading before making calls
3. Verify JavaScript is enabled in WebView settings

### Messages Not Received
If native handlers aren't receiving messages:
1. Verify the method name matches exactly (case-sensitive)
2. Ensure the handler is registered before the page loads
3. Check for JSON parsing errors in parameters

### Callbacks Not Working
If JavaScript callbacks aren't triggered:
1. Ensure you're calling the callback function in your handler
2. Verify the response is valid JSON
3. Check for JavaScript errors in the browser console

## Performance Considerations

- Message passing involves serialization/deserialization overhead
- Avoid sending large data objects frequently
- Consider batching multiple operations when possible
- Use appropriate error handling to prevent crashes

## Security Notes

- Always validate and sanitize data received from JavaScript
- Be cautious when exposing sensitive native functionality
- Consider implementing authentication/authorization for critical operations
- Avoid exposing file system or network access without proper validation 