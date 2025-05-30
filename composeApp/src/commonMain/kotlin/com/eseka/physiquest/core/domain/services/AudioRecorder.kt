package com.eseka.physiquest.core.domain.services

interface AudioRecorder {
    fun start(outputFile: Any) // Using Any since File is platform-specific
    fun stop()
}
