package com.eseka.physiquest.app.physics.domain.models

data class KinematicsResult(
    val finalVelocity: Double?,
    val displacement: Double?,
    val time: Double?,
    val acceleration: Double?,
    val initialVelocity: Double?,
    val trajectoryPoints: List<Point>
)