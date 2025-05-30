package com.eseka.physiquest.app.chat.data.utils

import co.touchlab.kermit.Logger
import com.eseka.physiquest.core.domain.utils.DataError
import com.eseka.physiquest.core.domain.utils.Result
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerializationException
import kotlin.coroutines.coroutineContext

suspend inline fun <T> safeOpenAiNetworkCall(crossinline execute: suspend () -> T): Result<T, DataError.Remote> {
    return try {
        val response = withTimeout(30000L) { execute() }
        Result.Success(response)
    } catch (_: UnresolvedAddressException) {
        Result.Error(DataError.Remote.NO_INTERNET)
    } catch (_: SerializationException) {
        Result.Error(DataError.Remote.SERIALIZATION)
    } catch (_: TimeoutCancellationException) {
        Result.Error(DataError.Remote.REQUEST_TIMEOUT)
    } catch (e: Exception) {
        Logger.e(tag = "OpenAiRepository", message = { "Exception: ${e.message}" })
        coroutineContext.ensureActive() // Avoids cancelled coroutines from being caught and not propagated up
        Result.Error(DataError.Remote.UNKNOWN)
    }
}
