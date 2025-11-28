package com.eseka.physiquest.app.physics.presentation.components.inputFields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RotationalInputs(
    onCalculate: (torque: Double?, momentOfInertia: Double?, angularAcceleration: Double?, initialAngularVelocity: Double?, finalAngularVelocity: Double?, angularDisplacement: Double?, time: Double?, pointMass: Double?, radius: Double?) -> Unit,
    isCalculating: Boolean
) {
    var torque by rememberSaveable { mutableStateOf("") }
    var momentOfInertia by rememberSaveable { mutableStateOf("") }
    var angularAcceleration by rememberSaveable { mutableStateOf("") }
    var initialAngularVelocity by rememberSaveable { mutableStateOf("") }
    var finalAngularVelocity by rememberSaveable { mutableStateOf("") }
    var angularDisplacement by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var mass by rememberSaveable { mutableStateOf("") }
    var radius by rememberSaveable { mutableStateOf("") }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Rotational Motion",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )


            ScientificOutlinedTextField(
                value = torque,
                onValueChange = { torque = it },
                label = { Text("Torque (N·m)") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = momentOfInertia,
                onValueChange = { momentOfInertia = it },
                label = { Text("Moment of Inertia (kg·m²)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = angularAcceleration,
                onValueChange = { angularAcceleration = it },
                label = { Text("Angular Acceleration (rad/s²)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = initialAngularVelocity,
                onValueChange = { initialAngularVelocity = it },
                label = { Text("Initial Angular Velocity (rad/s)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = finalAngularVelocity,
                onValueChange = { finalAngularVelocity = it },
                label = { Text("Final Angular Velocity (rad/s)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = angularDisplacement,
                onValueChange = { angularDisplacement = it },
                label = { Text("Angular Displacement (rad)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            ScientificOutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Time (s)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = mass,
                onValueChange = { mass = it },
                label = { Text("Point Mass (kg)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = radius,
                onValueChange = { radius = it },
                label = { Text("Radius (m)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    onCalculate(
                        torque.toDoubleOrNull(),
                        momentOfInertia.toDoubleOrNull(),
                        angularAcceleration.toDoubleOrNull(),
                        initialAngularVelocity.toDoubleOrNull(),
                        finalAngularVelocity.toDoubleOrNull(),
                        angularDisplacement.toDoubleOrNull(),
                        time.toDoubleOrNull(),
                        mass.toDoubleOrNull(),
                        radius.toDoubleOrNull()
                    )
                },
                enabled = !isCalculating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calculate")
            }
        }
    }
}