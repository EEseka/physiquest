package com.eseka.physiquest.core.presentation

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.eseka.physiquest.core.data.services.AudioPermissionHandlerImpl
import kotlinx.coroutines.launch

@Composable
actual fun AudioRecordingPermissionHandler(
    micIcon: ImageVector,
    modifier: Modifier,
    onStartRecording: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val permissionHandler = remember { AudioPermissionHandlerImpl() }
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = {
            scope.launch {
                if (permissionHandler.hasPermission()) {
                    onStartRecording()
                } else {
                    val granted = permissionHandler.requestPermission()
                    if (granted) {
                        onStartRecording()
                    } else {
                        onPermissionDenied()
                    }
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
}