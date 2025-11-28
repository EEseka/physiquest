package com.eseka.physiquest.app.physics.presentation

import com.eseka.physiquest.app.physics.presentation.utils.CalculationType

sealed class PhysicsCalculatorEvents {

    // General Events
    data class OnCalculationTypeSelected(val type: CalculationType) : PhysicsCalculatorEvents()
    data object OnClearResults : PhysicsCalculatorEvents()
    data class OnSaveCalculation(val calculationName: String) : PhysicsCalculatorEvents()
    data object OnLoadSavedCalculations : PhysicsCalculatorEvents()
    data class OnDeleteSavedCalculation(val calculationId: String) : PhysicsCalculatorEvents()

    // Projectile Motion Events
    data class OnProjectileMotionCalculate(
        val initialVelocity: Double?,
        val angle: Double?,
        val height: Double?
    ) : PhysicsCalculatorEvents()

    // Simple Harmonic Motion Events
    data class OnSHMCalculate(
        val amplitude: Double?,
        val frequency: Double?,
        val time: Double?,
        val phase: Double?
    ) : PhysicsCalculatorEvents()

    // Circuit Analysis Events
    data class OnCircuitCalculate(
        val voltage: Double?,
        val current: Double?,
        val resistance: Double?
    ) : PhysicsCalculatorEvents()

    // Wave Physics Events
    data class OnWaveCalculate(
        val frequency: Double?,
        val wavelength: Double?,
        val velocity: Double?
    ) : PhysicsCalculatorEvents()

    // Kinematics Events
    data class OnKinematicsCalculate(
        val initialVelocity: Double?,
        val finalVelocity: Double?,
        val acceleration: Double?,
        val time: Double?,
        val displacement: Double?
    ) : PhysicsCalculatorEvents()

    // Energy Calculations Events
    data class OnEnergyCalculate(
        val mass: Double?,
        val velocity: Double?,
        val height: Double?,
        val force: Double?,
        val distance: Double?
    ) : PhysicsCalculatorEvents()

    // Fluid Mechanics Events
    data class OnFluidCalculate(
        val pressure: Double?,
        val density: Double?,
        val velocity: Double?,
        val area: Double?,
        val viscosity: Double?
    ) : PhysicsCalculatorEvents()

    // Rotational Motion Events
    data class OnRotationalCalculate(
        val torque: Double?,
        val momentOfInertia: Double?,
        val angularAcceleration: Double?,
        val initialAngularVelocity: Double?,
        val finalAngularVelocity: Double?,
        val angularDisplacement: Double?,
        val time: Double?,
        val mass: Double?,
        val radius: Double?
    ) : PhysicsCalculatorEvents()

    // Thermodynamics Events
    data class OnThermodynamicsCalculate(
        val pressure: Double?,
        val volume: Double?,
        val temperature: Double?,
        val numberOfMoles: Double?,
        val heatCapacity: Double?,
        val deltaTemperature: Double?,
        val workDone: Double?
    ) : PhysicsCalculatorEvents()

    // Magnetism Events
    data class OnMagnetismCalculate(
        val magneticField: Double?,
        val current: Double?,
        val velocity: Double?,
        val charge: Double?,
        val length: Double?,
        val area: Double?,
        val numberOfTurns: Int?,
        val angle: Double?,
        val smallRadius: Double?,
        val mass: Double?
    ) : PhysicsCalculatorEvents()

    // Electricity Events
    data class OnElectricityCalculate(
        val charge1: Double?,
        val charge2: Double?,
        val distance: Double?,
        val electricField: Double?,
        val potential: Double?,
        val capacitance: Double?,
        val voltage: Double?,
        val area: Double?
    ) : PhysicsCalculatorEvents()
}