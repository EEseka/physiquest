package com.eseka.physiquest.authentication.presentation.signin

import androidx.compose.runtime.Immutable
import com.eseka.physiquest.core.presentation.UiText

@Immutable
data class SignInState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val isEmailVerified: Boolean = false,
    val emailVerificationError: UiText? = null,
    // For Reset Password
    val forgotPasswordEmail: String = "",
    val forgotPasswordEmailError: UiText? = null,
    val forgotPasswordEmailSent: Boolean = false,
)