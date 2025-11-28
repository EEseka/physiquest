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
fun ElectricityInputs(
    onCalculate: (charge1: Double?, charge2: Double?, distance: Double?, electricField: Double?, potential: Double?, capacitance: Double?, voltage: Double?, area: Double?) -> Unit,
    isCalculating: Boolean
) {
    var charge1 by rememberSaveable { mutableStateOf("") }
    var charge2 by rememberSaveable { mutableStateOf("") }
    var distance by rememberSaveable { mutableStateOf("") }
    var electricField by rememberSaveable { mutableStateOf("") }
    var potential by rememberSaveable { mutableStateOf("") }
    var capacitance by rememberSaveable { mutableStateOf("") }
    var voltage by rememberSaveable { mutableStateOf("") }
    var area by rememberSaveable { mutableStateOf("") }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Electricity",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ScientificOutlinedTextField(
                value = charge1,
                onValueChange = { charge1 = it },
                label = { Text("Charge 1 (C)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = charge2,
                onValueChange = { charge2 = it },
                label = { Text("Charge 2 (C)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = distance,
                onValueChange = { distance = it },
                label = { Text("Distance (m)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = electricField,
                onValueChange = { electricField = it },
                label = { Text("Electric Field (N/C)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = potential,
                onValueChange = { potential = it },
                label = { Text("Electric Potential (V)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = capacitance,
                onValueChange = { capacitance = it },
                label = { Text("Capacitance (F)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )


            ScientificOutlinedTextField(
                value = voltage,
                onValueChange = { voltage = it },
                label = { Text("Voltage (V)") },
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
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    onCalculate(
                        charge1.toDoubleOrNull(),
                        charge2.toDoubleOrNull(),
                        distance.toDoubleOrNull(),
                        electricField.toDoubleOrNull(),
                        potential.toDoubleOrNull(),
                        capacitance.toDoubleOrNull(),
                        voltage.toDoubleOrNull(),
                        area.toDoubleOrNull()
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