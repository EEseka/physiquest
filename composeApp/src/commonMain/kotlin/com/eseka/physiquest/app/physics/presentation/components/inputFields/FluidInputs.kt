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
fun FluidInputs(
    onCalculate: (pressure: Double?, density: Double?, velocity: Double?, area: Double?, viscosity: Double?) -> Unit,
    isCalculating: Boolean
) {
    var pressure by rememberSaveable { mutableStateOf("") }
    var density by rememberSaveable { mutableStateOf("") }
    var velocity by rememberSaveable { mutableStateOf("") }
    var area by rememberSaveable { mutableStateOf("") }
    var viscosity by rememberSaveable { mutableStateOf("") }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Fluid Mechanics",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ScientificOutlinedTextField(
                value = pressure,
                onValueChange = { pressure = it },
                label = { Text("Pressure (Pa)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            ScientificOutlinedTextField(
                value = density,
                onValueChange = { density = it },
                label = { Text("Density (kg/m³)") },
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
                value = area,
                onValueChange = { area = it },
                label = { Text("Cross-sectional Area (m²)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            ScientificOutlinedTextField(
                value = viscosity,
                onValueChange = { viscosity = it },
                label = { Text("Dynamic Viscosity (Pa·s)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    onCalculate(
                        pressure.toDoubleOrNull(),
                        density.toDoubleOrNull(),
                        velocity.toDoubleOrNull(),
                        area.toDoubleOrNull(),
                        viscosity.toDoubleOrNull()
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