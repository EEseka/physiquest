package com.eseka.physiquest.app.physics.presentation

import androidx.compose.runtime.Immutable
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
import com.eseka.physiquest.app.physics.domain.models.nasa.AstronomyPictureOfTheDay
import com.eseka.physiquest.app.physics.presentation.utils.CalculationType
import com.eseka.physiquest.core.presentation.UiText

@Immutable
data class PhysicsCalculatorState(
    val selectedCalculationType: CalculationType = CalculationType.PROJECTILE_MOTION,
    val currentResult: PhysicsResult? = null,
    val isCalculating: Boolean = false,
    val inputError: UiText? = null,
    val savedCalculations: List<SavedCalculation> = emptyList(),
    val isLoadingSavedCalculations: Boolean = false,
    val isAPODLoading: Boolean = false,
    val APOD: AstronomyPictureOfTheDay? = null
)

@Immutable
sealed class PhysicsResult {
    data class ProjectileMotion(val result: ProjectileResult) :
        PhysicsResult()

    data class SHM(val result: SHMResult) :
        PhysicsResult()

    data class Circuit(val result: CircuitResult) :
        PhysicsResult()

    data class Wave(val result: WaveResult) :
        PhysicsResult()

    data class Kinematics(val result: KinematicsResult) :
        PhysicsResult()

    data class Energy(val result: EnergyResult) :
        PhysicsResult()

    data class Fluid(val result: FluidResult) :
        PhysicsResult()

    data class Rotational(val result: RotationalResult) :
        PhysicsResult()

    data class Thermodynamics(val result: ThermodynamicsResult) :
        PhysicsResult()

    data class Magnetism(val result: MagnetismResult) :
        PhysicsResult()

    data class Electricity(val result: ElectricityResult) :
        PhysicsResult()
}

@Immutable
data class SavedCalculation(
    val id: String,
    val name: String,
    val calculationType: CalculationType,
    val result: PhysicsResult,
    val createdAt: Long,
    val inputParameters: Map<String, Double>
)