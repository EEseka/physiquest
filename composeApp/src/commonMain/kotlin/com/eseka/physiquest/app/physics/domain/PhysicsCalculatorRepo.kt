package com.eseka.physiquest.app.physics.domain

import com.eseka.physiquest.app.physics.domain.models.CircuitResult
import com.eseka.physiquest.app.physics.domain.models.ElectricityResult
import com.eseka.physiquest.app.physics.domain.models.EnergyResult
import com.eseka.physiquest.app.physics.domain.models.FluidResult
import com.eseka.physiquest.app.physics.domain.models.KinematicsResult
import com.eseka.physiquest.app.physics.domain.models.MagnetismResult
import com.eseka.physiquest.app.physics.domain.models.ProjectileResult
import com.eseka.physiquest.app.physics.domain.models.RotationalResult
import com.eseka.physiquest.app.physics.domain.models.SHMResult
import com.eseka.physiquest.app.physics.domain.models.ThermodynamicsResult
import com.eseka.physiquest.app.physics.domain.models.WaveResult

interface PhysicsCalculatorRepo {
    fun calculateProjectileMotion(v0: Double, angle: Double, height: Double = 0.0): ProjectileResult
    fun calculateSHM(
        amplitude: Double,
        frequency: Double,
        time: Double,
        phase: Double = 0.0
    ): SHMResult

    fun calculateCircuit(voltage: Double?, current: Double?, resistance: Double?): CircuitResult
    fun calculateWave(frequency: Double, wavelength: Double?, velocity: Double?): WaveResult
    fun calculateKinematics(
        initialVelocity: Double?,
        finalVelocity: Double?,
        acceleration: Double?,
        time: Double?,
        displacement: Double?
    ): KinematicsResult

    fun calculateEnergy(
        mass: Double?,
        velocity: Double?,
        height: Double?,
        force: Double?,
        distance: Double?
    ): EnergyResult

    fun calculateFluid(
        pressure: Double?,
        density: Double?,
        velocity: Double?,
        area: Double?,
        viscosity: Double?
    ): FluidResult

    fun calculateRotationalMotion(
        torque: Double?,
        momentOfInertia: Double?,
        angularAcceleration: Double?,
        initialAngularVelocity: Double?,
        finalAngularVelocity: Double?,
        angularDisplacement: Double?,
        time: Double?,
        mass: Double?,
        radius: Double?
    ): RotationalResult

    fun calculateThermodynamics(
        pressure: Double?,
        volume: Double?,
        temperature: Double?,
        numberOfMoles: Double?,
        heatCapacity: Double?,
        deltaTemperature: Double?,
        workDone: Double?
    ): ThermodynamicsResult

    fun calculateMagnetism(
        magneticField: Double?,
        current: Double?,
        velocity: Double?,
        charge: Double?,
        length: Double?,
        area: Double?,
        numberOfTurns: Int?,
        angle: Double?,
        smallRadius: Double?,
        mass: Double?
    ): MagnetismResult

    fun calculateElectricity(
        charge1: Double?,
        charge2: Double?,
        distance: Double?,
        electricField: Double?,
        potential: Double?,
        capacitance: Double?,
        voltage: Double?,
        area: Double?
    ): ElectricityResult
}