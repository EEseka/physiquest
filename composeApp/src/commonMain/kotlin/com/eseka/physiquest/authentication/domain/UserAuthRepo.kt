package com.eseka.physiquest.authentication.domain

import com.eseka.physiquest.core.domain.utils.FirebaseAuthError
import com.eseka.physiquest.core.domain.utils.Result
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface UserAuthRepo {
    val currentUser: FirebaseUser?
    suspend fun getAuthState(): Result<Flow<Boolean>, FirebaseAuthError>
    suspend fun signIn(email: String, password: String): Result<Unit, FirebaseAuthError>
    suspend fun signInWithGoogle(
        idToken: String?,
        accessToken: String?
    ): Result<Unit, FirebaseAuthError>

    suspend fun createAccount(email: String, password: String): Result<Unit, FirebaseAuthError>
    suspend fun sendEmailVerification(): Result<Unit, FirebaseAuthError>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit, FirebaseAuthError>
    suspend fun reloadUser(): Result<Unit, FirebaseAuthError>
    suspend fun updateProfile(
        displayName: String?,
        photoUrl: String?
    ): Result<Unit, FirebaseAuthError>
}