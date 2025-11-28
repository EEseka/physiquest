package com.eseka.physiquest.app.chat.presentation.utils

import com.multiplatform.webview.web.NativeWebView

actual fun NativeWebView.setTransparent() {
    this.setBackgroundColor(android.graphics.Color.TRANSPARENT)
}