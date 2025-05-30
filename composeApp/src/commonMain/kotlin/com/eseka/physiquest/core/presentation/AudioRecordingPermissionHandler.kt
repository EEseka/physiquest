package com.eseka.physiquest.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
expect fun AudioRecordingPermissionHandler(
    micIcon: ImageVector,
    modifier: Modifier = Modifier,
    onStartRecording: () -> Unit,
    onPermissionDenied: () -> Unit = {}
)
