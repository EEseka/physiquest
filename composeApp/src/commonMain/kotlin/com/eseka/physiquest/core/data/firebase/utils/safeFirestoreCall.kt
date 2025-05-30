package com.eseka.physiquest.core.data.firebase.utils

import co.touchlab.kermit.Logger
import com.eseka.physiquest.core.domain.utils.FirebaseFirestoreError
import com.eseka.physiquest.core.domain.utils.Result
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.FirestoreExceptionCode
import dev.gitlive.firebase.firestore.code
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

private const val TAG = "safeFirebaseFirestoreCall"

suspend fun <T> safeFirebaseFirestoreCall(execute: suspend () -> T): Result<T, FirebaseFirestoreError> {
    return try {
        val result = execute()
        Result.Success(result)
    } catch (e: FirebaseFirestoreException) {
        Result.Error(mapFirestoreException(e))
    } catch (_: FirebaseNetworkException) {
        Result.Error(FirebaseFirestoreError.NETWORK_ERROR)
    } catch (e: Exception) {
        coroutineContext.ensureActive()
        Logger.e(tag = TAG, message = { "An unexpected error occurred" }, throwable = e)
        Result.Error(FirebaseFirestoreError.UNKNOWN)
    }
}

private fun mapFirestoreException(e: FirebaseFirestoreException): FirebaseFirestoreError {
    return when (e.code) {
        FirestoreExceptionCode.CANCELLED -> FirebaseFirestoreError.CANCELLED
        FirestoreExceptionCode.UNKNOWN -> FirebaseFirestoreError.UNKNOWN
        FirestoreExceptionCode.INVALID_ARGUMENT -> FirebaseFirestoreError.INVALID_ARGUMENT
        FirestoreExceptionCode.DEADLINE_EXCEEDED -> FirebaseFirestoreError.DEADLINE_EXCEEDED
        FirestoreExceptionCode.NOT_FOUND -> FirebaseFirestoreError.NOT_FOUND
        FirestoreExceptionCode.ALREADY_EXISTS -> FirebaseFirestoreError.ALREADY_EXISTS
        FirestoreExceptionCode.PERMISSION_DENIED -> FirebaseFirestoreError.PERMISSION_DENIED
        FirestoreExceptionCode.RESOURCE_EXHAUSTED -> FirebaseFirestoreError.RESOURCE_EXHAUSTED
        FirestoreExceptionCode.FAILED_PRECONDITION -> FirebaseFirestoreError.FAILED_PRECONDITION
        FirestoreExceptionCode.ABORTED -> FirebaseFirestoreError.ABORTED
        FirestoreExceptionCode.OUT_OF_RANGE -> FirebaseFirestoreError.OUT_OF_RANGE
        FirestoreExceptionCode.UNIMPLEMENTED -> FirebaseFirestoreError.UNIMPLEMENTED
        FirestoreExceptionCode.INTERNAL -> FirebaseFirestoreError.INTERNAL
        FirestoreExceptionCode.UNAVAILABLE -> FirebaseFirestoreError.UNAVAILABLE
        FirestoreExceptionCode.DATA_LOSS -> FirebaseFirestoreError.DATA_LOSS
        FirestoreExceptionCode.UNAUTHENTICATED -> FirebaseFirestoreError.UNAUTHENTICATED
        else -> {
            Logger.w(tag = TAG, message = { "Unhandled Firestore error code: ${e.code}" })
            FirebaseFirestoreError.UNKNOWN
        }
    }
}