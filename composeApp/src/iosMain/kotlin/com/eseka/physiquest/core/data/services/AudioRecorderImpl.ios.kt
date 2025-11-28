package com.eseka.physiquest.core.data.services

import com.diamondedge.logging.logging
import com.eseka.physiquest.core.domain.services.AudioRecorder
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.setActive
import platform.Foundation.NSDictionary
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.dictionaryWithObjectsAndKeys
import platform.Foundation.numberWithInt

actual class AudioRecorderImpl : AudioRecorder {
    private var audioRecorder: AVAudioRecorder? = null
    private var audioSession: AVAudioSession = AVAudioSession.sharedInstance()

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual override fun start(outputFile: Any) {
        try {
            val fileURL = outputFile as NSURL

            memScoped {
                val sessionErrorPtr = alloc<ObjCObjectVar<NSError?>>()
                audioSession.setCategory(AVAudioSessionCategoryRecord, error = sessionErrorPtr.ptr)
                audioSession.setActive(true, error = sessionErrorPtr.ptr)

                val recorderErrorPtr = alloc<ObjCObjectVar<NSError?>>()

                val settings = NSDictionary.dictionaryWithObjectsAndKeys(
                    NSNumber.numberWithInt(1819304813), NSString.create("AVFormatIDKey"),
                    NSNumber.numberWithInt(44100), NSString.create("AVSampleRateKey"),
                    NSNumber.numberWithInt(1), NSString.create("AVNumberOfChannelsKey"),
                    NSNumber.numberWithInt(128000), NSString.create("AVEncoderBitRateKey"),
                    NSNumber.numberWithInt(12800), NSString.create("AVEncoderBitRatePerChannelKey"),
                    null
                )

                audioRecorder = AVAudioRecorder(fileURL, settings, recorderErrorPtr.ptr)

                val error = recorderErrorPtr.value
                if (error != null) {
                    log.e(
                        tag = TAG,
                        msg = { "Error creating audio recorder: ${error.localizedDescription()}" })
                    return@memScoped
                }

                audioRecorder?.prepareToRecord()
                audioRecorder?.record()
            }
        } catch (e: Exception) {
            log.e(tag = TAG, msg = { "Error starting audio recording: ${e.message}" })
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override fun stop() {
        try {
            audioRecorder?.stop()
            memScoped {
                val errorPtr = alloc<ObjCObjectVar<NSError?>>()
                audioSession.setActive(false, error = errorPtr.ptr)
            }
            audioRecorder = null
        } catch (e: Exception) {
            log.e(tag = TAG, msg = { "Error stopping audio recording: ${e.message}" })
        }
    }

    private companion object {
        private const val TAG = "AudioRecorder"
        val log = logging()
    }
}