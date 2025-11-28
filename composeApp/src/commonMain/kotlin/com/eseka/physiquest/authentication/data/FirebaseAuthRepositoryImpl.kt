package com.eseka.physiquest.authentication.data

import com.diamondedge.logging.logging
import com.eseka.physiquest.authentication.domain.UserAuthRepo
import com.eseka.physiquest.core.data.firebase.utils.safeFirebaseAuthCall
import com.eseka.physiquest.core.data.firebase.utils.safeFirebaseStorageCall
import com.eseka.physiquest.core.domain.MediaStorage
import com.eseka.physiquest.core.domain.utils.FirebaseAuthError
import com.eseka.physiquest.core.domain.utils.FirebaseStorageError
import com.eseka.physiquest.core.domain.utils.Result
import com.eseka.physiquest.core.domain.utils.onError
import com.eseka.physiquest.core.domain.utils.onSuccess
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FirebaseAuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val storage: MediaStorage
) : UserAuthRepo {
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

    override suspend fun getAuthState(): Result<Flow<Boolean>, FirebaseAuthError> =
        safeFirebaseAuthCall {
            _currentUserFlow.map { it != null }.distinctUntilChanged()
        }

    override suspend fun signIn(email: String, password: String): Result<Unit, FirebaseAuthError> =
        safeFirebaseAuthCall {
            auth.signInWithEmailAndPassword(email, password)
        }

    override suspend fun signInWithGoogle(
        idToken: String?,
        accessToken: String?
    ): Result<Unit, FirebaseAuthError> =
        safeFirebaseAuthCall {
            val credential = accessToken?.let {
                GoogleAuthProvider.credential(idToken, it)
            } ?: GoogleAuthProvider.credential(idToken, null)
            auth.signInWithCredential(credential)
        }

    override suspend fun createAccount(
        email: String,
        password: String
    ): Result<Unit, FirebaseAuthError> =
        safeFirebaseAuthCall {
            auth.createUserWithEmailAndPassword(email, password)
        }

    override suspend fun sendEmailVerification(): Result<Unit, FirebaseAuthError> =
        safeFirebaseAuthCall {
            currentUser?.sendEmailVerification()
        }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit, FirebaseAuthError> =
        safeFirebaseAuthCall {
            auth.sendPasswordResetEmail(email)
        }

    override suspend fun reloadUser(): Result<Unit, FirebaseAuthError> =
        safeFirebaseAuthCall {
            currentUser?.reload()
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

    companion object {
        private const val PROFILE_PHOTO_STORAGE_PATH = "profile_pictures/"
        private const val TAG = "FirebaseAuthRepositoryImpl"
        val log = logging()
    }
}