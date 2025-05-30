package com.eseka.physiquest.core.data.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.eseka.physiquest.core.domain.services.AudioPermissionHandler

actual class AudioPermissionHandlerImpl(
    private val context: Context
) : AudioPermissionHandler {

    actual override suspend fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    actual override suspend fun requestPermission(): Boolean {
        return hasPermission()
    }
}