package com.eseka.physiquest.app.settings.data

import com.diamondedge.logging.logging
import com.eseka.physiquest.app.settings.domain.UserRepo
import com.eseka.physiquest.core.data.firebase.utils.safeFirebaseAuthCall
import com.eseka.physiquest.core.data.firebase.utils.safeFirebaseStorageCall
import com.eseka.physiquest.core.domain.MediaStorage
import com.eseka.physiquest.core.domain.utils.FirebaseAuthError
import com.eseka.physiquest.core.domain.utils.FirebaseStorageError
import com.eseka.physiquest.core.domain.utils.Result
import com.eseka.physiquest.core.domain.utils.onError
import com.eseka.physiquest.core.domain.utils.onSuccess
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class UserRepoImpl(
    private val auth: FirebaseAuth,
    private val storage: MediaStorage
) : UserRepo {
    private val _currentUserFlow: StateFlow<FirebaseUser?> = callbackFlow {
        // Listen to auth state changes reactively
        val authStateJob = launch {
            auth.authStateChanged.collect { user ->
                trySend(user)
            }
        }

        // Periodically reload the current user to detect remote deletion
        val reloadJob = launch(Dispatchers.Default) {
            while (currentCoroutineContext().isActive) {
                delay(30_000)
                val user = auth.currentUser
                if (user != null) {
                    try {
                        user.reload()
                    } catch (e: Exception) {
                        log.e(
                            tag = TAG,
                            msg = { "Failed to reload user: ${e.message}" },
                            err = e
                        )
                        trySend(null)
                    }
                }
            }
        }

        awaitClose {
            authStateJob.cancel()
            reloadJob.cancel()
        }
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
        started = SharingStarted.Eagerly,
        initialValue = auth.currentUser
    )

    override val currentUser: FirebaseUser?
        get() = _currentUserFlow.value

    override suspend fun signOut(): Result<Unit, FirebaseAuthError> =
        safeFirebaseAuthCall {
            auth.signOut()
        }

    override suspend fun deleteAccount(): Result<Unit, FirebaseAuthError> =
        safeFirebaseAuthCall {
            currentUser?.delete()
        }

    override suspend fun updateProfile(
        displayName: String?,
        photoUrl: String?
    ): Result<Unit, FirebaseAuthError> {
        val userId = currentUser?.uid
        var finalPhotoUrl: String? = null

        if (photoUrl != null && userId != null) {
            safeFirebaseStorageCall {
                storage.uploadPicture(PROFILE_PHOTO_STORAGE_PATH + userId, photoUrl)
                    .onSuccess { secureUrl ->
                        finalPhotoUrl = secureUrl
                    }
                    .onError { error ->
                        log.e(
                            tag = TAG,
                            msg = { "Failed to upload profile image to Firebase Storage : $error" }
                        )
                        Result.Error(FirebaseStorageError.IO_ERROR)
                    }
            }
        }

        return safeFirebaseAuthCall {
            val user = currentUser ?: return@safeFirebaseAuthCall
            displayName?.let { user.updateProfile(displayName = it) }
            photoUrl?.let { user.updateProfile(photoUrl = it) }
            user.reload()
        }
    }

    override suspend fun reAuthenticateUser(credential: AuthCredential): Result<Unit, FirebaseAuthError> =
        safeFirebaseAuthCall {
            currentUser?.reauthenticate(credential)
        }

    companion object {
        private const val PROFILE_PHOTO_STORAGE_PATH = "profile_pictures/"
        private const val TAG = "UserRepoImpl"
        val log = logging()
    }
}