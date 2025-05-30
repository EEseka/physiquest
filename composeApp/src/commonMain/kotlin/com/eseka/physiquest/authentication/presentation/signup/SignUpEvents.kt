package com.eseka.physiquest.authentication.presentation.signup

sealed interface SignUpEvents {
    data class OnEmailChanged(val email: String) : SignUpEvents
    data class OnPasswordChanged(val password: String) : SignUpEvents
    data class OnRepeatedPasswordChanged(val repeatedPassword: String) : SignUpEvents
    data object OnSignUpClicked : SignUpEvents
    data class OnSignInWithGoogleClicked(val idToken: String?, val accessToken: String?) : SignUpEvents
    data object OnEmailVerifiedClicked : SignUpEvents
    data object ClearEmailVerificationError : SignUpEvents
    data class OnDisplayNameChanged(val name: String) : SignUpEvents
    data class OnPhotoSelected(val url: String, val extension: String) : SignUpEvents
    data object OnSaveProfileClicked : SignUpEvents
}