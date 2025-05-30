package com.eseka.physiquest.core.data.firebase.utils

import co.touchlab.kermit.Logger
import com.eseka.physiquest.core.domain.utils.FirebaseAuthError
import com.eseka.physiquest.core.domain.utils.Result
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

private const val TAG = "safeFirebaseAuthCall"

suspend fun <T> safeFirebaseAuthCall(execute: suspend () -> T): Result<T, FirebaseAuthError> {
    return try {
        val result = execute()
        Result.Success(result)
    } catch (e: FirebaseAuthException) {
        Result.Error(mapFirebaseAuthException(e))
    } catch (_: FirebaseNetworkException) {
        Result.Error(FirebaseAuthError.NETWORK_ERROR)
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        Logger.e(tag = TAG, message = { "An unexpected error occurred: $e" })
        Result.Error(FirebaseAuthError.UNKNOWN)
    }
}

private fun mapFirebaseAuthException(e: FirebaseAuthException): FirebaseAuthError {
    val message = e.message ?: ""
    return when {
        "invalid-email" in message -> FirebaseAuthError.INVALID_EMAIL
        "user-not-found" in message -> FirebaseAuthError.USER_NOT_FOUND
        "wrong-password" in message -> FirebaseAuthError.WRONG_PASSWORD
        "The email address is already in use" in message -> FirebaseAuthError.EMAIL_ALREADY_IN_USE
        "weak-password" in message -> FirebaseAuthError.WEAK_PASSWORD
        "too-many-requests" in message -> FirebaseAuthError.TOO_MANY_REQUESTS
        "operation-not-allowed" in message -> FirebaseAuthError.OPERATION_NOT_ALLOWED
        "account-exists-with-different-credential" in message -> FirebaseAuthError.ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL
        "credential-already-in-use" in message -> FirebaseAuthError.CREDENTIAL_ALREADY_IN_USE
        "invalid-credential" in message -> FirebaseAuthError.INVALID_CREDENTIAL
        "requires-recent-login" in message -> FirebaseAuthError.REQUIRES_RECENT_LOGIN
        "network-request-failed" in message -> FirebaseAuthError.NETWORK_ERROR
        "user-token-expired" in message -> FirebaseAuthError.USER_TOKEN_EXPIRED
        "invalid-user-token" in message -> FirebaseAuthError.INVALID_USER_TOKEN
        "user-not-signed-in" in message -> FirebaseAuthError.USER_NOT_SIGNED_IN
        "session-expired" in message -> FirebaseAuthError.SESSION_EXPIRED
        "quota-exceeded" in message -> FirebaseAuthError.QUOTA_EXCEEDED
        "unauthorized-domain" in message -> FirebaseAuthError.UNAUTHORIZED_DOMAIN
        "invalid-photo-url" in message -> FirebaseAuthError.INVALID_PHOTO_URL
        "disabled by an administrator" in message -> FirebaseAuthError.USER_DISABLED
        "credential is incorrect" in message -> FirebaseAuthError.INVALID_CREDENTIAL
        "incorrect, malformed or has expired" in message -> FirebaseAuthError.INVALID_CREDENTIAL
        "credential is malformed or has expired." in message -> FirebaseAuthError.INVALID_CREDENTIAL
        "invalid password" in message -> FirebaseAuthError.WRONG_PASSWORD
        else -> {
            Logger.w(tag = TAG, message = { "Unhandled exception message: $message" })
            FirebaseAuthError.UNKNOWN
        }
    }
}