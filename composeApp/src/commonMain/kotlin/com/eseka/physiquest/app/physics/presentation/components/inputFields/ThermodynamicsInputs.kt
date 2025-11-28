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
fun ThermodynamicsInputs(
    onCalculate: (pressure: Double?, volume: Double?, temperature: Double?, noOfMoles: Double?, heatOfCapacity: Double?, tempChange: Double?, workDone: Double?) -> Unit,
    isCalculating: Boolean
) {
    var pressure by rememberSaveable { mutableStateOf("") }
    var volume by rememberSaveable { mutableStateOf("") }
    var temperature by rememberSaveable { mutableStateOf("") }
    var numberOfMoles by rememberSaveable { mutableStateOf("") }
    var heatCapacity by rememberSaveable { mutableStateOf("") }
    var deltaTemperature by rememberSaveable { mutableStateOf("") }
    var workDone by rememberSaveable { mutableStateOf("") }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Thermodynamics",
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
                value = volume,
                onValueChange = { volume = it },
                label = { Text("Volume (m³)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = temperature,
                onValueChange = { temperature = it },
                label = { Text("Temperature (K)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = numberOfMoles,
                onValueChange = { numberOfMoles = it },
                label = { Text("Number of Moles (mol)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = heatCapacity,
                onValueChange = { heatCapacity = it },
                label = { Text("Heat Capacity (J/mol·K)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = deltaTemperature,
                onValueChange = { deltaTemperature = it },
                label = { Text("Temperature Change (K)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = workDone,
                onValueChange = { workDone = it },
                label = { Text("Work Done (J)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    onCalculate(
                        pressure.toDoubleOrNull(),
                        volume.toDoubleOrNull(),
                        temperature.toDoubleOrNull(),
                        numberOfMoles.toDoubleOrNull(),
                        heatCapacity.toDoubleOrNull(),
                        deltaTemperature.toDoubleOrNull(),
                        workDone.toDoubleOrNull()
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