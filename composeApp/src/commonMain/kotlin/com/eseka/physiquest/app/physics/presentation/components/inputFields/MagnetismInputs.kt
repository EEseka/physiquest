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
fun MagnetismInputs(
    onCalculate: (magneticField: Double?, current: Double?, velocity: Double?, charge: Double?, length: Double?, area: Double?, numberOfTurns: Int?, angle: Double?, smallRadius: Double?, mass: Double?) -> Unit,
    isCalculating: Boolean
) {
    var magneticField by rememberSaveable { mutableStateOf("") }
    var current by rememberSaveable { mutableStateOf("") }
    var velocity by rememberSaveable { mutableStateOf("") }
    var charge by rememberSaveable { mutableStateOf("") }
    var length by rememberSaveable { mutableStateOf("") }
    var area by rememberSaveable { mutableStateOf("") }
    var numberOfTurns by rememberSaveable { mutableStateOf("") }
    var angle by rememberSaveable { mutableStateOf("") }
    var smallRadius by rememberSaveable { mutableStateOf("") }
    var mass by rememberSaveable { mutableStateOf("") }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Magnetism",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ScientificOutlinedTextField(
                value = magneticField,
                onValueChange = { magneticField = it },
                label = { Text("Magnetic Field (T)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = current,
                onValueChange = { current = it },
                label = { Text("Current (A)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = velocity,
                onValueChange = { velocity = it },
                label = { Text("Velocity (m/s)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = charge,
                onValueChange = { charge = it },
                label = { Text("Charge (C)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = length,
                onValueChange = { length = it },
                label = { Text("Length (m)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = area,
                onValueChange = { area = it },
                label = { Text("Area (m²)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = numberOfTurns,
                onValueChange = { numberOfTurns = it },
                label = { Text("Number of Turns") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            ScientificOutlinedTextField(
                value = angle,
                onValueChange = { angle = it },
                label = { Text("Angle (°)") },
                showScientificHelper = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            ScientificOutlinedTextField(
                value = smallRadius,
                onValueChange = { smallRadius = it },
                label = { Text("Radius (m)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            ScientificOutlinedTextField(
                value = mass,
                onValueChange = { mass = it },
                label = { Text("Mass (kg)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    onCalculate(
                        magneticField.toDoubleOrNull(),
                        current.toDoubleOrNull(),
                        velocity.toDoubleOrNull(),
                        charge.toDoubleOrNull(),
                        length.toDoubleOrNull(),
                        area.toDoubleOrNull(),
                        numberOfTurns.toIntOrNull(),
                        angle.toDoubleOrNull(),
                        smallRadius.toDoubleOrNull(),
                        mass.toDoubleOrNull()
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