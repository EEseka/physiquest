package com.eseka.physiquest.app.physics.domain.models

data class ProjectileResult(
    val range: Double,
    val maxHeight: Double,
    val flightTime: Double,
    val velocityAtImpact: Double,
    val timeToMaxHeight: Double,
    val trajectoryPoints: List<Point>
)