package com.eseka.physiquest.app.physics.presentation.components.inputFields

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun ScientificOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    showScientificHelper: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(value, TextRange(value.length)))
    }

    var hasScientificNotation by remember(value) {
        mutableStateOf(value.contains("e", ignoreCase = true))
    }

    // Check if cursor is after 'e' for exponential minus
    val isInExponentialPart by remember {
        derivedStateOf {
            val text = textFieldValue.text
            val cursorPos = textFieldValue.selection.start
            val eIndex = text.indexOfAny(listOf("e", "E"))
            eIndex != -1 && cursorPos > eIndex
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValue = newValue
            onValueChange(newValue.text)
            hasScientificNotation = newValue.text.contains("e", ignoreCase = true)
        },
        label = label,
        leadingIcon = {
            IconButton(
                onClick = {
                    val currentText = textFieldValue.text
                    val cursorPos = textFieldValue.selection.start
                    val eIndex = currentText.indexOfAny(listOf("e", "E"))

                    if (eIndex != -1 && cursorPos > eIndex) {
                        // Handle exponential part minus
                        val beforeE = currentText.substring(0, eIndex + 1)
                        val afterE = currentText.substring(eIndex + 1)

                        val newAfterE = if (afterE.startsWith("-")) {
                            afterE.removePrefix("-")
                        } else {
                            "-$afterE"
                        }

                        val newText = beforeE + newAfterE
                        textFieldValue = TextFieldValue(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                        onValueChange(newText)
                    } else {
                        // Handle main number minus
                        val newText = if (currentText.startsWith("-")) {
                            currentText.removePrefix("-")
                        } else {
                            "-$currentText"
                        }
                        textFieldValue = TextFieldValue(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                        onValueChange(newText)
                    }
                }
            ) {
                val showingMinus = if (isInExponentialPart) {
                    // Check if exponential part has minus
                    val text = textFieldValue.text
                    val eIndex = text.indexOfAny(listOf("e", "E"))
                    if (eIndex != -1 && eIndex + 1 < text.length) {
                        text[eIndex + 1] == '-'
                    } else false
                } else {
                    // Check if main number has minus
                    textFieldValue.text.startsWith("-")
                }

                Text(
                    text = if (showingMinus) "−" else "+",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (showingMinus)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingIcon = if (showScientificHelper && textFieldValue.text.isNotBlank()) {
            {
                IconButton(
                    onClick = {
                        if (!hasScientificNotation) {
                            val newText = textFieldValue.text + "e"
                            textFieldValue = TextFieldValue(
                                text = newText,
                                selection = TextRange(newText.length)
                            )
                            onValueChange(newText)
                            hasScientificNotation = true
                        }
                    },
                    enabled = !hasScientificNotation
                ) {
                    Text(
                        text = "×10^",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier
    )
}