package com.eseka.physiquest.app.settings.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import com.eseka.physiquest.core.presentation.UiText

@Immutable
data class SettingsState(
    val isProfileUpdating: Boolean = false,
    val isSigningOut: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val displayName: TextFieldValue = TextFieldValue(),
    val email: String = "",
    val photoUrl: String? = null,
    val displayNameError: UiText? = null,
    val photoUrlError: UiText? = null
)