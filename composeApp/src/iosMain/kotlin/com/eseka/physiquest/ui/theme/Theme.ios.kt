package com.eseka.physiquest.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * iOS implementation of PlatformInfo
 */
actual class PlatformInfo {
    // Dynamic color is not supported on iOS through Compose
    actual val isDynamicColorSupported: Boolean = false
}

/**
 * Get platform-specific information for iOS
 */
@Composable
actual fun getPlatformInfo(): PlatformInfo = PlatformInfo()

/**
 * Get dynamic color scheme for iOS if available (always returns null)
 */
@Composable
actual fun getDynamicColorScheme(darkTheme: Boolean): ColorScheme? = null