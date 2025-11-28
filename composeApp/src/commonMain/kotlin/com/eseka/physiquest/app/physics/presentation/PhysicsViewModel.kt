package com.eseka.physiquest.app.physics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diamondedge.logging.logging
import com.eseka.physiquest.app.MainEvent
import com.eseka.physiquest.app.MainEventBus
import com.eseka.physiquest.app.physics.domain.AstronomyRepository
import com.eseka.physiquest.app.physics.domain.CalculateCircuitUseCase
import com.eseka.physiquest.app.physics.domain.CalculateElectricityUseCase
import com.eseka.physiquest.app.physics.domain.CalculateEnergyUseCase
import com.eseka.physiquest.app.physics.domain.CalculateFluidUseCase
import com.eseka.physiquest.app.physics.domain.CalculateKinematicsUseCase
import com.eseka.physiquest.app.physics.domain.CalculateMagnetismUseCase
import com.eseka.physiquest.app.physics.domain.CalculateProjectileMotionUseCase
import com.eseka.physiquest.app.physics.domain.CalculateRotationalMotionUseCase
import com.eseka.physiquest.app.physics.domain.CalculateSHMUseCase
import com.eseka.physiquest.app.physics.domain.CalculateThermodynamicsUseCase
import com.eseka.physiquest.app.physics.domain.CalculateWaveUseCase
import com.eseka.physiquest.core.domain.utils.onError
import com.eseka.physiquest.core.domain.utils.onSuccess
import com.eseka.physiquest.core.presentation.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PhysicsCalculatorViewModel(
    private val calculateProjectileMotionUseCase: CalculateProjectileMotionUseCase,
    private val calculateSHMUseCase: CalculateSHMUseCase,
    private val calculateCircuitUseCase: CalculateCircuitUseCase,
    private val calculateWaveUseCase: CalculateWaveUseCase,
    private val calculateKinematicsUseCase: CalculateKinematicsUseCase,
    private val calculateEnergyUseCase: CalculateEnergyUseCase,
    private val calculateFluidUseCase: CalculateFluidUseCase,
    private val calculateRotationalMotionUseCase: CalculateRotationalMotionUseCase,
    private val calculateThermodynamicsUseCase: CalculateThermodynamicsUseCase,
    private val calculateMagnetismUseCase: CalculateMagnetismUseCase,
    private val calculateElectricityUseCase: CalculateElectricityUseCase,
    private val astronomyRepository: AstronomyRepository,
    private val mainEventBus: MainEventBus
) : ViewModel() {

    private val _state = MutableStateFlow(PhysicsCalculatorState())
    val state = _state
        .onStart { getAstronomyPictureOfTheDay() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            PhysicsCalculatorState()
        )

    fun onEvent(event: PhysicsCalculatorEvents) {
        when (event) {
            is PhysicsCalculatorEvents.OnCalculationTypeSelected -> {
                _state.update {
                    it.copy(
                        selectedCalculationType = event.type,
                        currentResult = null,
                        inputError = null
                    )
                }
            }

            is PhysicsCalculatorEvents.OnProjectileMotionCalculate -> {
                calculateProjectileMotion(event.initialVelocity, event.angle, event.height)
            }

            is PhysicsCalculatorEvents.OnSHMCalculate -> {
                calculateSHM(event.amplitude, event.frequency, event.time, event.phase)
            }

            is PhysicsCalculatorEvents.OnCircuitCalculate -> {
                calculateCircuit(event.voltage, event.current, event.resistance)
            }

            is PhysicsCalculatorEvents.OnWaveCalculate -> {
                calculateWave(event.frequency, event.wavelength, event.velocity)
            }

            is PhysicsCalculatorEvents.OnKinematicsCalculate -> {
                calculateKinematics(
                    event.initialVelocity, event.finalVelocity, event.acceleration,
                    event.time, event.displacement
                )
            }

            is PhysicsCalculatorEvents.OnEnergyCalculate -> {
                calculateEnergy(
                    event.mass, event.velocity, event.height, event.force, event.distance
                )
            }

            is PhysicsCalculatorEvents.OnFluidCalculate -> {
                calculateFluid(
                    event.pressure, event.density, event.velocity, event.area, event.viscosity
                )
            }

            is PhysicsCalculatorEvents.OnRotationalCalculate -> {
                calculateRotationalMotion(
                    event.torque,
                    event.momentOfInertia,
                    event.angularAcceleration,
                    event.initialAngularVelocity,
                    event.finalAngularVelocity,
                    event.angularDisplacement,
                    event.time,
                    event.mass,
                    event.radius
                )
            }

            is PhysicsCalculatorEvents.OnThermodynamicsCalculate -> {
                calculateThermodynamics(
                    event.pressure, event.volume, event.temperature, event.numberOfMoles,
                    event.heatCapacity, event.deltaTemperature, event.workDone
                )
            }

            is PhysicsCalculatorEvents.OnMagnetismCalculate -> {
                calculateMagnetism(
                    event.magneticField,
                    event.current,
                    event.velocity,
                    event.charge,
                    event.length,
                    event.area,
                    event.numberOfTurns,
                    event.angle,
                    event.smallRadius,
                    event.mass
                )
            }

            is PhysicsCalculatorEvents.OnElectricityCalculate -> {
                calculateElectricity(
                    event.charge1, event.charge2, event.distance, event.electricField,
                    event.potential, event.capacitance, event.voltage, event.area
                )
            }

            PhysicsCalculatorEvents.OnClearResults -> {
                _state.update {
                    it.copy(
                        currentResult = null,
                        inputError = null
                    )
                }
            }

            is PhysicsCalculatorEvents.OnSaveCalculation -> {
                saveCalculation(event.calculationName)
            }

            PhysicsCalculatorEvents.OnLoadSavedCalculations -> {
                loadSavedCalculations()
            }

            is PhysicsCalculatorEvents.OnDeleteSavedCalculation -> {
                deleteSavedCalculation(event.calculationId)
            }
        }
    }

    private fun calculateProjectileMotion(v0: Double?, angle: Double?, height: Double?) {
        performCalculation(
            calculationName = "Projectile Motion",
            calculation = {
                val result = calculateProjectileMotionUseCase(v0, angle, height)
                PhysicsResult.ProjectileMotion(result)
            }
        )
    }

    private fun calculateSHM(
        amplitude: Double?,
        frequency: Double?,
        time: Double?,
        phase: Double?
    ) {
        performCalculation(
            calculationName = "Simple Harmonic Motion",
            calculation = {
                val result = calculateSHMUseCase(amplitude, frequency, time, phase)
                PhysicsResult.SHM(result)
            }
        )
    }

    private fun calculateCircuit(voltage: Double?, current: Double?, resistance: Double?) {
        performCalculation(
            calculationName = "Circuit Analysis",
            calculation = {
                val result = calculateCircuitUseCase(voltage, current, resistance)
                PhysicsResult.Circuit(result)
            }
        )
    }

    private fun calculateWave(frequency: Double?, wavelength: Double?, velocity: Double?) {
        performCalculation(
            calculationName = "Wave Physics",
            calculation = {
                val result = calculateWaveUseCase(frequency, wavelength, velocity)
                PhysicsResult.Wave(result)
            }
        )
    }

    private fun calculateKinematics(
        initialVelocity: Double?, finalVelocity: Double?, acceleration: Double?,
        time: Double?, displacement: Double?
    ) {
        performCalculation(
            calculationName = "Kinematics",
            calculation = {
                val result = calculateKinematicsUseCase(
                    initialVelocity, finalVelocity, acceleration, time, displacement
                )
                PhysicsResult.Kinematics(result)
            }
        )
    }

    private fun calculateEnergy(
        mass: Double?, velocity: Double?, height: Double?, force: Double?, distance: Double?
    ) {
        performCalculation(
            calculationName = "Energy Calculations",
            calculation = {
                val result = calculateEnergyUseCase(mass, velocity, height, force, distance)
                PhysicsResult.Energy(result)
            }
        )
    }

    private fun calculateFluid(
        pressure: Double?, density: Double?, velocity: Double?, area: Double?, viscosity: Double?
    ) {
        performCalculation(
            calculationName = "Fluid Mechanics",
            calculation = {
                val result = calculateFluidUseCase(pressure, density, velocity, area, viscosity)
                PhysicsResult.Fluid(result)
            }
        )
    }

    private fun calculateRotationalMotion(
        torque: Double?,
        momentOfInertia: Double?,
        angularAcceleration: Double?,
        initialAngularVelocity: Double?,
        finalAngularVelocity: Double?,
        angularDisplacement: Double?,
        time: Double?,
        mass: Double?,
        radius: Double?
    ) {
        performCalculation(
            calculationName = "Rotational Motion",
            calculation = {
                val result = calculateRotationalMotionUseCase(
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
                PhysicsResult.Rotational(result)
            }
        )
    }

    private fun calculateThermodynamics(
        pressure: Double?, volume: Double?, temperature: Double?, numberOfMoles: Double?,
        heatCapacity: Double?, deltaTemperature: Double?, workDone: Double?
    ) {
        performCalculation(
            calculationName = "Thermodynamics",
            calculation = {
                val result = calculateThermodynamicsUseCase(
                    pressure, volume, temperature, numberOfMoles,
                    heatCapacity, deltaTemperature, workDone
                )
                PhysicsResult.Thermodynamics(result)
            }
        )
    }

    private fun calculateMagnetism(
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
    ) {
        performCalculation(
            calculationName = "Magnetism",
            calculation = {
                val result = calculateMagnetismUseCase(
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
                PhysicsResult.Magnetism(result)
            }
        )
    }

    private fun calculateElectricity(
        charge1: Double?, charge2: Double?, distance: Double?, electricField: Double?,
        potential: Double?, capacitance: Double?, voltage: Double?, area: Double?
    ) {
        performCalculation(
            calculationName = "Electricity",
            calculation = {
                val result = calculateElectricityUseCase(
                    charge1, charge2, distance, electricField, potential, capacitance, voltage, area
                )
                PhysicsResult.Electricity(result)
            }
        )
    }

    private fun performCalculation(
        calculationName: String,
        calculation: () -> PhysicsResult
    ) {
        _state.update { it.copy(isCalculating = true, inputError = null) }

        viewModelScope.launch {
            try {
                val result = calculation()
                _state.update {
                    it.copy(
                        currentResult = result,
                        isCalculating = false
                    )
                }
                log.i(tag = TAG, msg = { "$calculationName calculated successfully" })
            } catch (e: IllegalArgumentException) {
                // Handle validation errors from use cases
                _state.update {
                    it.copy(
                        isCalculating = false,
                        inputError = UiText.DynamicString(e.message ?: "Invalid input")
                    )
                }
                log.w(tag = TAG, msg = { "$calculationName validation error: ${e.message}" })
            } catch (e: IllegalStateException) {
                _state.update {
                    it.copy(
                        isCalculating = false,
                        inputError = UiText.DynamicString(
                            e.message ?: "Calculation requirements not met"
                        )
                    )
                }
                log.w(
                    tag = TAG,
                    msg = { "$calculationName requirement error: ${e.message}" })
            } catch (e: Exception) {
                handleCalculationError(calculationName, e)
            }
        }
    }

    private fun getAstronomyPictureOfTheDay() {
        viewModelScope.launch {
            _state.update { it.copy(isAPODLoading = true) }
            astronomyRepository.getAstronomyPictureOfTheDay()
                .onSuccess { result ->
                    _state.update { it.copy(isAPODLoading = false, APOD = result) }
                }
                .onError { error ->
                    _state.update { it.copy(isAPODLoading = false) }
                    log.e(tag = TAG, msg = { "Error fetching APOD: ${error.name}" })
                    mainEventBus.send(MainEvent.NetworkingError(error))
                }
        }
    }

    private fun saveCalculation(calculationName: String) {
        val currentResult = _state.value.currentResult
        if (currentResult == null || calculationName.isBlank()) {
            _state.update {
                it.copy(inputError = UiText.DynamicString("Please provide a valid calculation name"))
            }
            return
        }

//        viewModelScope.launch {
//            try {
//                // TODO: Implement saving to database
//                Logger.i(tag = TAG, message = { "Calculation saved: $calculationName" })
//                // Show success message or update UI state
//            } catch (e: Exception) {
//                Logger.e(tag = TAG, message = { "Error saving calculation: ${e.message}" })
//                mainEventBus.send(MainEvent.DatabaseError(e))
//            }
//        }
    }

    private fun loadSavedCalculations() {
//        viewModelScope.launch {
//            _state.update { it.copy(isLoadingSavedCalculations = true) }
//            try {
//                // TODO: Implement loading from database
//                val savedCalculations = emptyList<SavedCalculation>()
//                _state.update {
//                    it.copy(
//                        savedCalculations = savedCalculations,
//                        isLoadingSavedCalculations = false
//                    )
//                }
//            } catch (e: Exception) {
//                _state.update { it.copy(isLoadingSavedCalculations = false) }
//                Logger.e(tag = TAG, message = { "Error loading saved calculations: ${e.message}" })
//                mainEventBus.send(MainEvent.DatabaseError(e))
//            }
//        }
    }

    private fun deleteSavedCalculation(calculationId: String) {
//        viewModelScope.launch {
//            try {
//                // TODO: Implement deletion from database
//                Logger.i(tag = TAG, message = { "Calculation deleted: $calculationId" })
//                loadSavedCalculations() // Refresh the list
//            } catch (e: Exception) {
//                Logger.e(tag = TAG, message = { "Error deleting calculation: ${e.message}" })
//                mainEventBus.send(MainEvent.DatabaseError(e))
//            }
//        }
    }

    private fun handleCalculationError(calculationName: String, exception: Exception) {
        _state.update {
            it.copy(
                isCalculating = false,
                inputError = UiText.DynamicString("$calculationName error: ${exception.message}")
            )
        }
        log.e(tag = TAG, msg = { "$calculationName error: ${exception.message}" })
    }

    companion object {
        private const val TAG = "PhysicsCalculatorViewModel"
        val log = logging()
    }
}