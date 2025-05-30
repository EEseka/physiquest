package com.eseka.physiquest.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Android implementation of PlatformInfo
 */
actual class PlatformInfo {
    // Dynamic color is supported on Android 12+ (API level 31+)
    actual val isDynamicColorSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

/**
 * Get platform-specific information for Android
 */
@Composable
actual fun getPlatformInfo(): PlatformInfo = PlatformInfo()

/**
 * Get dynamic color scheme for Android if available
 */
@Composable
actual fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme? {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } else {
        null
    }
}