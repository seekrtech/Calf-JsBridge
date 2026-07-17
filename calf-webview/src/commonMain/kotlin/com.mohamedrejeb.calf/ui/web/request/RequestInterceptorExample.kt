package com.mohamedrejeb.calf.ui.web.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.web.WebContent
import com.mohamedrejeb.calf.ui.web.WebView
import com.mohamedrejeb.calf.ui.web.WebViewNavigator
import com.mohamedrejeb.calf.ui.web.WebViewState
import com.mohamedrejeb.calf.ui.web.rememberWebViewNavigator

/**
 * Example demonstrating how to use RequestInterceptor in calf-webview.
 * 
 * This example shows how to:
 * 1. Create a custom RequestInterceptor
 * 2. Intercept and modify requests
 * 3. Block certain requests
 * 4. Redirect requests to different URLs
 * 5. Handle external links (likely target="_blank") using isForMainFrame
 * 6. Distinguish between main frame and external navigation
 */
@Composable
fun RequestInterceptorExample() {
    val initialUrl = "https://www.bing.com/search?q=Kotlin"
    
    // Create a custom request interceptor
    val requestInterceptor = remember {
        object : RequestInterceptor {
            override fun onInterceptUrlRequest(
                request: WebRequest,
                navigator: WebViewNavigator,
            ): WebRequestInterceptResult {
                println("Intercepting request: ${request.url} (isForMainFrame: ${request.isForMainFrame})")
                
                return when {
                    // Handle external links (likely target="_blank" or iframe) specially
                    !request.isForMainFrame && request.url.contains("example.com") -> {
                        println("Intercepted external link to example.com - blocking it")
                        WebRequestInterceptResult.Reject
                    }
                    
                    // Allow other external links but log them
                    !request.isForMainFrame -> {
                        println("Allowing external link to: ${request.url}")
                        WebRequestInterceptResult.Allow
                    }
                    
                    // Redirect any URL containing "kotlin" to Kotlin documentation
                    request.url.contains("kotlin", ignoreCase = true) -> {
                        WebRequestInterceptResult.Modify(
                            WebRequest(
                                url = "https://kotlinlang.org/docs/multiplatform.html",
                                headers = mutableMapOf("Custom-Header" to "InterceptedRequest")
                            )
                        )
                    }
                    
                    // Block ads (example pattern)
                    request.url.contains("ads") || request.url.contains("doubleclick") -> {
                        WebRequestInterceptResult.Reject
                    }
                    
                    // Allow all other requests
                    else -> WebRequestInterceptResult.Allow
                }
            }
        }
    }
    
    // Create navigator with the request interceptor
    val navigator = rememberWebViewNavigator(requestInterceptor = requestInterceptor)
    
    // Create WebView state
    val webViewState = remember { WebViewState(WebContent.Url(initialUrl)) }
    
    var textFieldValue by remember { mutableStateOf(initialUrl) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // URL input section
        Text(
            text = "RequestInterceptor Example",
            modifier = Modifier.padding(16.dp)
        )
        
        Text(
            text = "• URLs containing 'kotlin' will be redirected to Kotlin docs\n" +
                    "• URLs containing 'ads' will be blocked\n" +
                    "• External links (non-main frame) to example.com will be blocked\n" +
                    "• Other external links will be allowed and logged\n" +
                    "• All other requests will be allowed",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            label = { Text("URL") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        
        Button(
            onClick = {
                navigator.loadUrl(textFieldValue)
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Load URL")
        }
        
        // WebView
        WebView(
            state = webViewState,
            navigator = navigator,
            modifier = Modifier.fillMaxSize()
        )
    }
} 