package com.eseka.physiquest.core.data.services

import com.eseka.physiquest.core.domain.services.AudioRecorder

expect class AudioRecorderImpl : AudioRecorder {
    override fun start(outputFile: Any)
    override fun stop()
}