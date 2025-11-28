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
import com.eseka.physiquest.app.physics.presentation.components.simulators.FieldLineSimulator
import com.eseka.physiquest.app.physics.presentation.components.simulators.TrajectorySimulator
import com.eseka.physiquest.app.physics.presentation.utils.formatDecimal

@Composable
fun MagnetismResultContent(result: PhysicsResult.Magnetism) {
    var isParticleTrajectorySimulating by remember { mutableStateOf(false) }
    var isFieldLinesSimulating by remember { mutableStateOf(false) }
    var showSimulation by remember { mutableStateOf(false) }
    Column {
        Text(
            text = buildString {
                result.result.magneticField?.let {
                    append("Magnetic Field: ${it.formatDecimal(2)} T\n")
                }
                result.result.magneticForce?.let {
                    append("Magnetic Force: ${it.formatDecimal(2)} N\n")
                }
                result.result.magneticFlux?.let {
                    append("Magnetic Flux: ${it.formatDecimal(2)} Wb\n")
                }
                result.result.inductance?.let {
                    append("Inductance: ${it.formatDecimal(2)} H\n")
                }
            },
            style = MaterialTheme.typography.bodyMedium
        )

        if (result.result.particleTrajectory.isNotEmpty() || result.result.fieldLines.isNotEmpty()) {
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
                    if (result.result.particleTrajectory.isNotEmpty()) {
                        TrajectorySimulator(
                            points = result.result.particleTrajectory,
                            title = "Particle Trajectory in Magnetic Field",
                            xLabel = "X Position (m)",
                            yLabel = "Y Position (m)",
                            isSimulating = isParticleTrajectorySimulating,
                            onSimulationComplete = { isParticleTrajectorySimulating = false },
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { isParticleTrajectorySimulating = true },
                                enabled = !isParticleTrajectorySimulating
                            ) {
                                Text("Start Simulation")
                            }
                            Button(
                                onClick = { isParticleTrajectorySimulating = false },
                                enabled = isParticleTrajectorySimulating
                            ) {
                                Text("Stop")
                            }
                        }
                    }
                    if (result.result.fieldLines.isNotEmpty()) {
                        FieldLineSimulator(
                            fieldLines = result.result.fieldLines,
                            title = "Magnetic Field Lines",
                            isAnimating = isFieldLinesSimulating,
                            onAnimationComplete = { isFieldLinesSimulating = false },
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { isFieldLinesSimulating = true },
                                enabled = !isFieldLinesSimulating
                            ) {
                                Text("Start Animation")
                            }
                            Button(
                                onClick = { isFieldLinesSimulating = false },
                                enabled = isFieldLinesSimulating
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