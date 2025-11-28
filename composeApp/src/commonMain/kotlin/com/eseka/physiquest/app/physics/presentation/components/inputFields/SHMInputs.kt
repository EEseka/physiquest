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
fun SHMInputs(
    onCalculate: (amplitude: Double?, frequency: Double?, time: Double?, phase: Double?) -> Unit,
    isCalculating: Boolean
) {
    var amplitude by rememberSaveable { mutableStateOf("") }
    var frequency by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var phase by rememberSaveable { mutableStateOf("") }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Simple Harmonic Motion",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ScientificOutlinedTextField(
                value = amplitude,
                onValueChange = { amplitude = it },
                label = { Text("Amplitude (m)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            ScientificOutlinedTextField(
                value = frequency,
                onValueChange = { frequency = it },
                label = { Text("Frequency (Hz)") },
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
                value = phase,
                onValueChange = { phase = it },
                label = { Text("Phase (radians)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    onCalculate(
                        amplitude.toDoubleOrNull(),
                        frequency.toDoubleOrNull(),
                        time.toDoubleOrNull(),
                        phase.toDoubleOrNull()
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