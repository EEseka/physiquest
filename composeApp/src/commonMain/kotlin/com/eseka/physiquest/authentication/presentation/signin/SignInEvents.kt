package com.eseka.physiquest.authentication.presentation.signin


sealed interface SignInEvents {
    data class OnEmailChanged(val email: String) : SignInEvents
    data class OnPasswordChanged(val password: String) : SignInEvents
    data class OnForgotPasswordEmailChanged(val email: String) : SignInEvents
    data object OnSignInClicked : SignInEvents
    data class OnSignInWithGoogleClicked(val idToken: String?, val accessToken: String?) :
        SignInEvents

    data object OnResendVerificationEmailClicked : SignInEvents
    data object OnEmailVerifiedClicked : SignInEvents
    data object ClearEmailVerificationError : SignInEvents
    data object ClearForgotPasswordError : SignInEvents
    data object ClearForgotPasswordEmailSent : SignInEvents
    data object OnSendPasswordResetClicked : SignInEvents
}