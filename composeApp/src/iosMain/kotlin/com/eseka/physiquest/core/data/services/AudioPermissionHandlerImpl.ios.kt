package com.eseka.physiquest.core.data.services

import com.eseka.physiquest.core.domain.services.AudioPermissionHandler
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVAudioSessionRecordPermissionUndetermined
import kotlin.coroutines.resume

actual class AudioPermissionHandlerImpl : AudioPermissionHandler {

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun hasPermission(): Boolean {
        val audioSession = AVAudioSession.sharedInstance()
        return audioSession.recordPermission() == AVAudioSessionRecordPermissionGranted
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun requestPermission(): Boolean =
        suspendCancellableCoroutine { continuation ->
            val audioSession = AVAudioSession.sharedInstance()

            when (audioSession.recordPermission()) {
                AVAudioSessionRecordPermissionGranted -> {
                    continuation.resume(true)
                }

                AVAudioSessionRecordPermissionDenied -> {
                    continuation.resume(false)
                }

                AVAudioSessionRecordPermissionUndetermined -> {
                    audioSession.requestRecordPermission { granted ->
                        continuation.resume(granted)
                    }
                }
            }
        }
}
