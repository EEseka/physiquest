package com.eseka.physiquest.app.physics.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.eseka.physiquest.app.physics.presentation.components.ApodCard
import com.eseka.physiquest.app.physics.presentation.components.PhysicsFactCard
import com.eseka.physiquest.app.physics.presentation.components.inputFields.CircuitInputs
import com.eseka.physiquest.app.physics.presentation.components.inputFields.ElectricityInputs
import com.eseka.physiquest.app.physics.presentation.components.inputFields.EnergyInputs
import com.eseka.physiquest.app.physics.presentation.components.inputFields.FluidInputs
import com.eseka.physiquest.app.physics.presentation.components.inputFields.KinematicsInputs
import com.eseka.physiquest.app.physics.presentation.components.inputFields.MagnetismInputs
import com.eseka.physiquest.app.physics.presentation.components.inputFields.ProjectileMotionInputs
import com.eseka.physiquest.app.physics.presentation.components.inputFields.RotationalInputs
import com.eseka.physiquest.app.physics.presentation.components.inputFields.SHMInputs
import com.eseka.physiquest.app.physics.presentation.components.inputFields.ThermodynamicsInputs
import com.eseka.physiquest.app.physics.presentation.components.inputFields.WaveInputs
import com.eseka.physiquest.app.physics.presentation.components.result.ResultCard
import com.eseka.physiquest.app.physics.presentation.utils.CalculationType
import com.eseka.physiquest.app.physics.presentation.utils.physicsFacts
import com.eseka.physiquest.core.presentation.components.PulseAnimation
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.physics_calculator
import physiquest.composeapp.generated.resources.select_calculation_type

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysicsCalculatorScreen(
    state: PhysicsCalculatorState,
    onCalculationTypeSelected: (CalculationType) -> Unit,
    onProjectileMotionCalculate: (initialVelocity: Double?, angle: Double?, height: Double?) -> Unit,
    onSHMCalculate: (amplitude: Double?, frequency: Double?, time: Double?, phase: Double?) -> Unit,
    onCircuitCalculate: (voltage: Double?, current: Double?, resistance: Double?) -> Unit,
    onWaveCalculate: (frequency: Double?, wavelength: Double?, velocity: Double?) -> Unit,
    onKinematicsCalculate: (initialVelocity: Double?, finalVelocity: Double?, acceleration: Double?, time: Double?, displacement: Double?) -> Unit,
    onEnergyCalculate: (mass: Double?, velocity: Double?, height: Double?, force: Double?, distance: Double?) -> Unit,
    onFluidCalculate: (pressure: Double?, density: Double?, velocity: Double?, area: Double?, viscosity: Double?) -> Unit,
    onRotationalCalculate: (torque: Double?, momentOfInertia: Double?, angularAcceleration: Double?, initialAngularVelocity: Double?, finalAngularVelocity: Double?, angularDisplacement: Double?, time: Double?, pointMass: Double?, radius: Double?) -> Unit,
    onThermodynamicsCalculate: (pressure: Double?, volume: Double?, temperature: Double?, noOfMoles: Double?, heatOfCapacity: Double?, tempChange: Double?, workDone: Double?) -> Unit,
    onMagnetismCalculate: (magneticField: Double?, current: Double?, velocity: Double?, charge: Double?, length: Double?, area: Double?, numberOfTurns: Int?, angle: Double?, smallRadius: Double?, mass: Double?) -> Unit,
    onElectricityCalculate: (charge1: Double?, charge2: Double?, distance: Double?, electricField: Double?, potential: Double?, capacitance: Double?, voltage: Double?, area: Double?) -> Unit,
    onClearResults: () -> Unit,
    onSaveCalculation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Glowing animation for calculation types
    val infiniteTransition = rememberInfiniteTransition()
    val glowingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val focusManager = LocalFocusManager.current

    // Daily physics fact - changes based on day of year
    var dailyPhysicsFact by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val dayOfYear = Clock.System.now().toEpochMilliseconds() / (1000 * 60 * 60 * 24)
        val factIndex = (dayOfYear % physicsFacts.size).toInt()
        dailyPhysicsFact = physicsFacts[factIndex]
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            },
        contentPadding = PaddingValues(16.dp)
    ) {
        // Physics Fact Card
        if (dailyPhysicsFact.isNotEmpty()) {
            item {
                PhysicsFactCard(
                    fact = dailyPhysicsFact,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // APOD Section
        item {
            state.APOD?.let { apod ->
                ApodCard(
                    apod = apod,
                    isLoading = state.isAPODLoading,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                PulseAnimation(modifier = Modifier.size(40.dp))
            }
        }

        // Physics Calculator Header (will be sticky when scrolled to)
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 8.dp)
            ) {
                // Header Title
                Text(
                    text = stringResource(Res.string.physics_calculator),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Calculation Type Selection
                Text(
                    text = stringResource(Res.string.select_calculation_type),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(CalculationType.entries) { type ->
                        FilterChip(
                            onClick = { onCalculationTypeSelected(type) },
                            label = {
                                Text(
                                    text = type.displayName.asString(),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            selected = state.selectedCalculationType == type,
                            leadingIcon = {
                                Icon(
                                    imageVector = type.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.alpha(
                                if (state.selectedCalculationType == type) glowingAlpha else 1f
                            )
                        )
                    }
                }

                // Error Display (sticky with header)
                state.inputError?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error.asString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Loading Indicator (sticky with header)
                if (state.isCalculating) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }
            }
        }

        // Input Forms
        item {
            when (state.selectedCalculationType) {
                CalculationType.PROJECTILE_MOTION -> {
                    ProjectileMotionInputs(
                        onCalculate = onProjectileMotionCalculate,
                        isCalculating = state.isCalculating
                    )
                }

                CalculationType.SIMPLE_HARMONIC_MOTION -> {
                    SHMInputs(
                        onCalculate = onSHMCalculate,
                        isCalculating = state.isCalculating
                    )
                }

                CalculationType.CIRCUIT_ANALYSIS -> {
                    CircuitInputs(
                        onCalculate = onCircuitCalculate,
                        isCalculating = state.isCalculating
                    )
                }

                CalculationType.WAVE_PHYSICS -> {
                    WaveInputs(
                        onCalculate = onWaveCalculate,
                        isCalculating = state.isCalculating
                    )
                }

                CalculationType.KINEMATICS -> {
                    KinematicsInputs(
                        onCalculate = onKinematicsCalculate,
                        isCalculating = state.isCalculating
                    )
                }

                CalculationType.ENERGY_CALCULATIONS -> {
                    EnergyInputs(
                        onCalculate = onEnergyCalculate,
                        isCalculating = state.isCalculating
                    )
                }

                CalculationType.FLUID_MECHANICS -> {
                    FluidInputs(
                        onCalculate = onFluidCalculate,
                        isCalculating = state.isCalculating
                    )
                }

                CalculationType.ROTATIONAL_MOTION -> {
                    RotationalInputs(
                        onCalculate = onRotationalCalculate,
                        isCalculating = state.isCalculating
                    )
                }

                CalculationType.THERMODYNAMICS -> {
                    ThermodynamicsInputs(
                        onCalculate = onThermodynamicsCalculate,
                        isCalculating = state.isCalculating
                    )
                }

                CalculationType.MAGNETISM -> {
                    MagnetismInputs(
                        onCalculate = onMagnetismCalculate,
                        isCalculating = state.isCalculating
                    )
                }

                CalculationType.ELECTRICITY -> {
                    ElectricityInputs(
                        onCalculate = onElectricityCalculate,
                        isCalculating = state.isCalculating
                    )
                }
            }
        }

        // Results Display
        state.currentResult?.let { result ->
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    ResultCard(
                        result = result,
                        onClearResults = onClearResults,
                        onSaveCalculation = onSaveCalculation
                    )
                }
            }
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}