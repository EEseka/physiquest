package com.eseka.physiquest.app.chat.domain.utils

import java.util.Locale

actual fun getCurrentLanguage(): String =
    Locale.getDefault().language.ifEmpty { "en" }