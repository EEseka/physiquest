package com.eseka.physiquest.app.settings.domain

import com.eseka.physiquest.core.domain.utils.FirebaseAuthError
import com.eseka.physiquest.core.domain.utils.Result
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.FirebaseUser

interface UserRepo {
    val currentUser: FirebaseUser?
    suspend fun signOut(): Result<Unit, FirebaseAuthError>
    suspend fun deleteAccount(): Result<Unit, FirebaseAuthError>
    suspend fun updateProfile(
        displayName: String?,
        photoUri: String?
    ): Result<Unit, FirebaseAuthError>

    suspend fun reAuthenticateUser(credential: AuthCredential): Result<Unit, FirebaseAuthError>
}