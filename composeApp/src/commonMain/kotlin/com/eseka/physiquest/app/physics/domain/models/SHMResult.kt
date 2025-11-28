package com.eseka.physiquest.app.physics.domain.models

data class SHMResult(
    val position: Double,
    val velocity: Double,
    val acceleration: Double,
    val period: Double,
    val frequency: Double,
    val angularFrequency: Double,
    val trajectoryPoints: List<Point>
)
