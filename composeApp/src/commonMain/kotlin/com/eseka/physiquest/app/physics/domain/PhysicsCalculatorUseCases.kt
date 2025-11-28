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

class CalculateProjectileMotionUseCase(
    private val repository: PhysicsCalculatorRepo
) {
    operator fun invoke(velocity: Double?, angle: Double?, height: Double?): ProjectileResult {
        requireNotNull(velocity) { "Initial velocity is required" }
        requireNotNull(angle) { "Launch angle is required" }

        require(velocity > 0) { "Initial velocity must be positive" }
        require(angle in 0.0..90.0) { "Angle must be between 0 and 90 degrees" }
        require((height ?: 0.0) >= 0) { "Height must be non-negative" }

        return repository.calculateProjectileMotion(velocity, angle, height ?: 0.0)
    }
}

class CalculateSHMUseCase(
    private val repository: PhysicsCalculatorRepo
) {
    operator fun invoke(
        amplitude: Double?,
        frequency: Double?,
        time: Double?,
        phase: Double?
    ): SHMResult {
        requireNotNull(amplitude) { "Amplitude is required" }
        requireNotNull(frequency) { "Frequency is required" }
        requireNotNull(time) { "Time is required" }

        require(amplitude > 0) { "Amplitude must be positive" }
        require(frequency > 0) { "Frequency must be positive" }
        require(time >= 0) { "Time must be non-negative" }

        return repository.calculateSHM(amplitude, frequency, time, phase ?: 0.0)
    }
}

class CalculateCircuitUseCase(
    private val repository: PhysicsCalculatorRepo
) {
    operator fun invoke(voltage: Double?, current: Double?, resistance: Double?): CircuitResult {
        val nonNullCount = listOfNotNull(voltage, current, resistance).size
        require(nonNullCount >= 2) { "At least two values must be provided" }

        voltage?.let { require(it >= 0) { "Voltage must be non-negative" } }
        current?.let { require(it >= 0) { "Current must be non-negative" } }
        resistance?.let { require(it > 0) { "Resistance must be positive" } }

        return repository.calculateCircuit(voltage, current, resistance)
    }
}

class CalculateWaveUseCase(
    private val repository: PhysicsCalculatorRepo
) {
    operator fun invoke(frequency: Double?, wavelength: Double?, velocity: Double?): WaveResult {
        requireNotNull(frequency) { "Frequency is required" }
        require(frequency > 0) { "Frequency must be positive" }

        wavelength?.let { require(it > 0) { "Wavelength must be positive" } }
        velocity?.let { require(it > 0) { "Velocity must be positive" } }

        return repository.calculateWave(frequency, wavelength, velocity)
    }
}

class CalculateKinematicsUseCase(
    private val repository: PhysicsCalculatorRepo
) {
    operator fun invoke(
        initialVelocity: Double?,
        finalVelocity: Double?,
        acceleration: Double?,
        time: Double?,
        displacement: Double?
    ): KinematicsResult {
        val providedValues =
            listOfNotNull(initialVelocity, finalVelocity, acceleration, time, displacement)
        require(providedValues.size >= 3) { "At least three values must be provided" }

        time?.let { require(it >= 0) { "Time must be non-negative" } }

        return repository.calculateKinematics(
            initialVelocity, finalVelocity, acceleration, time, displacement
        )
    }
}

class CalculateEnergyUseCase(
    private val repository: PhysicsCalculatorRepo
) {
    operator fun invoke(
        mass: Double?,
        velocity: Double?,
        height: Double?,
        force: Double?,
        distance: Double?
    ): EnergyResult {
        // At least one pair of values should be provided for meaningful calculation
        val hasKineticInputs = mass != null && velocity != null
        val hasPotentialInputs = mass != null && height != null
        val hasWorkInputs = force != null && distance != null

        require(hasKineticInputs || hasPotentialInputs || hasWorkInputs) {
            "At least one complete energy calculation requires valid inputs"
        }

        mass?.let { require(it > 0) { "Mass must be positive" } }
        velocity?.let { require(it >= 0) { "Velocity must be non-negative" } }
        height?.let { require(it >= 0) { "Height must be non-negative" } }
        distance?.let { require(it >= 0) { "Distance must be non-negative" } }

        return repository.calculateEnergy(mass, velocity, height, force, distance)
    }
}

class CalculateFluidUseCase(
    private val repository: PhysicsCalculatorRepo
) {
    operator fun invoke(
        pressure: Double?,
        density: Double?,
        velocity: Double?,
        area: Double?,
        viscosity: Double?
    ): FluidResult {
        val providedValues = listOfNotNull(pressure, density, velocity, area, viscosity)
        require(providedValues.size >= 2) {
            "At least two input values must be provided to compute fluid properties."
        }

        pressure?.let { require(it >= 0) { "Pressure must be non-negative" } }
        density?.let { require(it > 0) { "Density must be positive" } }
        velocity?.let { require(it >= 0) { "Velocity must be non-negative" } }
        area?.let { require(it > 0) { "Area must be positive" } }
        viscosity?.let { require(it > 0) { "Viscosity must be positive" } }

        return repository.calculateFluid(pressure, density, velocity, area, viscosity)
    }
}

class CalculateRotationalMotionUseCase(
    private val repository: PhysicsCalculatorRepo
) {
    operator fun invoke(
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
        val providedValues = listOfNotNull(
            torque, momentOfInertia, angularAcceleration,
            initialAngularVelocity, finalAngularVelocity,
            angularDisplacement, time, mass, radius
        )
        require(providedValues.size >= 3) {
            "At least three values must be provided to compute rotational motion."
        }
        momentOfInertia?.let { require(it > 0) { "Moment of inertia must be positive" } }
        time?.let { require(it >= 0) { "Time must be non-negative" } }
        mass?.let { require(it > 0) { "Mass must be positive" } }
        radius?.let { require(it > 0) { "Radius must be positive" } }

        return repository.calculateRotationalMotion(
            torque,
            momentOfInertia,
            angularAcceleration,
            initialAngularVelocity,
            finalAngularVelocity,
            angularDisplacement,
            time,
            mass,
            radius
        )
    }
}

class CalculateThermodynamicsUseCase(
    private val repository: PhysicsCalculatorRepo
) {
    operator fun invoke(
        pressure: Double?,
        volume: Double?,
        temperature: Double?,
        numberOfMoles: Double?,
        heatCapacity: Double?,
        deltaTemperature: Double?,
        workDone: Double?
    ): ThermodynamicsResult {

        // Require at least 3 inputs to calculate anything meaningful
        val provided = listOfNotNull(
            pressure, volume, temperature,
            numberOfMoles, heatCapacity, deltaTemperature, workDone
        )
        require(provided.size >= 3) {
            "At least three known values are required to calculate thermodynamic properties."
        }

        // Validate physical constraints
        pressure?.let { require(it > 0) { "Pressure must be positive." } }
        volume?.let { require(it > 0) { "Volume must be positive." } }
        temperature?.let { require(it > 0) { "Temperature must be in Kelvin and positive." } }
        numberOfMoles?.let { require(it > 0) { "Number of moles must be positive." } }
        heatCapacity?.let { require(it > 0) { "Heat capacity must be positive." } }

        return repository.calculateThermodynamics(
            pressure, volume, temperature, numberOfMoles,
            heatCapacity, deltaTemperature, workDone
        )
    }
}

class CalculateMagnetismUseCase(
    private val repository: PhysicsCalculatorRepo
) {
    operator fun invoke(
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
        val providedValues = listOfNotNull(
            magneticField, current, velocity, charge,
            length, area, numberOfTurns, angle, smallRadius, mass
        )
        require(providedValues.isNotEmpty()) {
            "At least one input must be provided"
        }

        magneticField?.let { require(it >= 0) { "Magnetic field must be non-negative" } }
        current?.let { require(it >= 0) { "Current must be non-negative" } }
        velocity?.let { require(it >= 0) { "Velocity must be non-negative" } }
        charge?.let { require(it != 0.0) { "Charge must not be zero" } }
        length?.let { require(it > 0) { "Length must be positive" } }
        area?.let { require(it > 0) { "Area must be positive" } }
        numberOfTurns?.let { require(it > 0) { "Number of turns must be positive" } }
        angle?.let { require(angle in 0.0..90.0) { "Angle must be between 0 and 90 degrees" } }
        smallRadius?.let { require(it > 0) { "Small radius must be positive" } }
        mass?.let { require(it > 0) { "Mass must be positive" } }

        return repository.calculateMagnetism(
            magneticField,
            current,
            velocity,
            charge,
            length,
            area,
            numberOfTurns,
            angle,
            smallRadius,
            mass
        )
    }
}

class CalculateElectricityUseCase(
    private val repository: PhysicsCalculatorRepo
) {
    operator fun invoke(
        charge1: Double?,
        charge2: Double?,
        distance: Double?,
        electricField: Double?,
        potential: Double?,
        capacitance: Double?,
        voltage: Double?,
        area: Double?
    ): ElectricityResult {
        val hasCoulombsLaw = charge1 != null && charge2 != null && distance != null
        val hasFieldPotential = electricField != null && potential != null
        val hasCapacitor = capacitance != null && voltage != null
        val hasArea = area != null

        require(
            hasCoulombsLaw || hasFieldPotential || hasCapacitor || hasArea
        ) { "Provide a valid set of inputs for electricity calculations." }

        charge1?.let { require(it != 0.0) { "Charge1 must not be zero." } }
        charge2?.let { require(it != 0.0) { "Charge2 must not be zero." } }
        distance?.let { require(it > 0) { "Distance must be positive." } }
        electricField?.let { require(it >= 0) { "Electric field strength must be non-negative." } }
        capacitance?.let { require(it > 0) { "Capacitance must be positive." } }
        voltage?.let { require(it >= 0) { "Voltage must be non-negative." } }
        area?.let { require(it > 0) { "Area must be positive." } }

        return repository.calculateElectricity(
            charge1, charge2, distance, electricField, potential, capacitance, voltage, area
        )
    }
}
