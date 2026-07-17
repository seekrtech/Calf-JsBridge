package com.mohamedrejeb.calf.sample.screens

import androidx.compose.runtime.Composable

@Composable
actual fun WebViewScreen(
    navigateBack: () -> Unit
) {
    WebViewNotSupportedScreen(navigateBack)
} 