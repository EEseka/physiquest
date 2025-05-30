package com.eseka.physiquest.authentication.presentation.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.eseka.physiquest.authentication.domain.UserAuthRepo
import com.eseka.physiquest.authentication.domain.validation.ValidateEmail
import com.eseka.physiquest.authentication.domain.validation.ValidateSignInPassword
import com.eseka.physiquest.authentication.presentation.AuthEvent
import com.eseka.physiquest.authentication.presentation.AuthEventBus
import com.eseka.physiquest.core.domain.utils.FirebaseAuthError
import com.eseka.physiquest.core.domain.utils.onError
import com.eseka.physiquest.core.domain.utils.onSuccess
import com.eseka.physiquest.core.presentation.UiText
import com.eseka.physiquest.core.presentation.utils.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.email_not_verified

class SignInViewModel(
    private val userAuthRepo: UserAuthRepo,
    private val validateEmail: ValidateEmail,
    private val validatePassword: ValidateSignInPassword,
    private val authEventBus: AuthEventBus
) : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        SignInState()
    )
    private val isEmailVerified
        get() = userAuthRepo.currentUser?.isEmailVerified == true

    fun onEvent(event: SignInEvents) {
        when (event) {
            SignInEvents.ClearEmailVerificationError -> clearEmailVerificationError()
            SignInEvents.ClearForgotPasswordError -> clearForgotPasswordError()
            is SignInEvents.OnEmailChanged -> {
                _state.update { it.copy(email = event.email.trim()) }
            }

            is SignInEvents.OnPasswordChanged -> {
                _state.update { it.copy(password = event.password.trim()) }
            }

            is SignInEvents.OnForgotPasswordEmailChanged -> {
                _state.update { it.copy(forgotPasswordEmail = event.email.trim()) }
            }

            is SignInEvents.OnSignInWithGoogleClicked -> signInWithGoogle(event.idToken, event.accessToken)
            SignInEvents.OnResendVerificationEmailClicked -> resendVerificationEmail()
            SignInEvents.OnSignInClicked -> validateAndSignIn()
            SignInEvents.OnEmailVerifiedClicked -> checkEmailVerification()
            SignInEvents.OnSendPasswordResetClicked -> validateAndSendPasswordReset()
            SignInEvents.ClearForgotPasswordEmailSent -> clearForgotPasswordEmailSent()
        }
    }

    private fun signInWithGoogle(idToken: String?, accessToken: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            if (idToken == null) {
                _state.update { it.copy(isLoading = false) }
                authEventBus.send(AuthEvent.Error(FirebaseAuthError.GOOGLE_SIGN_IN_FAILED))
                return@launch
            }
            userAuthRepo.signInWithGoogle(idToken, accessToken)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isEmailVerified = isEmailVerified,
                            emailVerificationError = if (!isEmailVerified) UiText.StringResourceId(
                                Res.string.email_not_verified
                            ) else null
                        )
                    }
                    authEventBus.send(AuthEvent.SignInSuccess)
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false) }
                    authEventBus.send(AuthEvent.Error(error))
                }
        }
    }

    private fun validateAndSignIn() {
        val emailResult = validateEmail(_state.value.email)
        val passwordResult = validatePassword(_state.value.password)

        val hasError = listOf(
            emailResult,
            passwordResult
        ).any { !it.successful }

        if (hasError) {
            _state.update {
                it.copy(
                    emailError = emailResult.errorMessage?.toUiText(),
                    passwordError = passwordResult.errorMessage?.toUiText()
                )
            }
            return
        }
        _state.update { it.copy(emailError = null, passwordError = null) }
        signIn()
    }

    private fun signIn() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            userAuthRepo.signIn(email = _state.value.email, password = _state.value.password)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isEmailVerified = isEmailVerified,
                            emailVerificationError = if (!isEmailVerified) UiText.StringResourceId(
                                Res.string.email_not_verified
                            ) else null
                        )
                    }
                    authEventBus.send(AuthEvent.SignInSuccess)
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false) }
                    authEventBus.send(AuthEvent.Error(error))
                }
        }
    }

    private fun checkEmailVerification() {
        viewModelScope.launch {
            userAuthRepo.reloadUser()
                .onSuccess {
                    _state.update {
                        it.copy(
                            isEmailVerified = isEmailVerified,
                            emailVerificationError = if (!isEmailVerified) UiText.StringResourceId(
                                Res.string.email_not_verified
                            ) else null
                        )
                    }
                    if (isEmailVerified) {
                        authEventBus.send(AuthEvent.EmailVerified)
                    }
                }
                .onError { error ->
                    Logger.w(tag = TAG, message = { "AuthError reloading user : $error" })
                }
        }
    }


    private fun resendVerificationEmail() {
        viewModelScope.launch {
            if (isEmailVerified) {
                Logger.d(tag = TAG, message = { "User already verified. No need to resend email." })
                return@launch
            }
            _state.update { it.copy(isLoading = true) }

            userAuthRepo.sendEmailVerification()
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false) }
                    authEventBus.send(AuthEvent.Error(error))
                }
        }
    }

    private fun validateAndSendPasswordReset() {
        val emailResult = validateEmail(_state.value.forgotPasswordEmail)
        if (!emailResult.successful) {
            _state.update {
                it.copy(forgotPasswordEmailError = emailResult.errorMessage?.toUiText())
            }
            return
        }
        _state.update { it.copy(forgotPasswordEmailError = null) }
        sendPasswordReset()
    }

    private fun sendPasswordReset() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            userAuthRepo.sendPasswordResetEmail(_state.value.forgotPasswordEmail)
                .onSuccess {
                    // Shouldn't this be successful only if the account exists?
                    Logger.d(
                        tag = TAG,
                        message = { "Password reset email sent to ${_state.value.forgotPasswordEmail}" }
                    )
                    _state.update { it.copy(isLoading = false, forgotPasswordEmailSent = true) }
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            forgotPasswordEmailSent = false
                        )
                    }
                    authEventBus.send(AuthEvent.Error(error))
                }
        }
    }

    private fun clearEmailVerificationError() {
        _state.update { it.copy(emailVerificationError = null) }
    }

    private fun clearForgotPasswordEmailSent() {
        _state.update { it.copy(forgotPasswordEmailSent = false) }
    }

    private fun clearForgotPasswordError() {
        _state.update {
            it.copy(
                emailVerificationError = null,
                forgotPasswordEmailSent = false
            )
        }
    }

    companion object {
        private const val TAG = "SignInViewModel"
    }
}