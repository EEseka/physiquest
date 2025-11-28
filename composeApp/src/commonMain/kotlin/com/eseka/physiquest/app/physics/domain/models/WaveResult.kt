package com.eseka.physiquest.app.physics.domain.models

data class WaveResult(
    val wavelength: Double,
    val period: Double,
    val angularFrequency: Double,
    val waveNumber: Double,
    val phaseVelocity: Double
)