package com.eseka.physiquest.app.chat.domain.utils

// iosMain
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual fun getCurrentLanguage(): String =
    NSLocale.currentLocale.languageCode.ifEmpty { "en" }