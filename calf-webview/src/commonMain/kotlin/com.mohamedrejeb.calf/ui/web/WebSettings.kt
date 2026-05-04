package com.mohamedrejeb.calf.ui.web

class WebSettings internal constructor(
    private val onSettingsChanged: () -> Unit
) {
    var javaScriptEnabled: Boolean = false
        set(value) {
            field = value
            onSettingsChanged()
        }

    var javaScriptCanOpenWindowsAutomatically = true
        set(value) {
            field = value
            onSettingsChanged()
        }

    val androidSettings = AndroidSettings(onSettingsChanged)

    val iosSettings = IosSettings(onSettingsChanged)

    class AndroidSettings internal constructor(
        private val onSettingsChanged: () -> Unit
    ) {
        var supportZoom = false
            set(value) {
                field = value
                onSettingsChanged()
            }

        var textZoom = 100
            set(value) {
                field = value
                onSettingsChanged()
            }

        var allowContentAccess = true
            set(value) {
                field = value
                onSettingsChanged()
            }

        var allowFileAccess = true
            set(value) {
                field = value
                onSettingsChanged()
            }

        var blockNetworkImage = false
            set(value) {
                field = value
                onSettingsChanged()
            }

        var blockNetworkLoads = false
            set(value) {
                field = value
                onSettingsChanged()
            }

        var databaseEnabled = true
            set(value) {
                field = value
                onSettingsChanged()
            }

        var domStorageEnabled = true
            set(value) {
                field = value
                onSettingsChanged()
            }

        var loadsImagesAutomatically = true
            set(value) {
                field = value
                onSettingsChanged()
            }

        var useWideViewPort = true
            set(value) {
                field = value
                onSettingsChanged()
            }

        var loadWithOverviewMode = true
            set(value) {
                field = value
                onSettingsChanged()
            }

        var mediaPlaybackRequiresUserGesture = false
            set(value) {
                field = value
                onSettingsChanged()
            }

        var supportMultipleWindows = true
            set(value) {
                field = value
                onSettingsChanged()
            }

        var builtInZoomControls = true
            set(value) {
                field = value
                onSettingsChanged()
            }

        var displayZoomControls = false
            set(value) {
                field = value
                onSettingsChanged()
            }

        var setGeolocationEnabled = true
            set(value) {
                field = value
                onSettingsChanged()
            }

        var isAlgorithmicDarkeningAllowed = true
            set(value) {
                field = value
                onSettingsChanged()
            }

        var safeBrowsingEnabled = true
            set(value) {
                field = value
                onSettingsChanged()
            }
    }

    class IosSettings internal constructor(
        private val onSettingsChanged: () -> Unit
    ) {
        enum class AudiovisualMediaType {
            None, Audio, Video, All
        }

        enum class ScrollViewContentInsetAdjustment {
            Never, ScrollableAxes, Always, Automatic
        }

        var mediaTypesRequiringUserActionForPlayback = AudiovisualMediaType.None
            set(value) {
                field = value
                onSettingsChanged()
            }

        var allowsInlineMediaPlayback = false
            set(value) {
                field = value
                onSettingsChanged()
            }

        var isInspectable = false
            set(value) {
                field = value
                onSettingsChanged()
            }

        var isOpaque = true
            set(value) {
                field = value
                onSettingsChanged()
            }

        var bounces: Boolean = true
            set(value) {
                field = value
                onSettingsChanged()
            }

        var minimumZoomScale = 1.0
            set(value) {
                field = value
                onSettingsChanged()
            }

        var maximumZoomScale = 1.0
            set(value) {
                field = value
                onSettingsChanged()
            }

        var zoomScale = 1.0
            set(value) {
                field = value
                onSettingsChanged()
            }

        var contentInsetAdjustmentBehavior = ScrollViewContentInsetAdjustment.Never
            set(value) {
                field = value
                onSettingsChanged()
            }
    }
}
