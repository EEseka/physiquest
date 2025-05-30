package com.eseka.physiquest.core.data.firebase.utils

import co.touchlab.kermit.Logger
import com.eseka.physiquest.core.domain.utils.FirebaseStorageError
import com.eseka.physiquest.core.domain.utils.Result
import dev.gitlive.firebase.FirebaseNetworkException
import kotlinx.coroutines.ensureActive
import kotlinx.io.IOException
import kotlin.coroutines.coroutineContext

private const val TAG = "safeFirebaseStorageCall"

suspend fun <T> safeFirebaseStorageCall(execute: suspend () -> T): Result<T, FirebaseStorageError> {
    return try {
        val result = execute()
        Result.Success(result)
    } catch (_: FirebaseNetworkException) {
        Result.Error(FirebaseStorageError.NETWORK_ERROR)
    } catch (e: IOException) {
        Logger.e(tag = TAG, message = { "IO error during Firebase Storage operation: $e" })
        Result.Error(FirebaseStorageError.IO_ERROR)
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        Logger.e(tag = TAG, message = { "Unexpected error during Firebase Storage operation: $e" })
        Result.Error(mapStorageException(e))
    }
}

// Optionally: Parse message for known error strings, if you want more granularity
private fun mapStorageException(e: Exception): FirebaseStorageError {
    val msg = e.message ?: ""
    return when {
        "object-not-found" in msg -> FirebaseStorageError.OBJECT_NOT_FOUND
        "bucket-not-found" in msg -> FirebaseStorageError.BUCKET_NOT_FOUND
        "project-not-found" in msg -> FirebaseStorageError.PROJECT_NOT_FOUND
        "quota-exceeded" in msg -> FirebaseStorageError.QUOTA_EXCEEDED
        "not-authenticated" in msg -> FirebaseStorageError.NOT_AUTHENTICATED
        "not-authorized" in msg -> FirebaseStorageError.NOT_AUTHORIZED
        "retry-limit-exceeded" in msg -> FirebaseStorageError.RETRY_LIMIT_EXCEEDED
        "invalid-checksum" in msg -> FirebaseStorageError.INVALID_CHECKSUM
        "canceled" in msg -> FirebaseStorageError.CANCELED
        else -> FirebaseStorageError.UNKNOWN
    }
}