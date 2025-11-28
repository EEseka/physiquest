package com.eseka.physiquest.app.chat.presentation.utils

import com.multiplatform.webview.web.NativeWebView
import platform.UIKit.UIColor

actual fun NativeWebView.setTransparent() {
    // 1. Make the WebView itself transparent.
    this.opaque = false
    this.backgroundColor = UIColor.clearColor

    // 2. Make the scroll view INSIDE the WebView transparent.
    // This is the key to removing the white/black background.
    this.scrollView.backgroundColor = UIColor.clearColor
}