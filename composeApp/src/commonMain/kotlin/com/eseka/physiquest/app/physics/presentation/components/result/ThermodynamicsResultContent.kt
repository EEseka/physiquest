package com.eseka.physiquest.app.physics.presentation.components.result

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eseka.physiquest.app.physics.presentation.PhysicsResult
import com.eseka.physiquest.app.physics.presentation.components.simulators.TrajectorySimulator
import com.eseka.physiquest.app.physics.presentation.utils.formatDecimal

@Composable
fun ThermodynamicsResultContent(result: PhysicsResult.Thermodynamics) {
    var isTemperatureTimeSimulating by remember { mutableStateOf(false) }
    var isPressureVolumeSimulating by remember { mutableStateOf(false) }
    var showSimulation by remember { mutableStateOf(false) }
    Column {
        Text(
            text = buildString {
                result.result.pressure?.let {
                    append("Pressure: ${it.formatDecimal(2)} Pa\n")
                }
                result.result.volume?.let {
                    append("Volume: ${it.formatDecimal(2)} m³\n")
                }
                result.result.temperature?.let {
                    append("Temperature: ${it.formatDecimal(2)} K\n")
                }
                result.result.numberOfMoles?.let {
                    append("Number of Moles: ${it.formatDecimal(2)}\n")
                }
                result.result.heatTransfer?.let {
                    append("Heat Transfer: ${it.formatDecimal(2)} J\n")
                }
                result.result.work?.let {
                    append("Work: ${it.formatDecimal(2)} J\n")
                }
                result.result.internalEnergyChange?.let {
                    append("ΔU: ${it.formatDecimal(2)} J\n")
                }
                result.result.entropy?.let {
                    append("Entropy: ${it.formatDecimal(2)} J/K")
                }
            },
            style = MaterialTheme.typography.bodyMedium
        )

        if (result.result.temperatureTimePoints.isNotEmpty() || result.result.pvDiagramPoints.isNotEmpty()) {
            Button(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                onClick = { showSimulation = !showSimulation }
            ) {
                Icon(
                    imageVector = if (showSimulation) Icons.Default.Close else Icons.Default.Draw,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (showSimulation) "Hide Simulation" else "Simulate")
            }

            AnimatedVisibility(
                visible = showSimulation,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    // Show temperature vs time if available
                    if (result.result.temperatureTimePoints.isNotEmpty()) {
                        TrajectorySimulator(
                            points = result.result.temperatureTimePoints,
                            title = "Temperature vs Time",
                            xLabel = "Time (s)",
                            yLabel = "Temperature (K)",
                            isSimulating = isTemperatureTimeSimulating,
                            onSimulationComplete = { isTemperatureTimeSimulating = false },
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { isTemperatureTimeSimulating = true },
                                enabled = !isTemperatureTimeSimulating
                            ) {
                                Text("Start Simulation")
                            }
                            Button(
                                onClick = { isTemperatureTimeSimulating = false },
                                enabled = isTemperatureTimeSimulating
                            ) {
                                Text("Stop")
                            }
                        }
                    }

                    // Show PV diagram if available
                    if (result.result.pvDiagramPoints.isNotEmpty()) {
                        TrajectorySimulator(
                            points = result.result.pvDiagramPoints,
                            title = "PV Diagram",
                            xLabel = "Volume (m³)",
                            yLabel = "Pressure (Pa)",
                            isSimulating = isPressureVolumeSimulating,
                            onSimulationComplete = { isPressureVolumeSimulating = false },
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { isPressureVolumeSimulating = true },
                                enabled = !isPressureVolumeSimulating
                            ) {
                                Text("Start Simulation")
                            }
                            Button(
                                onClick = { isPressureVolumeSimulating = false },
                                enabled = isPressureVolumeSimulating
                            ) {
                                Text("Stop")
                            }
                        }
                    }
                }
            }
        }
    }
}