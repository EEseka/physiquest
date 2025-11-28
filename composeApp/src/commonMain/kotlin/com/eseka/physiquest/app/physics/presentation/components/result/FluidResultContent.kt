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
fun FluidResultContent(result: PhysicsResult.Fluid) {
    var isSimulating by remember { mutableStateOf(false) }
    var showSimulation by remember { mutableStateOf(false) }
    Column {
        Text(
            text = buildString {
                result.result.pressure?.let {
                    append("Pressure: ${it.formatDecimal(2)} Pa\n")
                }
                result.result.density?.let {
                    append("Density: ${it.formatDecimal(2)} kg/m³\n")
                }
                result.result.velocity?.let {
                    append("Velocity: ${it.formatDecimal(2)} m/s\n")
                }
                result.result.flowRate?.let {
                    append("Flow Rate: ${it.formatDecimal(4)} m³/s\n")
                }
                result.result.reynoldsNumber?.let { re ->
                    append("Reynolds Number: ${re.formatDecimal(2)}\n")
                    append(
                        "Flow Regime: ${
                            when {
                                re < 2300 -> "Laminar (smooth flow)"
                                re < 4000 -> "Transitional"
                                else -> "Turbulent (chaotic flow)"
                            }
                        }"
                    )
                }
            },
            style = MaterialTheme.typography.bodyMedium
        )

        if (result.result.trajectoryPoints.isNotEmpty()) {
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
                    TrajectorySimulator(
                        points = result.result.trajectoryPoints,
                        title = "Fluid Flow",
                        xLabel = "Velocity (m/s)",
                        yLabel = "Reynolds Number",
                        isSimulating = isSimulating,
                        onSimulationComplete = { isSimulating = false },
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { isSimulating = true },
                            enabled = !isSimulating
                        ) {
                            Text("Start Simulation")
                        }
                        Button(
                            onClick = { isSimulating = false },
                            enabled = isSimulating
                        ) {
                            Text("Stop")
                        }
                    }
                }
            }
        }
    }
}