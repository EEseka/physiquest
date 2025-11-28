package com.eseka.physiquest.app.physics.data.repository

import com.eseka.physiquest.app.physics.domain.PhysicsCalculatorRepo
import com.eseka.physiquest.app.physics.domain.models.CircuitResult
import com.eseka.physiquest.app.physics.domain.models.ElectricityResult
import com.eseka.physiquest.app.physics.domain.models.EnergyResult
import com.eseka.physiquest.app.physics.domain.models.FieldLine
import com.eseka.physiquest.app.physics.domain.models.FluidResult
import com.eseka.physiquest.app.physics.domain.models.ForceVector
import com.eseka.physiquest.app.physics.domain.models.KinematicsResult
import com.eseka.physiquest.app.physics.domain.models.MagnetismResult
import com.eseka.physiquest.app.physics.domain.models.Point
import com.eseka.physiquest.app.physics.domain.models.ProjectileResult
import com.eseka.physiquest.app.physics.domain.models.RotationalResult
import com.eseka.physiquest.app.physics.domain.models.SHMResult
import com.eseka.physiquest.app.physics.domain.models.ThermodynamicsResult
import com.eseka.physiquest.app.physics.domain.models.WaveResult
import com.eseka.physiquest.app.physics.domain.utils.PhysicsConstants
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PhysicsCalculatorRepositoryImpl : PhysicsCalculatorRepo {

    override fun calculateProjectileMotion(
        v0: Double,
        angle: Double,
        height: Double
    ): ProjectileResult {
        val angleRad = angle * PI / 180.0
        val g = PhysicsConstants.GRAVITY_EARTH

        val vx = v0 * cos(angleRad)
        val vy = v0 * sin(angleRad)

        // Time to reach maximum height
        val timeToMaxHeight = vy / g

        // Maximum height
        val maxHeight = height + (vy * vy) / (2 * g)

        // Total flight time (accounting for initial height)
        val discriminant = vy * vy + 2 * g * height
        val flightTime = if (discriminant >= 0) {
            (vy + sqrt(discriminant)) / g
        } else {
            timeToMaxHeight * 2 // Fallback (should not happen with valid inputs)
        }

        // Range
        val range = vx * flightTime

        // Velocity at impact
        val vyAtImpact = vy - g * flightTime
        val velocityAtImpact = sqrt(vx * vx + vyAtImpact * vyAtImpact)

        // Generate trajectory points
        val trajectoryPoints = mutableListOf<Point>()
        val numPoints = 100
        for (i in 0..numPoints) {
            val t = (flightTime * i) / numPoints
            val x = vx * t
            val y = height + vy * t - 0.5 * g * t * t
            if (y >= 0) {
                trajectoryPoints.add(Point(x, y))
            }
        }

        return ProjectileResult(
            range = range,
            maxHeight = maxHeight,
            flightTime = flightTime,
            trajectoryPoints = trajectoryPoints,
            velocityAtImpact = velocityAtImpact,
            timeToMaxHeight = timeToMaxHeight
        )
    }

    override fun calculateSHM(
        amplitude: Double,
        frequency: Double,
        time: Double,
        phase: Double
    ): SHMResult {
        val angularFrequency = 2 * PI * frequency
        val period = 1.0 / frequency

        // Position: x(t) = A⋅cos(ωt + φ)
        val position = amplitude * cos(angularFrequency * time + phase)

        // Velocity: v(t) = -Aω⋅sin(ωt + φ)
        val velocity =
            -amplitude * angularFrequency * sin(angularFrequency * time + phase)

        // Acceleration: a(t) = -Aω²⋅cos(ωt + φ)
        val acceleration =
            -amplitude * angularFrequency * angularFrequency * cos(angularFrequency * time + phase)

        // Generate trajectory points
        val trajectoryPoints = mutableListOf<Point>()
        val numPoints = 100
        val totalTime = 2 * period  // 2 full cycles

        for (i in 0..numPoints) {
            val t = (totalTime * i) / numPoints
            val x = amplitude * cos(angularFrequency * t + phase)
            trajectoryPoints.add(Point(t, x))
        }

        return SHMResult(
            position = position,
            velocity = velocity,
            acceleration = acceleration,
            period = period,
            frequency = frequency,
            angularFrequency = angularFrequency,
            trajectoryPoints = trajectoryPoints
        )
    }

    override fun calculateCircuit(
        voltage: Double?,
        current: Double?,
        resistance: Double?
    ): CircuitResult {
        val v = when {
            voltage != null -> voltage
            current != null && resistance != null -> current * resistance // V = IR
            else -> null
        }

        val i = when {
            current != null -> current
            voltage != null && resistance != null -> voltage / resistance // I = V/R
            else -> null
        }

        val r = when {
            resistance != null -> resistance
            voltage != null && current != null -> voltage / current // R = V/I
            else -> null
        }

        val power = when {
            v != null && i != null -> v * i // P = VI
            v != null && r != null -> (v * v) / r // P = V²/R
            i != null && r != null -> i * i * r // P = I²R
            else -> 0.0
        }

        return CircuitResult(
            voltage = v,
            current = i,
            resistance = r,
            power = power
        )
    }

    override fun calculateWave(
        frequency: Double,
        wavelength: Double?,
        velocity: Double?
    ): WaveResult {
        val c = velocity ?: PhysicsConstants.SPEED_OF_LIGHT
        val lambda = wavelength ?: (c / frequency)
        val actualVelocity = velocity ?: (frequency * lambda)

        val period = 1.0 / frequency
        val angularFrequency = 2 * PI * frequency
        val waveNumber = 2 * PI / lambda

        return WaveResult(
            wavelength = lambda,
            period = period,
            angularFrequency = angularFrequency,
            waveNumber = waveNumber,
            phaseVelocity = actualVelocity
        )
    }

    override fun calculateKinematics(
        initialVelocity: Double?,
        finalVelocity: Double?,
        acceleration: Double?,
        time: Double?,
        displacement: Double?
    ): KinematicsResult {

        // Using kinematic equations to find missing values
        var u = initialVelocity
        var v = finalVelocity
        var a = acceleration
        var t = time
        var s = displacement

        // Try to solve using different kinematic equations
        // Equation 1: v = u + at
        if (u != null && a != null && t != null && v == null) {
            v = u + a * t
        }

        if (u != null && v != null && a != null && t == null && a != 0.0) {
            t = (v - u) / a
        }

        if (u != null && v != null && t != null && a == null && t != 0.0) {
            a = (v - u) / t
        }

        // Equation 2: s = ut + 0.5at²
        if (u != null && a != null && t != null && s == null) {
            s = u * t + 0.5 * a * t * t
        }

        if (u != null && s != null && t != null && a == null && t != 0.0) {
            a = 2 * (s - u * t) / (t * t)
        }

        // Equation 3: v² = u² + 2as
        if (u != null && v != null && a != null && s == null && a != 0.0) {
            s = (v * v - u * u) / (2 * a)
        }

        if (u != null && v != null && s != null && a == null && s != 0.0) {
            a = (v * v - u * u) / (2 * s)
        }

        // Equation 4: s = ((u + v)/2) * t
        if (u != null && v != null && t != null && s == null) {
            s = (u + v) / 2.0 * t
        }

        // Solve for u when missing
        if (v != null && a != null && t != null && u == null) {
            u = v - a * t
        }

        if (s != null && a != null && t != null && u == null && t != 0.0) {
            u = (s - 0.5 * a * t * t) / t
        }

        if (s != null && v != null && t != null && u == null && t != 0.0) {
            u = (2 * s / t) - v
        }

        if (s != null && v != null && a != null && u == null) {
            val discriminant = v * v - 2 * a * s
            if (discriminant >= 0) u = sqrt(discriminant)
        }

        // Solve for t via quadratic (s = ut + 0.5at²)
        if (u != null && a != null && s != null && t == null && a != 0.0) {
            val discriminant = u * u + 2 * a * s
            if (discriminant >= 0) {
                val t1 = (-u + sqrt(discriminant)) / a
                val t2 = (-u - sqrt(discriminant)) / a
                // choose positive root if valid
                t = listOf(t1, t2).filter { it >= 0 }.minOrNull()
            }
        }

        // Special case: if a = 0, then s = u * t
        if (a == 0.0 && u != null && s != null && t == null && u != 0.0) {
            t = s / u
        }

        // Generate trajectory points: displacement vs time
        val trajectoryPoints = mutableListOf<Point>()
        val numPoints = 100
        if (u != null && a != null && t != null) {
            for (i in 0..numPoints) {
                val timePoint = (t * i) / numPoints
                val displacementPoint = u * timePoint + 0.5 * a * timePoint * timePoint
                trajectoryPoints.add(Point(timePoint, displacementPoint))
            }
        }

        return KinematicsResult(
            initialVelocity = u,
            finalVelocity = v,
            acceleration = a,
            time = t,
            displacement = s,
            trajectoryPoints = trajectoryPoints
        )
    }

    override fun calculateEnergy(
        mass: Double?,
        velocity: Double?,
        height: Double?,
        force: Double?,
        distance: Double?
    ): EnergyResult {
        val kineticEnergy = if (mass != null && velocity != null) {
            0.5 * mass * velocity * velocity
        } else null

        val potentialEnergy = if (mass != null && height != null) {
            mass * PhysicsConstants.GRAVITY_EARTH * height
        } else null

        val workDone = if (force != null && distance != null) {
            force * distance
        } else null

        val totalEnergy = (kineticEnergy ?: 0.0) + (potentialEnergy ?: 0.0)

        return EnergyResult(
            kineticEnergy = kineticEnergy,
            potentialEnergy = potentialEnergy,
            totalEnergy = totalEnergy,
            workDone = workDone
        )
    }

    override fun calculateFluid(
        pressure: Double?,
        density: Double?,
        velocity: Double?,
        area: Double?,
        viscosity: Double?
    ): FluidResult {
        // Bernoulli's equation components
        val flowRate = if (velocity != null && area != null) {
            velocity * area
        } else null

        // Reynolds number calculation
        val reynoldsNumber = if (density != null && velocity != null && viscosity != null) {
            val characteristicLength =
                if (area != null) sqrt(area / PI) * 2 else 1.0
            (density * velocity * characteristicLength) / viscosity
        } else null

        // Generate Reynolds vs Velocity graph points
        val reynoldsGraphPoints = mutableListOf<Point>()
        if (density != null && viscosity != null) {
            val characteristicLength = if (area != null) sqrt(area / PI) * 2 else 1.0
            val maxVelocity = (velocity ?: 10.0).coerceAtLeast(10.0) // default upper range

            val numPoints = 100
            for (i in 0..numPoints) {
                val v = (maxVelocity * i) / numPoints
                val re = (density * v * characteristicLength) / viscosity
                reynoldsGraphPoints.add(Point(v, re))
            }
        }

        return FluidResult(
            pressure = pressure,
            density = density,
            velocity = velocity,
            flowRate = flowRate,
            reynoldsNumber = reynoldsNumber,
            trajectoryPoints = reynoldsGraphPoints
        )
    }

    override fun calculateRotationalMotion(
        torque: Double?,
        momentOfInertia: Double?,
        angularAcceleration: Double?,
        initialAngularVelocity: Double?,
        finalAngularVelocity: Double?,
        angularDisplacement: Double?,
        time: Double?,
        mass: Double?,
        radius: Double?
    ): RotationalResult {

        var τ = torque
        var I = momentOfInertia
        var α = angularAcceleration
        var ω_0 = initialAngularVelocity
        var ω = finalAngularVelocity
        var θ = angularDisplacement
        var t = time

        // Calculate moment of inertia if mass and radius provided
        if (I == null && mass != null && radius != null) {
            I = mass * radius * radius // point mass or cylinder approximation
        }

        // Equation 1: τ = I * α
        if (τ != null && I != null && α == null) α = τ / I
        if (I != null && α != null && τ == null) τ = I * α

        // Equation 2: ω = ω₀ + α * t
        if (α != null && t != null && ω_0 != null && ω == null) ω = ω_0 + α * t
        if (ω != null && α != null && ω_0 != null && t == null && α != 0.0) t = (ω - ω_0) / α
        if (ω != null && t != null && ω_0 != null && α == null && t != 0.0) α = (ω - ω_0) / t

        // Equation 3: θ = ω₀ * t + 0.5 * α * t²
        if (α != null && t != null && ω_0 != null && θ == null) θ = ω_0 * t + 0.5 * α * t * t

        // Equation 4: ω² = ω₀² + 2 * α * θ
        if (α != null && θ != null && ω_0 != null && ω == null) {
            val discriminant = ω_0 * ω_0 + 2 * α * θ
            if (discriminant >= 0) ω = sqrt(discriminant)
        }

        // Solve for ω₀ when missing
        if (ω != null && α != null && t != null && ω_0 == null) ω_0 = ω - α * t
        if (θ != null && α != null && t != null && ω_0 == null && t != 0.0) ω_0 =
            (θ - 0.5 * α * t * t) / t
        if (θ != null && ω != null && t != null && ω_0 == null && t != 0.0) ω_0 = (2 * θ / t) - ω
        if (θ != null && ω != null && α != null && ω_0 == null) {
            val discriminant = ω * ω - 2 * α * θ
            if (discriminant >= 0) ω_0 = sqrt(discriminant)
        }

        // Solve for t via quadratic (θ = ω₀ * t + 0.5 * α * t²)
        if (ω_0 != null && α != null && θ != null && t == null && α != 0.0) {
            val discriminant = ω_0 * ω_0 + 2 * α * θ
            if (discriminant >= 0) {
                val t1 = (-ω_0 + sqrt(discriminant)) / α
                val t2 = (-ω_0 - sqrt(discriminant)) / α
                t = listOf(t1, t2).filter { it >= 0 }.minOrNull()
            }
        }

        // Special case: α = 0 → uniform rotation
        if (α == 0.0 && ω_0 != null && θ != null && t == null && ω_0 != 0.0) {
            t = θ / ω_0
        }

        // Derived quantities
        val rotationalKE = if (I != null && ω != null) 0.5 * I * ω * ω else null
        val angularMomentum = if (I != null && ω != null) I * ω else null

        // Generate trajectory points: angular displacement vs time
        val trajectoryPoints = mutableListOf<Point>()
        val numPoints = 100
        if (ω_0 != null && α != null && t != null) {
            for (i in 0..numPoints) {
                val timePoint = (t * i) / numPoints
                val anglePoint = ω_0 * timePoint + 0.5 * α * timePoint * timePoint
                trajectoryPoints.add(Point(timePoint, anglePoint))
            }
        }

        return RotationalResult(
            angularVelocity = ω,
            angularAcceleration = α,
            torque = τ,
            momentOfInertia = I,
            angularDisplacement = θ,
            time = t,
            rotationalKineticEnergy = rotationalKE,
            angularMomentum = angularMomentum,
            trajectoryPoints = trajectoryPoints
        )
    }

    override fun calculateThermodynamics(
        pressure: Double?,
        volume: Double?,
        temperature: Double?,
        numberOfMoles: Double?,
        heatCapacity: Double?,
        deltaTemperature: Double?,
        workDone: Double?
    ): ThermodynamicsResult {

        var P = pressure
        var V = volume
        var T = temperature
        var n = numberOfMoles
        val R = PhysicsConstants.GAS_CONSTANT

        // Ideal Gas Law: PV = nRT
        if (P != null && V != null && n != null && T == null) T = (P * V) / (n * R)
        if (P != null && T != null && n != null && V == null) V = (n * R * T) / P
        if (V != null && T != null && n != null && P == null) P = (n * R * T) / V
        if (P != null && V != null && T != null && n == null) n = (P * V) / (R * T)
        // Heat transfer: Q = n⋅Cp⋅ΔT
        val heatTransfer = if (n != null && heatCapacity != null && deltaTemperature != null) {
            n * heatCapacity * deltaTemperature
        } else null

        // Work done (if user provided), else unknown due to lack of ΔV or process type
        val work = workDone

        // Internal energy change: ΔU = Q - W
        val internalEnergyChange = if (heatTransfer != null && work != null) {
            heatTransfer - work
        } else null

        // Entropy change: ΔS = Q / T (reversible process assumption)
        val entropy = if (heatTransfer != null && T != null && T > 0) {
            heatTransfer / T
        } else null

        // Efficiency cannot be calculated without Th and Tc
        val efficiency = null

        // Simulation points
        val temperatureTimePoints = mutableListOf<Point>()
        val pvDiagramPoints = mutableListOf<Point>()

        // Temperature vs time simulation
        if (T != null && deltaTemperature != null) {
            val numPoints = 50
            for (i in 0..numPoints) {
                val time = i.toDouble()
                val temp = T + (deltaTemperature * i / numPoints)
                temperatureTimePoints.add(Point(time, temp))
            }
        }

        // PV diagram (assuming isothermal process)
        if (n != null && T != null && V != null) {
            val numPoints = 50
            val startV = V * 0.5
            val endV = V * 2.0
            for (i in 0..numPoints) {
                val volumePoint = startV + (endV - startV) * i / numPoints
                val pressurePoint = (n * R * T) / volumePoint
                pvDiagramPoints.add(Point(pressurePoint, volumePoint))
            }
        }

        return ThermodynamicsResult(
            pressure = P,
            volume = V,
            temperature = T,
            numberOfMoles = n,
            heatTransfer = heatTransfer,
            work = work,
            internalEnergyChange = internalEnergyChange,
            entropy = entropy,
            efficiency = efficiency,
            temperatureTimePoints = temperatureTimePoints,
            pvDiagramPoints = pvDiagramPoints
        )
    }

    override fun calculateMagnetism(
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
    ): MagnetismResult {

        var B = magneticField
        var I = current
        var v = velocity
        var q = charge
        val μ = PhysicsConstants.PERMEABILITY_VACUUM
        val θ = angle?.times(PI)?.div(180.0) ?: (PI / 2.0) // default 90°
        val m = mass ?: PhysicsConstants.MASS_ELECTRON
        val rDefault = smallRadius ?: 0.01

        // Magnetic Force: F = qvBsinθ or F = BILsinθ
        var F: Double? = null
        if (q != null && v != null && B != null) F = abs(q * v * B * sin(θ))
        if (I != null && length != null && B != null) F = I * length * B * sin(θ)

        // Solve for missing variables

        // Solve for B
        if (B == null) {
            if (F != null && q != null && v != null && sin(θ) != 0.0) B = F / (q * v * sin(θ))
            else if (F != null && I != null && length != null && sin(θ) != 0.0) B =
                F / (I * length * sin(θ))
            else if (I != null) B = (μ * I) / (2 * PI * rDefault)
        }

        // Solve for I
        if (I == null && B != null) {
            I = (B * 2 * PI * rDefault) / μ
        }

        // Solve for F if missing
        if (F == null && B != null) {
            if (q != null && v != null) F = abs(q * v * B * sin(θ))
            else if (I != null && length != null) F = I * length * B * sin(θ)
        }

        // Solve for radius of circular motion: r = mv/(qB)
        val rMotion = if (rDefault != 0.0) {
            if (charge != null && v != null && B != null) (m * v) / (abs(charge) * B)
            else null
        } else null

        // Solve for magnetic flux: Φ = B*A*cosθ
        val flux = if (B != null && area != null) B * area * cos(θ) else null

        // Solve for inductance: L = μ₀ * n² * A * l
        val L = if (numberOfTurns != null && area != null && length != null) {
            val n = numberOfTurns.toDouble() / length
            μ * n * n * area * length
        } else null

        // Visualization: field lines
        val fieldLines = mutableListOf<FieldLine>()
        if (B != null) {
            val numLines = 8
            for (i in 0 until numLines) {
                val angleRad = (2 * PI * i) / numLines
                val startPoint = Point(0.1 * cos(angleRad), 0.1 * sin(angleRad))
                val endPoint = Point(1.0 * cos(angleRad), 1.0 * sin(angleRad))
                fieldLines.add(FieldLine(startPoint, endPoint, B))
            }
        }

        // Particle circular trajectory
        val particleTrajectory = mutableListOf<Point>()
        if (rMotion != null && v != null && charge != null && B != null) {
            val ω = abs(charge * B) / m
            val numPoints = 100
            for (i in 0..numPoints) {
                val t = (2 * PI * i) / (numPoints * ω)
                val x = rMotion * cos(ω * t)
                val y = rMotion * sin(ω * t)
                particleTrajectory.add(Point(x, y))
            }
        }

        return MagnetismResult(
            magneticField = B,
            magneticForce = F,
            current = I,
            velocity = v,
            charge = q,
            radius = rMotion,
            magneticFlux = flux,
            inductance = L,
            fieldLines = fieldLines,
            particleTrajectory = particleTrajectory
        )
    }

    override fun calculateElectricity(
        charge1: Double?,
        charge2: Double?,
        distance: Double?,
        electricField: Double?,
        potential: Double?,
        capacitance: Double?,
        voltage: Double?,
        area: Double?
    ): ElectricityResult {

        var q1 = charge1
        var q2 = charge2
        var r = distance
        var E = electricField
        var V = potential
        var C = capacitance
        var U = voltage
        var A = area
        val k = PhysicsConstants.COULOMB_CONSTANT

        // Electric Force
        // F = k*q1*q2 / r² or F = qE
        var electricForce: Double? = null
        if (q1 != null && q2 != null && r != null && r != 0.0) {
            electricForce = k * abs(q1 * q2) / (r * r)
        } else if (q1 != null && E != null) {
            electricForce = abs(q1 * E)
        }

        // Solve for Electric Field
        // E = k*q/r² or E = F/q or E = V/r
        if (E == null) {
            if (q1 != null && r != null && r != 0.0) E = k * abs(q1) / (r * r)
            else if (electricForce != null && q1 != null && q1 != 0.0) E = electricForce / abs(q1)
            else if (V != null && r != null && r != 0.0) E = abs(V / r)
        }

        // Solve for Potential
        // V = k*q/r or V = E*r
        if (V == null) {
            if (q1 != null && r != null && r != 0.0) V = k * q1 / r
            else if (E != null && r != null) V = E * r
        }

        // Solve for distance r
        // r = sqrt(k*q1*q2 / F) or r = k*q1 / V
        if (r == null) {
            if (q1 != null && q2 != null && electricForce != null && electricForce != 0.0)
                r = sqrt(k * abs(q1 * q2) / electricForce)
            else if (q1 != null && V != null && V != 0.0) r = k * abs(q1) / V
            else if (E != null && V != null && E != 0.0) r = V / E
        }

        // Capacitance energy
        // U = 0.5*C*V²
        val storedEnergy = if (C != null && U != null) 0.5 * C * U * U else null

        // Electric flux
        // Φ = E*A
        val electricFlux = if (E != null && A != null) E * A else null

        // Visualization: field lines
        val electricFieldLines = mutableListOf<FieldLine>()
        if (E != null && q1 != null) {
            val numLines = 12
            val sign = if (q1 > 0) 1 else -1
            for (i in 0 until numLines) {
                val angle = (2 * PI * i) / numLines
                val startR = 0.1
                val endR = if (sign > 0) 2.0 else 0.05
                val startPoint = Point(startR * cos(angle), startR * sin(angle))
                val endPoint = Point(endR * cos(angle), endR * sin(angle))
                electricFieldLines.add(FieldLine(startPoint, endPoint, E))
            }
        }

        // Visualization: equipotential lines
        val equipotentialLines = mutableListOf<List<Point>>()
        if (V != null && r != null) {
            val numLines = 5
            for (i in 1..numLines) {
                val equipotentialR = r * i / numLines
                val points = mutableListOf<Point>()
                val numPoints = 50
                for (j in 0..numPoints) {
                    val angle = (2 * PI * j) / numPoints
                    points.add(Point(equipotentialR * cos(angle), equipotentialR * sin(angle)))
                }
                equipotentialLines.add(points)
            }
        }

        // Visualization: force vectors
        val forceVectors = mutableListOf<ForceVector>()
        if (electricForce != null && E != null) {
            val numVectors = 16
            for (i in 0 until numVectors) {
                val angle = (2 * PI * i) / numVectors
                val position = Point(cos(angle), sin(angle))
                val forceDirection = Point(cos(angle), sin(angle))
                forceVectors.add(ForceVector(position, forceDirection, E))
            }
        }

        return ElectricityResult(
            electricField = E,
            electricPotential = V,
            electricForce = electricForce,
            charge = q1,
            distance = r,
            capacitance = C,
            storedEnergy = storedEnergy,
            electricFlux = electricFlux,
            electricFieldLines = electricFieldLines,
            equipotentialLines = equipotentialLines,
            forceVectors = forceVectors
        )
    }
}