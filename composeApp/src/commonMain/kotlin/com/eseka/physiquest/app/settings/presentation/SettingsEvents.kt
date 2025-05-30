package com.eseka.physiquest.app.settings.presentation

import androidx.compose.ui.text.input.TextFieldValue

sealed interface SettingsEvents {
    data class OnDisplayNameChanged(val name: TextFieldValue) : SettingsEvents
    data class OnPhotoSelected(val url: String, val extension: String) : SettingsEvents
    data object OnUpdateProfileClicked : SettingsEvents
    data object OnSignOutClicked : SettingsEvents
    data object OnDeleteAccountClicked : SettingsEvents
    data class OnReAuthenticateWithGoogle(val idToken: String?, val accessToken: String?) :
        SettingsEvents

    data class OnReAuthenticateWithPassword(val password: String) : SettingsEvents
    data object OnScreenLeave : SettingsEvents
    data object OnScreenReturn : SettingsEvents
}