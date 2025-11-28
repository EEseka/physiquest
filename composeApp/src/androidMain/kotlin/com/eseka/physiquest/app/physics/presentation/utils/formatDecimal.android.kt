package com.eseka.physiquest.app.physics.presentation.utils

actual fun Double.formatDecimal(digits: Int): String {
    return if (kotlin.math.abs(this) >= 1e6 || kotlin.math.abs(this) <= 1e-3) {
        val scientificStr = String.format("%.${digits}e", this)
        convertToReadableScientific(scientificStr)
    } else {
        String.format("%.${digits}f", this)
    }
}

private fun convertToReadableScientific(scientificStr: String): String {
    val parts = scientificStr.split("e", "E")
    if (parts.size != 2) return scientificStr

    val coefficient = parts[0]
    val exponent = parts[1].toIntOrNull() ?: return scientificStr

    return "$coefficient × 10^$exponent"
}