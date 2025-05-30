package com.eseka.physiquest.core.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import physiquest.composeapp.generated.resources.Res
import physiquest.composeapp.generated.resources.microphone_permission_denied

@Composable
actual fun AudioRecordingPermissionHandler(
    micIcon: ImageVector,
    modifier: Modifier,
    onStartRecording: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    var audioPermissionRequested by rememberSaveable { mutableStateOf(false) }
    val permissionDeniedString =
        UiText.StringResourceId(Res.string.microphone_permission_denied).asString()

    // Permission launcher for audio recording
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        audioPermissionRequested = false
        if (isGranted) {
            onStartRecording()
        } else {
            onPermissionDenied()
            Toast.makeText(
                context,
                permissionDeniedString,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    IconButton(
        onClick = {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) -> {
                    onStartRecording()
                }

                else -> {
                    audioPermissionRequested = true
                }
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = micIcon,
            contentDescription = null
        )
    }

    // Permission Request Trigger
    LaunchedEffect(audioPermissionRequested) {
        if (audioPermissionRequested) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}