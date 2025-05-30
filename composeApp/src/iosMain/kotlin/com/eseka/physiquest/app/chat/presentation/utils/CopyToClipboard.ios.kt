package com.eseka.physiquest.app.chat.presentation.utils

import androidx.compose.runtime.Composable
import platform.UIKit.UIPasteboard

@Composable
actual fun SetClipboardText(text: String) {
    UIPasteboard.generalPasteboard.string = text
}