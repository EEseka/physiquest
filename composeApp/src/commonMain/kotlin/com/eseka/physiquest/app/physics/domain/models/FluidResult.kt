package com.eseka.physiquest.app.physics.domain.models

data class FluidResult(
    val pressure: Double?,
    val density: Double?,
    val velocity: Double?,
    val flowRate: Double?,
    val reynoldsNumber: Double?,
    val trajectoryPoints: List<Point>
)