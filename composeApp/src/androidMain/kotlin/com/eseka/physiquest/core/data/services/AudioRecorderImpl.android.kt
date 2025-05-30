package com.eseka.physiquest.core.data.services

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import co.touchlab.kermit.Logger
import com.eseka.physiquest.core.domain.services.AudioRecorder
import java.io.File
import java.io.FileOutputStream

actual class AudioRecorderImpl : AudioRecorder {
    private var context: Context
    private var recorder: MediaRecorder? = null

    constructor(context: Context) {
        this.context = context
    }

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    actual override fun start(outputFile: Any) {
        try {
            val file = outputFile as File
            createRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(FileOutputStream(file).fd)
                prepare()
                start()
                recorder = this
            }
        } catch (e: Exception) {
            Logger.e(tag = TAG, message = { "Error starting audio recording: ${e.message}" })
        }
    }

    actual override fun stop() {
        try {
            recorder?.apply {
                stop()
                reset()
                release()
            }
            recorder = null
        } catch (e: Exception) {
            Logger.e(tag = TAG, message = { "Error stopping audio recording: ${e.message}" })
        }
    }

    private companion object {
        private const val TAG = "AudioRecorder"
    }
}