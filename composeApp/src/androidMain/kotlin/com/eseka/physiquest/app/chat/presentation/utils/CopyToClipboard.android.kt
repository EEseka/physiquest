package com.eseka.physiquest.app.chat.presentation.utils

import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard

@Composable
actual fun SetClipboardText(text: String) {
    val clipboardManager = LocalClipboard.current
    LaunchedEffect(Unit) {
        clipboardManager.setClipEntry(
            ClipEntry(
                ClipData.newPlainText(
                    "",
                    text
                )
            )
        )
    }
}