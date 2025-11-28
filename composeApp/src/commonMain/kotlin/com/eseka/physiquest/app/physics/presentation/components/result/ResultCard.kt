package com.eseka.physiquest.app.physics.presentation.components.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eseka.physiquest.app.physics.presentation.PhysicsResult
import com.eseka.physiquest.app.physics.presentation.utils.formatDecimal

@Composable
fun ResultCard(
    result: PhysicsResult,
    onClearResults: () -> Unit,
    onSaveCalculation: (String) -> Unit
) {
    var calculationName by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Results",
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
//                    IconButton(onClick = { showSaveDialog = true }) {
//                        Icon(
//                            imageVector = Icons.Default.Save,
//                            contentDescription = "Save Results",
//                            tint = MaterialTheme.colorScheme.onPrimaryContainer
//                        )
//                    }
                    IconButton(onClick = onClearResults) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Results"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Display result based on type
            when (result) {
                is PhysicsResult.ProjectileMotion -> {
                    ProjectileResultContent(result = result)
                }

                is PhysicsResult.SHM -> {
                    SHMResultContent(result = result)
                }

                is PhysicsResult.Kinematics -> {
                    KinematicsResultContent(result = result)
                }

                is PhysicsResult.Fluid -> {
                    FluidResultContent(result = result)
                }

                is PhysicsResult.Rotational -> {
                    RotationalResultContent(result = result)
                }

                is PhysicsResult.Thermodynamics -> {
                    ThermodynamicsResultContent(result = result)
                }

                is PhysicsResult.Magnetism -> {
                    MagnetismResultContent(result = result)
                }

                is PhysicsResult.Electricity -> {
                    ElectricityResultContent(result = result)
                }

                // Non-simulatable results (keep original display)
                is PhysicsResult.Circuit -> {
                    Text(
                        text = buildString {
                            result.result.voltage?.let {
                                append("Voltage: ${it.formatDecimal(2)} V\n")
                            }
                            result.result.current?.let {
                                append("Current: ${it.formatDecimal(2)} A\n")
                            }
                            result.result.resistance?.let {
                                append("Resistance: ${it.formatDecimal(2)} Ω\n")
                            }
                            append("Power: ${result.result.power.formatDecimal(2)} W")
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                is PhysicsResult.Wave -> {
                    Text(
                        text = buildString {
                            append("Wavelength: ${result.result.wavelength.formatDecimal(2)} m\n")
                            append("Period: ${result.result.period.formatDecimal(4)} s\n")
                            append(
                                "Angular Frequency: ${
                                    result.result.angularFrequency.formatDecimal(
                                        2
                                    )
                                } rad/s\n"
                            )
                            append("Wave Number: ${result.result.waveNumber.formatDecimal(2)} rad/m\n")
                            append("Phase Velocity: ${result.result.phaseVelocity.formatDecimal(2)} m/s")
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                is PhysicsResult.Energy -> {
                    Text(
                        text = buildString {
                            result.result.kineticEnergy?.let {
                                append("Kinetic Energy: ${it.formatDecimal(2)} J\n")
                            }
                            result.result.potentialEnergy?.let {
                                append("Potential Energy: ${it.formatDecimal(2)} J\n")
                            }
                            append("Total Energy: ${result.result.totalEnergy.formatDecimal(2)} J\n")
                            result.result.workDone?.let {
                                append("Work Done: ${it.formatDecimal(2)} J")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Calculation") },
            text = {
                OutlinedTextField(
                    value = calculationName,
                    onValueChange = { calculationName = it },
                    label = { Text("Calculation Name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (calculationName.isNotBlank()) {
                            onSaveCalculation(calculationName)
                            showSaveDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}