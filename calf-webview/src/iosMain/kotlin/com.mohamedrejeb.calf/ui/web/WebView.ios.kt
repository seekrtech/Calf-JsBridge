package com.mohamedrejeb.calf.ui.web

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectZero
import platform.Foundation.HTTPMethod
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.setValue
import platform.Foundation.dataUsingEncoding
import platform.Foundation.NSURL
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.allHTTPHeaderFields
import platform.WebKit.*
import platform.darwin.NSObject
import platform.UIKit.UIScrollViewContentInsetAdjustmentBehavior
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

actual typealias PlatformWebView = WKWebView

/**
 * A wrapper around the Android View WebView to provide a basic WebView composable.
 *
 * If you require more customisation you are most likely better rolling your own and using this
 * wrapper as an example.
 *
 * The WebView attempts to set the layoutParams based on the Compose modifier passed in. If it
 * is incorrectly sizing, use the layoutParams composable function instead.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param modifier A compose modifier
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the WebView is first created, this can be used to set additional
 * settings on the WebView. WebChromeClient and WebViewClient should not be set here as they will be
 * subsequently overwritten after this lambda is called.
 * @param onDispose Called when the WebView is destroyed. Provides a bundle which can be saved
 * if you need to save and restore state in this WebView.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun WebView(
    state: WebViewState,
    modifier: Modifier,
    alpha: Float,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    webViewJsBridge: com.mohamedrejeb.calf.ui.web.jsbridge.WebViewJsBridge?,
    loadContentDelay: Duration,
    onCreated: (PlatformWebView) -> Unit,
    onDispose: (PlatformWebView) -> Unit,
) {
    val webView = state.webView

    webView?.let { wv ->
        LaunchedEffect(wv, navigator) {
            navigator.handleNavigationEvents(wv)
        }

        LaunchedEffect(wv, state) {
            snapshotFlow { state.content }.collect { content ->
                delay(loadContentDelay)
                when (content) {
                    is WebContent.Url -> {
                        val url = NSURL(string = content.url)
                        val urlRequest = NSMutableURLRequest()
                        urlRequest.setURL(url)
                        content.additionalHttpHeaders.forEach { (key, value) ->
                            urlRequest.setValue(value = value, forHTTPHeaderField = key)
                        }
                        wv.loadRequest(urlRequest)
                    }

                    is WebContent.Data -> {
                        val baseUrl = content.baseUrl?.let { NSURL(string = it) }

                        wv.loadHTMLString(
                            content.data,
                            baseUrl
                        )
                    }

                    is WebContent.NavigatorOnly -> {
                        // NO-OP
                    }

                    else -> {}
                }
            }
        }
    }

    UIKitView(
        factory = {
            val config = WKWebViewConfiguration().apply {
                val webSettings = state.settings
                defaultWebpagePreferences.allowsContentJavaScript = webSettings.javaScriptEnabled
                preferences.javaScriptEnabled = webSettings.javaScriptEnabled
                preferences.javaScriptCanOpenWindowsAutomatically = webSettings.javaScriptCanOpenWindowsAutomatically

                // Configure viewport and content sizing settings similar to Android
                defaultWebpagePreferences.preferredContentMode = WKContentMode.WKContentModeRecommended

                // Apply iOS-specific settings
                val iosSettings = webSettings.iosSettings

                mediaTypesRequiringUserActionForPlayback = when (iosSettings.mediaTypesRequiringUserActionForPlayback) {
                    WebSettings.IosSettings.AudiovisualMediaType.None -> WKAudiovisualMediaTypeNone
                    WebSettings.IosSettings.AudiovisualMediaType.Audio -> WKAudiovisualMediaTypeAudio
                    WebSettings.IosSettings.AudiovisualMediaType.Video -> WKAudiovisualMediaTypeVideo
                    WebSettings.IosSettings.AudiovisualMediaType.All -> WKAudiovisualMediaTypeAll
                }
            }
            WKWebView(
                frame = CGRectZero.readValue(),
                configuration = config,
            ).apply {
                setAlpha(alpha.toDouble())
                // Configure back/forward gesture handling separately
                allowsBackForwardNavigationGestures = captureBackPresses

                applySettings(state.settings)
                
                // Setup JavaScript bridge if provided
                webViewJsBridge?.let { bridge ->
                    val iosHandler = com.mohamedrejeb.calf.ui.web.jsbridge.IOSJsBridgeHandler(bridge)
                    configuration.userContentController.addScriptMessageHandler(iosHandler, "iosJsBridge")
                    bridge.webViewState = state
                    state.webViewJsBridge = bridge
                }
                
                state.webView = this
                state.navigator = navigator
                navigationDelegate = state

                onCreated(this)
            }
        },
        update = {
            it.setAlpha(alpha.toDouble())
        },
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
        ),
        onRelease = {
            state.webView = null
            onDispose(it)
        },
        modifier = modifier,
    )
}

/**
 * A state holder to hold the state for the WebView. In most cases this will be remembered
 * using the rememberWebViewState(uri) function.
 */
@Stable
actual class WebViewState actual constructor(
    webContent: WebContent
): NSObject(), WKNavigationDelegateProtocol {
    
    internal var webViewJsBridge: com.mohamedrejeb.calf.ui.web.jsbridge.WebViewJsBridge? = null
    internal var navigator: WebViewNavigator? = null
    actual var lastLoadedUrl: String? by mutableStateOf(null)
        internal set

    /**
     *  The content being loaded by the WebView
     */
    actual var content: WebContent by mutableStateOf(webContent)

    /**
     * Whether the WebView is currently [LoadingState.Loading] data in its main frame (along with
     * progress) or the data loading has [LoadingState.Finished]. See [LoadingState]
     */
    actual var loadingState: LoadingState by mutableStateOf(LoadingState.Initializing)
        internal set

    /**
     * Whether the webview is currently loading data in its main frame
     */
    actual val isLoading: Boolean
        get() = loadingState !is LoadingState.Finished

    actual val errorsForCurrentRequest: SnapshotStateList<WebViewError> = mutableStateListOf()

    /**
     * The title received from the loaded content of the current page
     */
    actual var pageTitle: String? by mutableStateOf(null)
        internal set

    actual val settings: WebSettings = WebSettings(
        onSettingsChanged = {
            applySetting()
        }
    )

    private fun applySetting() {
        webView?.applySettings(settings)
    }

    actual fun evaluateJavascript(script: String, callback: ((String?) -> Unit)?) {
        webView?.evaluateJavaScript(script) { result, error ->
            if (error != null) {
                callback?.invoke(error.localizedDescription())
            } else {
                callback?.invoke(result?.toString())
            }
        }
    }

    // We need access to this in the state saver. An internal DisposableEffect or AndroidView
    // onDestroy is called after the state saver and so can't be used.
    var webView by mutableStateOf<WKWebView?>(null)
        internal set

    /**
     * Called when the web view begins to receive web content.
     */
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didStartProvisionalNavigation: WKNavigation?,
    ) {
        errorsForCurrentRequest.clear()
    }


    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        loadingState = LoadingState.Finished
        
        // Inject JavaScript bridge when page is finished loading
        webViewJsBridge?.let { bridge ->
            com.mohamedrejeb.calf.ui.web.jsbridge.JsBridgeInjector.injectJsBridge(this, bridge)
            
            // Inject iOS-specific bridge connection
            val iosScript = """
                window.${bridge.jsBridgeName}.postMessage = function (message) {
                    window.webkit.messageHandlers.iosJsBridge.postMessage(message);
                };
            """.trimIndent()
            
            com.mohamedrejeb.calf.ui.web.jsbridge.JsBridgeInjector.injectPlatformBridge(this, bridge, iosScript)
        }
    }

    /**
     * Called when the web view fails to load content.
     */
    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: WKNavigation?,
        withError: NSError,
    ) {
        errorsForCurrentRequest.add(
            WebViewError(
                code = withError.code.toInt(),
                description = withError.localizedDescription,
                // on iOS all errors are from the main frame
                isFromMainFrame = true,
            ),
        )
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didCommitNavigation: WKNavigation?) {
        loadingState = LoadingState.Loading(webView.estimatedProgress.toFloat())
    }

    private var isRedirect = false

    @OptIn(ExperimentalForeignApi::class)
    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit
    ) {
        val url = decidePolicyForNavigationAction.request.URL?.absoluteString
        
        // Intercept ALL navigation requests (including target="_blank") when interceptor is available
        if (url != null && !isRedirect && navigator?.requestInterceptor != null) {
            navigator?.requestInterceptor?.apply {
                val request = decidePolicyForNavigationAction.request
                val headerMap = mutableMapOf<String, String>()
                request.allHTTPHeaderFields?.forEach {
                    headerMap[it.key.toString()] = it.value.toString()
                }
                
                // Determine target type based on frame information
                val isMainFrame = decidePolicyForNavigationAction.targetFrame?.mainFrame ?: false
                
                val webRequest = com.mohamedrejeb.calf.ui.web.request.WebRequest(
                    url = request.URL?.absoluteString ?: "",
                    headers = headerMap,
                    isForMainFrame = isMainFrame,
                    isRedirect = isRedirect,
                    method = request.HTTPMethod ?: "GET"
                )
                
                val interceptResult = this.onInterceptUrlRequest(webRequest, navigator!!)
                
                when (interceptResult) {
                    is com.mohamedrejeb.calf.ui.web.request.WebRequestInterceptResult.Allow -> {
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
                    }
                    is com.mohamedrejeb.calf.ui.web.request.WebRequestInterceptResult.Reject -> {
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                    }
                    is com.mohamedrejeb.calf.ui.web.request.WebRequestInterceptResult.Modify -> {
                        isRedirect = true
                        interceptResult.request.apply {
                            navigator!!.stopLoading()
                            navigator!!.loadUrl(this.url, this.headers)
                        }
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                    }
                }
            }
        } else {
            isRedirect = false
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
        }
    }
}

private fun WKWebView.applySettings(webSettings: WebSettings) {
    configuration.defaultWebpagePreferences.allowsContentJavaScript = webSettings.javaScriptEnabled
    configuration.preferences.javaScriptEnabled = webSettings.javaScriptEnabled
    configuration.preferences.javaScriptCanOpenWindowsAutomatically = webSettings.javaScriptCanOpenWindowsAutomatically

    // Configure viewport and content sizing settings similar to Android
    configuration.defaultWebpagePreferences.preferredContentMode = WKContentMode.WKContentModeRecommended

    // Apply iOS-specific settings
    val iosSettings = webSettings.iosSettings

    configuration.mediaTypesRequiringUserActionForPlayback = when (iosSettings.mediaTypesRequiringUserActionForPlayback) {
        WebSettings.IosSettings.AudiovisualMediaType.None -> WKAudiovisualMediaTypeNone
        WebSettings.IosSettings.AudiovisualMediaType.Audio -> WKAudiovisualMediaTypeAudio
        WebSettings.IosSettings.AudiovisualMediaType.Video -> WKAudiovisualMediaTypeVideo
        WebSettings.IosSettings.AudiovisualMediaType.All -> WKAudiovisualMediaTypeAll
    }
    setInspectable(iosSettings.isInspectable)
    opaque = iosSettings.isOpaque

    scrollView.bounces = iosSettings.bounces
    scrollView.minimumZoomScale = iosSettings.minimumZoomScale
    scrollView.maximumZoomScale = iosSettings.maximumZoomScale
    scrollView.zoomScale = iosSettings.zoomScale
    scrollView.contentInsetAdjustmentBehavior = when (iosSettings.contentInsetAdjustmentBehavior) {
        WebSettings.IosSettings.ScrollViewContentInsetAdjustment.Never -> UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentNever
        WebSettings.IosSettings.ScrollViewContentInsetAdjustment.ScrollableAxes-> UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentScrollableAxes
        WebSettings.IosSettings.ScrollViewContentInsetAdjustment.Always -> UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentAlways
        WebSettings.IosSettings.ScrollViewContentInsetAdjustment.Automatic -> UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentAutomatic
    }
}

// Use Dispatchers.Main to ensure that the webview methods are called on UI thread
@OptIn(BetaInteropApi::class)
internal suspend fun WebViewNavigator.handleNavigationEvents(
    webView: WKWebView
): Nothing = withContext(Dispatchers.Main) {
    navigationEvents.collect { event ->
        when (event) {
            is WebViewNavigator.NavigationEvent.Back -> webView.goBack()
            is WebViewNavigator.NavigationEvent.Forward -> webView.goForward()
            is WebViewNavigator.NavigationEvent.Reload -> webView.reload()
            is WebViewNavigator.NavigationEvent.StopLoading -> webView.stopLoading()
            is WebViewNavigator.NavigationEvent.LoadHtml -> {
                val data = NSString
                    .create(string = event.html)
                    .dataUsingEncoding(NSUTF8StringEncoding) ?: return@collect
                val baseUrl =
                    if (event.baseUrl != null) NSURL(string = event.baseUrl)
                    else return@collect

                webView.loadData(
                    data,
                    event.mimeType ?: "text/html",
                    event.encoding ?: "utf-8",
                    baseUrl
                )
            }

            is WebViewNavigator.NavigationEvent.LoadUrl -> {
                loadUrl(event.url, event.additionalHttpHeaders)
            }
        }
    }
}

actual val WebStateSaver: Saver<WebViewState, Any> = run {
    val pageTitleKey = "pagetitle"
    val lastLoadedUrlKey = "lastloaded"

    mapSaver(
        save = {
            mapOf(
                pageTitleKey to it.pageTitle,
                lastLoadedUrlKey to it.lastLoadedUrl,
            )
        },
        restore = {
            WebViewState(WebContent.NavigatorOnly).apply {
                this.pageTitle = it[pageTitleKey] as String?
                this.lastLoadedUrl = it[lastLoadedUrlKey] as String?
            }
        }
    )
}
