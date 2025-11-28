package com.eseka.physiquest.app.physics.presentation.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Waves
import androidx.compose.ui.graphics.vector.ImageVector
import com.eseka.physiquest.core.presentation.UiText
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.circuit_analysis
import physiquest.composeapp.generated.resources.electricity
import physiquest.composeapp.generated.resources.energy_calculations
import physiquest.composeapp.generated.resources.fluid_mechanics
import physiquest.composeapp.generated.resources.kinematics
import physiquest.composeapp.generated.resources.magnetism
import physiquest.composeapp.generated.resources.projectile_motion
import physiquest.composeapp.generated.resources.rotational_motion
import physiquest.composeapp.generated.resources.simple_harmonic_motion
import physiquest.composeapp.generated.resources.thermodynamics
import physiquest.composeapp.generated.resources.wave_physics

enum class CalculationType(val displayName: UiText, val icon: ImageVector) {
    PROJECTILE_MOTION(UiText.StringResourceId(Res.string.projectile_motion), Icons.Default.Science),
    SIMPLE_HARMONIC_MOTION(
        UiText.StringResourceId(Res.string.simple_harmonic_motion),
        Icons.Default.Waves
    ),
    CIRCUIT_ANALYSIS(
        UiText.StringResourceId(Res.string.circuit_analysis),
        Icons.Default.ElectricBolt
    ),
    WAVE_PHYSICS(UiText.StringResourceId(Res.string.wave_physics), Icons.Default.Waves),
    KINEMATICS(UiText.StringResourceId(Res.string.kinematics), Icons.Default.Science),
    ENERGY_CALCULATIONS(
        UiText.StringResourceId(Res.string.energy_calculations),
        Icons.Default.ElectricBolt
    ),
    FLUID_MECHANICS(UiText.StringResourceId(Res.string.fluid_mechanics), Icons.Default.Waves),
    ROTATIONAL_MOTION(UiText.StringResourceId(Res.string.rotational_motion), Icons.Default.Science),
    THERMODYNAMICS(UiText.StringResourceId(Res.string.thermodynamics), Icons.Default.Science),
    MAGNETISM(UiText.StringResourceId(Res.string.magnetism), Icons.Default.ElectricBolt),
    ELECTRICITY(UiText.StringResourceId(Res.string.electricity), Icons.Default.ElectricBolt)
}