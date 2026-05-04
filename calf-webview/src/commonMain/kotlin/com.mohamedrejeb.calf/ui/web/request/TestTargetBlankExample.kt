package com.mohamedrejeb.calf.ui.web.request

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.calf.ui.web.WebContent
import com.mohamedrejeb.calf.ui.web.WebView
import com.mohamedrejeb.calf.ui.web.WebViewNavigator
import com.mohamedrejeb.calf.ui.web.WebViewState
import com.mohamedrejeb.calf.ui.web.rememberWebViewNavigator

/**
 * Example demonstrating external link interception using isForMainFrame.
 * 
 * This simplified approach uses isForMainFrame to distinguish between:
 * - Main frame navigation (regular links)
 * - External frame navigation (likely target="_blank" links or iframes)
 * 
 * This creates a simple HTML page with various link types to test
 * the RequestInterceptor's ability to handle external links.
 */
@Composable
fun TestTargetBlankExample() {
    val testHtml = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Target Blank Test</title>
            <style>
                body { font-family: Arial, sans-serif; padding: 20px; }
                .link-section { margin: 20px 0; padding: 15px; border: 1px solid #ccc; }
                a { display: block; margin: 10px 0; color: #0066cc; }
                .warning { background: #ffeb3b; padding: 10px; margin: 10px 0; border-radius: 4px; }
                .success { background: #4caf50; color: white; padding: 10px; margin: 10px 0; border-radius: 4px; }
                .error { background: #f44336; color: white; padding: 10px; margin: 10px 0; border-radius: 4px; }
            </style>
        </head>
        <body>
            <h1>RequestInterceptor Target="_blank" Test</h1>
            
            <div class="warning">
                <strong>⚠️ Important:</strong> target="_blank" interception has limitations, 
                especially on Android. Some links may open in system browser.
            </div>
            
            <div class="link-section">
                <h2>Normal Links (Should be intercepted ✅)</h2>
                <a href="https://kotlinlang.org/">Kotlin Official Site</a>
                <a href="https://github.com/">GitHub</a>
                <div class="success">These should always be intercepted by RequestInterceptor</div>
            </div>
            
            <div class="link-section">
                <h2>Target="_blank" Links (May or may not be intercepted ⚠️)</h2>
                <a href="https://example.com" target="_blank">Example.com (Should be BLOCKED if intercepted)</a>
                <a href="https://google.com" target="_blank">Google.com (Should be ALLOWED if intercepted)</a>
                <a href="https://stackoverflow.com" target="_blank">StackOverflow (Should be ALLOWED if intercepted)</a>
                <div class="warning">
                    Android: May open in system browser instead of being intercepted<br/>
                    iOS: More likely to be intercepted but not guaranteed
                </div>
            </div>
            
            <div class="link-section">
                <h2>JavaScript Links (Least likely to be intercepted ❌)</h2>
                <button onclick="window.open('https://example.com', '_blank')">JS Open Example.com</button>
                <button onclick="window.open('https://google.com', '_blank')">JS Open Google.com</button>
                <div class="error">
                    These JavaScript window.open() calls are very unlikely to be intercepted,
                    especially on Android. They may open in system browser.
                </div>
            </div>
            
            <div class="link-section">
                <h2>Test Other Interceptor Rules</h2>
                <a href="https://kotlinlang.org/docs/kotlin-docs" target="_blank">Kotlin Docs (Should be REDIRECTED if intercepted)</a>
                <a href="https://ads.google.com/test" target="_blank">Ads Link (Should be BLOCKED if intercepted)</a>
            </div>
            
            <div class="link-section">
                <h2>How to Test</h2>
                <p>1. Click the normal links first - they should be intercepted</p>
                <p>2. Try target="_blank" links - check console/logs</p>
                <p>3. If target="_blank" links open in system browser, that's expected behavior</p>
                <p>4. JavaScript buttons are least likely to work</p>
            </div>
        </body>
        </html>
    """.trimIndent()
    
    // Create a request interceptor that demonstrates target="_blank" handling
    val requestInterceptor = remember {
        object : RequestInterceptor {
            override fun onInterceptUrlRequest(
                request: WebRequest,
                navigator: WebViewNavigator,
            ): WebRequestInterceptResult {
                println("🔍 Intercepting: ${request.url}")
                println("   Is Main Frame: ${request.isForMainFrame}")
                println("   Method: ${request.method}")
                
                return when {
                    // Block external links (likely target="_blank") to example.com
                    !request.isForMainFrame && request.url.contains("example.com") -> {
                        println("   ❌ BLOCKING external link to example.com")
                        WebRequestInterceptResult.Reject
                    }
                    
                    // Allow other external links but log them
                    !request.isForMainFrame -> {
                        println("   ✅ ALLOWING external link to: ${request.url}")
                        WebRequestInterceptResult.Allow
                    }
                    
                    // Redirect any URL containing "kotlin" to Kotlin documentation
                    request.url.contains("kotlin", ignoreCase = true) -> {
                        println("   🔄 REDIRECTING kotlin-related URL")
                        WebRequestInterceptResult.Modify(
                            WebRequest(
                                url = "https://kotlinlang.org/docs/multiplatform.html",
                                headers = mutableMapOf("Custom-Header" to "InterceptedRequest")
                            )
                        )
                    }
                    
                    // Block ads
                    request.url.contains("ads") || request.url.contains("doubleclick") -> {
                        println("   ❌ BLOCKING ads URL")
                        WebRequestInterceptResult.Reject
                    }
                    
                    // Allow all other requests
                    else -> {
                        println("   ✅ ALLOWING request")
                        WebRequestInterceptResult.Allow
                    }
                }
            }
        }
    }
    
    val navigator = rememberWebViewNavigator(requestInterceptor = requestInterceptor)
    val webViewState = remember { WebViewState(WebContent.Data(testHtml)) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Target='_blank' Interceptor Test",
            modifier = Modifier.padding(16.dp)
        )
        
        Text(
            text = "⚠️ Note: target='_blank' interception has platform limitations. Check console/logs for actual behavior.",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        WebView(
            state = webViewState,
            navigator = navigator,
            modifier = Modifier.fillMaxSize()
        )
    }
} 