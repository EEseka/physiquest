package com.eseka.physiquest.app.physics.domain.models

data class ElectricityResult(
    val electricField: Double?,
    val electricPotential: Double?,
    val electricForce: Double?,
    val charge: Double?,
    val distance: Double?,
    val capacitance: Double?,
    val storedEnergy: Double?,
    val electricFlux: Double?,
    // For simulation - electric field lines, equipotential lines
    val electricFieldLines: List<FieldLine>,
    val equipotentialLines: List<List<Point>>,
    val forceVectors: List<ForceVector>
)

data class ForceVector(
    val position: Point,
    val forceDirection: Point, // normalized vector
    val magnitude: Double
)