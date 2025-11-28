package com.eseka.physiquest.app.chat.presentation.utils

import androidx.compose.ui.graphics.Color

fun Color.toHex(): String {
    val red = (red * 255).toInt().toString(16).padStart(2, '0')
    val green = (green * 255).toInt().toString(16).padStart(2, '0')
    val blue = (blue * 255).toInt().toString(16).padStart(2, '0')
    return "#$red$green$blue"
}