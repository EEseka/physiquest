package com.eseka.physiquest.app.physics.domain.models

data class MagnetismResult(
    val magneticField: Double?,
    val magneticForce: Double?,
    val current: Double?,
    val velocity: Double?,
    val charge: Double?,
    val radius: Double?,
    val magneticFlux: Double?,
    val inductance: Double?,
    // For simulation - field lines, particle motion in magnetic field
    val fieldLines: List<FieldLine>,
    val particleTrajectory: List<Point>
)