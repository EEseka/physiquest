package com.eseka.physiquest.authentication.presentation.signup

import androidx.compose.runtime.Immutable
import com.eseka.physiquest.core.presentation.UiText

@Immutable
data class SignUpState(
    val isLoading: Boolean = false,
    val email: String = "",
    val emailError: UiText? = null,
    val password: String = "",
    val passwordError: UiText? = null,
    val repeatedPassword: String = "",
    val repeatedPasswordError: UiText? = null,
    val isEmailVerified: Boolean = false,
    val emailVerificationError: UiText? = null,
    // For User Profile
    val displayName: String = "",
    val photoUrl: String? = null,
    val displayNameError: UiText? = null,
    val photoUriError: UiText? = null
)