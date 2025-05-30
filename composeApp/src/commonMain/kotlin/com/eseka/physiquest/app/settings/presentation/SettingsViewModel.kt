package com.eseka.physiquest.app.settings.presentation

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eseka.physiquest.app.MainEvent
import com.eseka.physiquest.app.MainEventBus
import com.eseka.physiquest.app.settings.domain.UserRepo
import com.eseka.physiquest.core.domain.services.FileManager
import com.eseka.physiquest.core.domain.services.ImageCompressor
import com.eseka.physiquest.core.domain.utils.FirebaseAuthError
import com.eseka.physiquest.core.domain.utils.onError
import com.eseka.physiquest.core.domain.utils.onSuccess
import com.eseka.physiquest.core.domain.validation.ValidateDisplayName
import com.eseka.physiquest.core.presentation.utils.toUiText
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class SettingsViewModel(
    private val userRepoUseCase: UserRepo,
    private val imageCompressor: ImageCompressor,
    private val fileManager: FileManager,
    private val validateDisplayName: ValidateDisplayName,
    private val mainEventBus: MainEventBus
) : ViewModel() {
    private val user = userRepoUseCase.currentUser

    // State preservation variables
    private var lastEditTimestamp: Long = 0
    private var temporaryDisplayName: TextFieldValue? = null
    private var temporaryPhotoUri: String? = null

    private val _state = MutableStateFlow(
        SettingsState(
            displayName = TextFieldValue(
                text = user?.displayName ?: "User${user?.uid}",
                selection = TextRange(
                    user?.displayName?.length ?: (4 + user?.uid.toString().length)
                )
            ),
            email = user?.email ?: "",
            photoUrl = user?.photoURL
        )
    )
    val state = _state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        SettingsState(
            displayName = TextFieldValue(
                text = user?.displayName ?: "User${user?.uid}",
                selection = TextRange(
                    user?.displayName?.length ?: (4 + user?.uid.toString().length)
                )
            ),
            email = user?.email ?: "",
            photoUrl = user?.photoURL
        )
    )

    fun onEvent(event: SettingsEvents) {
        when (event) {
            is SettingsEvents.OnDeleteAccountClicked -> deleteAccount()
            is SettingsEvents.OnDisplayNameChanged -> {
                lastEditTimestamp = Clock.System.now().toEpochMilliseconds()
                temporaryDisplayName = event.name
                _state.update { it.copy(displayName = event.name) }
            }

            is SettingsEvents.OnPhotoSelected -> selectImage(event.url, event.extension)
            SettingsEvents.OnSignOutClicked -> signOut()
            SettingsEvents.OnUpdateProfileClicked -> validateAndUpdateProfile()
            SettingsEvents.OnScreenLeave -> preserveTemporaryState()
            SettingsEvents.OnScreenReturn -> restoreTemporaryState()
            is SettingsEvents.OnReAuthenticateWithGoogle -> reAuthenticateAndDeleteGoogleAccount(
                event.idToken,
                event.accessToken
            )

            is SettingsEvents.OnReAuthenticateWithPassword -> reAuthenticateAndDeleteEmailAccount(
                user?.email,
                event.password
            )
        }
    }

    private fun preserveTemporaryState() {
        viewModelScope.launch {
            // Start a timer to potentially reset state
            delay(30_000)

            val currentTime = Clock.System.now().toEpochMilliseconds()
            if (currentTime - lastEditTimestamp >= 30_000) {
                // Reset temporary state if 30 seconds have passed
                temporaryDisplayName = null
                temporaryPhotoUri = null
            }
        }
    }

    private fun restoreTemporaryState() {
        val currentTime = Clock.System.now().toEpochMilliseconds()

        // Restore temporary display name if within 30 seconds
        if (temporaryDisplayName != null &&
            currentTime - lastEditTimestamp < 30_000
        ) {
            _state.update {
                it.copy(
                    displayName = temporaryDisplayName!!,
                    photoUrl = temporaryPhotoUri ?: it.photoUrl
                )
            }
        } else {
            // Reset to original state if 30 seconds have passed
            _state.update {
                it.copy(
                    displayName = TextFieldValue(
                        text = user?.displayName ?: "User${user?.uid}",
                        selection = TextRange(
                            user?.displayName?.length ?: (4 + user?.uid.toString().length)
                        )
                    ),
                    photoUrl = user?.photoURL
                )
            }
            temporaryDisplayName = null
            temporaryPhotoUri = null
        }
    }

    private fun selectImage(uri: String, extension: String) {
        viewModelScope.launch {
            _state.update { it.copy(isProfileUpdating = true) }

            imageCompressor.compressImage(uri, MAX_IMAGE_SIZE)
                .onSuccess { byteArray ->
                    val compressedUri = fileManager.saveImageToCache(
                        byteArray,
                        "$PHOTO_FILE_NAME${Clock.System.now().toEpochMilliseconds()}.$extension"
                    )

                    lastEditTimestamp = Clock.System.now().toEpochMilliseconds()
                    temporaryPhotoUri = compressedUri

                    _state.update {
                        it.copy(
                            photoUrl = compressedUri,
                            isProfileUpdating = false
                        )
                    }
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            isProfileUpdating = false,
                            photoUrlError = error.toUiText()
                        )
                    }
                }
        }
    }

    private fun validateAndUpdateProfile() {
        val displayNameResult = validateDisplayName(_state.value.displayName.text.trim())

        if (!displayNameResult.successful) {
            _state.update {
                it.copy(displayNameError = displayNameResult.errorMessage?.toUiText())
            }
            return
        }
        _state.update { it.copy(displayNameError = null) }
        viewModelScope.launch {
            val isNameSame = _state.value.displayName.text.trim() == user?.displayName
            val isPhotoSame = _state.value.photoUrl == user?.photoURL

            if (isNameSame && isPhotoSame) return@launch
            _state.update { it.copy(isProfileUpdating = true) }

            userRepoUseCase.updateProfile(
                displayName = if (isNameSame) null else _state.value.displayName.text.trim(),
                photoUri = if (isPhotoSame) null else _state.value.photoUrl
            ).onSuccess {
                _state.update { it.copy(isProfileUpdating = false) }
                mainEventBus.send(MainEvent.ProfileUpdateComplete)
            }.onError { error ->
                _state.update { it.copy(isProfileUpdating = false) }
                mainEventBus.send(MainEvent.AuthError(error))
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _state.update { it.copy(isSigningOut = true) }

            userRepoUseCase.signOut()
                .onSuccess {
                    _state.update { it.copy(isSigningOut = false) }
                    mainEventBus.send(MainEvent.SignOutComplete)
                }
                .onError { error ->
                    _state.update { it.copy(isSigningOut = false) }
                    mainEventBus.send(MainEvent.AuthError(error))
                }
        }
    }

    private fun deleteAccount() {
        viewModelScope.launch {
            if (user == null) {
                mainEventBus.send(MainEvent.AuthError(FirebaseAuthError.USER_NOT_FOUND))
                return@launch
            }
            val providers = user.providerData.map { it.providerId }
            val usesGoogle = "google.com" in providers

            if (usesGoogle) {
                mainEventBus.send(MainEvent.ReAuthenticateWithGoogle)
            } else {
                mainEventBus.send(MainEvent.ReAuthenticateWithPassword)
            }
        }
    }

    private fun reAuthenticateAndDeleteGoogleAccount(idToken: String?, accessToken: String?) {
        _state.update { it.copy(isDeletingAccount = true) }
        viewModelScope.launch {
            val credential = accessToken?.let {
                GoogleAuthProvider.credential(idToken, it)
            } ?: GoogleAuthProvider.credential(idToken, null)
            completeReauthenticationAndDeletion(credential)
        }
    }

    private fun reAuthenticateAndDeleteEmailAccount(email: String?, password: String) {
        _state.update { it.copy(isDeletingAccount = true) }
        viewModelScope.launch {
            if (email.isNullOrEmpty()) {
                mainEventBus.send(MainEvent.AuthError(FirebaseAuthError.USER_NOT_FOUND))
                return@launch
            }
            val credential = EmailAuthProvider.credential(email, password)
            completeReauthenticationAndDeletion(credential)
        }
    }

    private suspend fun completeReauthenticationAndDeletion(authCredential: AuthCredential) {
        userRepoUseCase.reAuthenticateUser(authCredential)
            .onSuccess {
                userRepoUseCase.deleteAccount()
                    .onSuccess {
                        _state.update { it.copy(isDeletingAccount = false) }
                        mainEventBus.send(MainEvent.AccountDeletionComplete)
                    }
                    .onError { error ->
                        _state.update { it.copy(isDeletingAccount = false) }
                        mainEventBus.send(MainEvent.AuthError(error))
                    }
            }
            .onError { error ->
                _state.update { it.copy(isDeletingAccount = false) }
                mainEventBus.send(MainEvent.AuthError(error))
            }
    }

    private companion object {
        private const val TAG = "SettingsViewModel"
        private const val MAX_IMAGE_SIZE = 256 * 1024L // 256KB
        private const val PHOTO_FILE_NAME = "compressed_profile_photo"
    }
}