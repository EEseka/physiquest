package com.eseka.physiquest.app.physics.domain.models

data class RotationalResult(
    val angularVelocity: Double?,
    val angularAcceleration: Double?,
    val torque: Double?,
    val momentOfInertia: Double?,
    val angularDisplacement: Double?,
    val time: Double?,
    val rotationalKineticEnergy: Double?,
    val angularMomentum: Double?,
    val trajectoryPoints: List<Point>
)