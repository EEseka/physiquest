package com.eseka.physiquest.app

import com.eseka.physiquest.core.domain.utils.DataError
import com.eseka.physiquest.core.domain.utils.FirebaseAuthError
import com.eseka.physiquest.core.domain.utils.FirebaseFirestoreError

sealed interface MainEvent {
    data class AuthError(val error: FirebaseAuthError) : MainEvent
    data class NetworkingError(val error: DataError.Remote) : MainEvent
    data class DatabaseError(val error: FirebaseFirestoreError) : MainEvent
    data object ProfileUpdateComplete : MainEvent
    data object SignOutComplete : MainEvent
    data object AccountDeletionComplete : MainEvent
    data object ReAuthenticateWithGoogle : MainEvent
    data object ReAuthenticateWithPassword : MainEvent
}