package com.eseka.physiquest.app.physics.presentation.utils

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSString
import platform.Foundation.create

@OptIn(BetaInteropApi::class)
actual fun Double.formatDecimal(digits: Int): String {
    return if (kotlin.math.abs(this) >= 1e6 || kotlin.math.abs(this) <= 1e-3) {
        val scientificStr =
            NSString.create(format = "%.${digits}e", args = arrayOf(this)).toString()
        convertToReadableScientific(scientificStr)
    } else {
        NSString.create(format = "%.${digits}f", args = arrayOf(this)).toString()
    }
}

private fun convertToReadableScientific(scientificStr: String): String {
    val parts = scientificStr.split("e", "E")
    if (parts.size != 2) return scientificStr

    val coefficient = parts[0]
    val exponent = parts[1].toIntOrNull() ?: return scientificStr

    return "$coefficient × 10^$exponent"
}