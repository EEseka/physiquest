package com.eseka.physiquest.authentication.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import org.jetbrains.compose.resources.stringResource
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.email

@Composable
fun EmailInputField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    emailError: String?,
    emailFocusRequester: FocusRequester?,
    imeAction: ImeAction,
    onKeyboardNextClicked: () -> Unit = {},
    onKeyboardDoneClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = {
            Text(
                text = stringResource(Res.string.email),
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        isError = isError,
        supportingText = {
            emailError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        modifier = emailFocusRequester?.let {
            modifier
                .fillMaxWidth()
                .focusRequester(it)
        } ?: modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction,
            keyboardType = KeyboardType.Email
        ),
        keyboardActions = when (imeAction) {
            ImeAction.Next -> KeyboardActions(onNext = { onKeyboardNextClicked() })
            ImeAction.Done -> KeyboardActions(onDone = { onKeyboardDoneClicked() })
            else -> KeyboardActions()
        },
        singleLine = true
    )
}