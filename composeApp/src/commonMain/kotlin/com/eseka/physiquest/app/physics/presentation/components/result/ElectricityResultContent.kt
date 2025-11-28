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
import com.eseka.physiquest.app.physics.presentation.components.simulators.EquipotentialSimulator
import com.eseka.physiquest.app.physics.presentation.components.simulators.FieldLineSimulator
import com.eseka.physiquest.app.physics.presentation.components.simulators.ForceVectorSimulator
import com.eseka.physiquest.app.physics.presentation.utils.formatDecimal

@Composable
fun ElectricityResultContent(result: PhysicsResult.Electricity) {
    var showFieldLines by remember { mutableStateOf(false) }

    var isSimulatingFieldLines by remember { mutableStateOf(false) }
    var isSimulatingEquipotential by remember { mutableStateOf(false) }
    var isSimulatingForceVectors by remember { mutableStateOf(false) }
    Column {
        Text(
            text = buildString {
                result.result.electricField?.let {
                    append("Electric Field: ${it.formatDecimal(2)} N/C\n")
                }
                result.result.electricForce?.let {
                    append("Electric Force: ${it.formatDecimal(2)} N\n")
                }
                result.result.electricPotential?.let {
                    append("Electric Potential: ${it.formatDecimal(2)} V\n")
                }
                result.result.capacitance?.let {
                    append("Capacitance: ${it.formatDecimal(2)} F\n")
                }
                result.result.storedEnergy?.let {
                    append("Stored Energy: ${it.formatDecimal(2)} J\n")
                }
                result.result.electricFlux?.let {
                    append("Electric Flux: ${it.formatDecimal(2)} N⋅m²/C")
                }
            },
            style = MaterialTheme.typography.bodyMedium
        )

        if (result.result.electricFieldLines.isNotEmpty() || result.result.equipotentialLines.isNotEmpty() || result.result.forceVectors.isNotEmpty()) {
            Button(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                onClick = { showFieldLines = !showFieldLines }
            ) {
                Icon(
                    imageVector = if (showFieldLines) Icons.Default.Close else Icons.Default.Draw,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (showFieldLines) "Hide Simulation" else "Show Field Lines")
            }

            AnimatedVisibility(
                visible = showFieldLines,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    if (result.result.electricFieldLines.isNotEmpty()) {
                        FieldLineSimulator(
                            fieldLines = result.result.electricFieldLines,
                            title = "Electric Field Lines",
                            isAnimating = isSimulatingFieldLines,
                            onAnimationComplete = { isSimulatingFieldLines = false },
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { isSimulatingFieldLines = true },
                                enabled = !isSimulatingFieldLines
                            ) {
                                Text("Start Animation")
                            }
                            Button(
                                onClick = { isSimulatingFieldLines = false },
                                enabled = isSimulatingFieldLines
                            ) {
                                Text("Stop")
                            }
                        }
                    }
                    if (result.result.equipotentialLines.isNotEmpty()) {
                        EquipotentialSimulator(
                            equipotentialLines = result.result.equipotentialLines,
                            isAnimating = isSimulatingEquipotential,
                            onAnimationComplete = { isSimulatingEquipotential = false },
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { isSimulatingEquipotential = true },
                                enabled = !isSimulatingEquipotential
                            ) {
                                Text("Start Animation")
                            }
                            Button(
                                onClick = { isSimulatingEquipotential = false },
                                enabled = isSimulatingEquipotential
                            ) {
                                Text("Stop")
                            }
                        }
                    }
                    if (result.result.forceVectors.isNotEmpty()) {
                        ForceVectorSimulator(
                            forceVectors = result.result.forceVectors,
                            isAnimating = isSimulatingForceVectors,
                            onAnimationComplete = { isSimulatingForceVectors = false },
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { isSimulatingForceVectors = true },
                                enabled = !isSimulatingForceVectors
                            ) {
                                Text("Start Animation")
                            }
                            Button(
                                onClick = { isSimulatingForceVectors = false },
                                enabled = isSimulatingForceVectors
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