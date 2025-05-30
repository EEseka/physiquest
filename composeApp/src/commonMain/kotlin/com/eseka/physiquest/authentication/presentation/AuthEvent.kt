package com.eseka.physiquest.authentication.presentation

import com.eseka.physiquest.core.domain.utils.FirebaseAuthError

sealed interface AuthEvent {
    data object SignUpSuccess : AuthEvent
    data object SignInSuccess : AuthEvent
    data object EmailVerified : AuthEvent
    data object ProfileSetupComplete : AuthEvent
    data class Error(val error: FirebaseAuthError) : AuthEvent
}