package com.eseka.physiquest.app.chat.presentation.utils

import com.multiplatform.webview.web.NativeWebView

// In commonMain/your/package/PlatformWebViewUtils.kt


/**
 * Declares a common function to make the native WebView background transparent.
 * The actual implementation will be provided by each platform.
 */
expect fun NativeWebView.setTransparent()