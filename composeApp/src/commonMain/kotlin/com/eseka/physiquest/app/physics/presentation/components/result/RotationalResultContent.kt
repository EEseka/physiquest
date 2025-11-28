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
fun RotationalResultContent(result: PhysicsResult.Rotational) {
    var isSimulating by remember { mutableStateOf(false) }
    var showSimulation by remember { mutableStateOf(false) }
    Column {
        Text(
            text = buildString {
                result.result.angularDisplacement?.let {
                    append("Angular Displacement: ${it.formatDecimal(2)} rad\n")
                }
                result.result.angularVelocity?.let {
                    append("Angular Velocity: ${it.formatDecimal(2)} rad/s\n")
                }
                result.result.angularAcceleration?.let {
                    append("Angular Acceleration: ${it.formatDecimal(2)} rad/s²\n")
                }
                result.result.torque?.let {
                    append("Torque: ${it.formatDecimal(2)} N⋅m\n")
                }
                result.result.momentOfInertia?.let {
                    append("Moment of Inertia: ${it.formatDecimal(2)} kg⋅m²\n")
                }
                result.result.rotationalKineticEnergy?.let {
                    append("Rotational KE: ${it.formatDecimal(2)} J\n")
                }
                result.result.angularMomentum?.let {
                    append("Angular Momentum: ${it.formatDecimal(2)} kg⋅m²/s")
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
                        title = "Rotational Motion",
                        xLabel = "Time (s)",
                        yLabel = "Angular Position (rad)",
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