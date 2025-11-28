package com.eseka.physiquest.app.physics.domain.models

data class EnergyResult(
    val kineticEnergy: Double?,
    val potentialEnergy: Double?,
    val totalEnergy: Double,
    val workDone: Double?
)