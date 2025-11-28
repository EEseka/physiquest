package com.eseka.physiquest.authentication.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diamondedge.logging.logging
import com.eseka.physiquest.authentication.domain.UserAuthRepo
import com.eseka.physiquest.authentication.domain.validation.ValidateEmail
import com.eseka.physiquest.authentication.domain.validation.ValidatePassword
import com.eseka.physiquest.authentication.domain.validation.ValidateRepeatedPassword
import com.eseka.physiquest.authentication.presentation.AuthEvent
import com.eseka.physiquest.authentication.presentation.AuthEventBus
import com.eseka.physiquest.core.domain.services.FileManager
import com.eseka.physiquest.core.domain.services.ImageCompressor
import com.eseka.physiquest.core.domain.utils.FirebaseAuthError
import com.eseka.physiquest.core.domain.utils.onError
import com.eseka.physiquest.core.domain.utils.onSuccess
import com.eseka.physiquest.core.domain.validation.ValidateDisplayName
import com.eseka.physiquest.core.presentation.UiText
import com.eseka.physiquest.core.presentation.utils.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.email_not_verified
import kotlin.time.ExperimentalTime

class SignUpViewModel(
    private val userAuthRepo: UserAuthRepo,
    private val validateEmail: ValidateEmail,
    private val validatePassword: ValidatePassword,
    private val validateRepeatedPassword: ValidateRepeatedPassword,
    private val validateDisplayName: ValidateDisplayName,
    private val imageCompressor: ImageCompressor,
    private val fileManager: FileManager,
    private val authEventBus: AuthEventBus
) : ViewModel() {
    private val _state = MutableStateFlow(SignUpState())
    val state = _state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        SignUpState()
    )
    private val isEmailVerified get() = userAuthRepo.currentUser?.isEmailVerified == true

    fun onEvent(event: SignUpEvents) {
        when (event) {
            is SignUpEvents.OnEmailChanged -> {
                _state.update { it.copy(email = event.email.trim()) }
            }

            is SignUpEvents.OnPasswordChanged -> {
                _state.update { it.copy(password = event.password.trim()) }
            }

            is SignUpEvents.OnRepeatedPasswordChanged -> {
                _state.update { it.copy(repeatedPassword = event.repeatedPassword.trim()) }
            }

            SignUpEvents.OnSignUpClicked -> validateAndSubmit()
            is SignUpEvents.OnSignInWithGoogleClicked -> signInWithGoogle(
                event.idToken,
                event.accessToken
            )

            SignUpEvents.OnEmailVerifiedClicked -> checkEmailVerification()
            SignUpEvents.ClearEmailVerificationError -> clearEmailVerificationError()
            is SignUpEvents.OnDisplayNameChanged -> {
                _state.update { it.copy(displayName = event.name) }
            }

            is SignUpEvents.OnPhotoSelected -> selectImage(event.url, event.extension)
            SignUpEvents.OnSaveProfileClicked -> validateAndSaveProfile()
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


    private fun validateAndSubmit() {
        val emailResult = validateEmail(_state.value.email)
        val passwordResult = validatePassword(_state.value.password)
        val repeatedPasswordResult =
            validateRepeatedPassword(_state.value.password, _state.value.repeatedPassword)

        val hasError = listOf(
            emailResult,
            passwordResult,
            repeatedPasswordResult
        ).any { !it.successful }

        if (hasError) {
            _state.update {
                it.copy(
                    emailError = emailResult.errorMessage?.toUiText(),
                    passwordError = passwordResult.errorMessage?.toUiText(),
                    repeatedPasswordError = repeatedPasswordResult.errorMessage?.toUiText()
                )
            }
            return
        }
        _state.update {
            it.copy(
                emailError = null,
                passwordError = null,
                repeatedPasswordError = null
            )
        }
        signUp()
    }

    private fun signUp(
        email: String = _state.value.email,
        password: String = _state.value.password
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            userAuthRepo.createAccount(email, password)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    sendVerificationEmail()
                    authEventBus.send(AuthEvent.SignUpSuccess)
                }
                .onError { error ->
                    _state.update { it.copy(isLoading = false) }
                    authEventBus.send(AuthEvent.Error(error))
                }
        }
    }

    private fun sendVerificationEmail() {
        viewModelScope.launch {
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

    private fun checkEmailVerification() {
        viewModelScope.launch {
            userAuthRepo.reloadUser()
                .onSuccess {
                    _state.update {
                        it.copy(
                            isEmailVerified = isEmailVerified,
                            emailVerificationError = if (!isEmailVerified) {
                                UiText.StringResourceId(Res.string.email_not_verified)
                            } else null
                        )
                    }
                    if (isEmailVerified) {
                        authEventBus.send(AuthEvent.EmailVerified)
                    }
                }
                .onError { error ->
                    log.w(tag = TAG, msg = { "AuthError reloading user : $error" })
                }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun selectImage(url: String, extension: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            imageCompressor.compressImage(url, MAX_IMAGE_SIZE)
                .onSuccess { byteArray ->
                    val compressedUrl = fileManager.saveImageToCache(
                        byteArray,
                        "${PHOTO_FILE_NAME}${Clock.System.now().toEpochMilliseconds()}.$extension"
                    )
                    _state.update {
                        it.copy(
                            photoUrl = compressedUrl,
                            isLoading = false
                        )
                    }
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            photoUriError = error.toUiText()
                        )
                    }
                }
        }
    }

    private fun validateAndSaveProfile() {
        val displayNameResult = validateDisplayName(_state.value.displayName.trim())

        if (!displayNameResult.successful) {
            _state.update {
                it.copy(displayNameError = displayNameResult.errorMessage?.toUiText())
            }
            return
        }
        _state.update { it.copy(displayNameError = null) }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            userAuthRepo.updateProfile(
                displayName = _state.value.displayName.trim(),
                photoUrl = _state.value.photoUrl
            ).onSuccess {
                _state.update { it.copy(isLoading = false) }
                authEventBus.send(AuthEvent.ProfileSetupComplete)
            }.onError { error ->
                _state.update { it.copy(isLoading = false) }
                authEventBus.send(AuthEvent.Error(error))
            }
        }
    }

    private fun clearEmailVerificationError() =
        _state.update { it.copy(emailVerificationError = null) }

    private companion object {
        private const val TAG = "SignUpViewModel"
        private const val MAX_IMAGE_SIZE = 256 * 1024L // 256KB
        private const val PHOTO_FILE_NAME = "compressed_profile_photo"
        val log = logging()
    }
}